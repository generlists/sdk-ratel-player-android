package com.sean.ratel.player.core.data.player.media

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import androidx.annotation.OptIn
import androidx.core.graphics.createBitmap
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.BandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.extractor.DefaultExtractorInput
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.DummyTrackOutput
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.PositionHolder
import androidx.media3.extractor.SeekMap
import androidx.media3.extractor.TrackOutput
import com.sean.ratel.player.core.Configurations
import com.sean.ratel.player.core.configurations
import com.sean.ratel.player.core.data.domain.MediaStreamPlayer
import com.sean.ratel.player.core.data.domain.model.MediaStreamTransitionReason
import com.sean.ratel.player.core.data.domain.model.PlayMuteInfo
import com.sean.ratel.player.core.data.domain.model.PlaySpeed
import com.sean.ratel.player.core.data.domain.model.PlaybackState
import com.sean.ratel.player.core.data.domain.model.RepeatMode
import com.sean.ratel.player.core.data.domain.model.Resolution
import com.sean.ratel.player.core.data.domain.model.SampleBandWidth
import com.sean.ratel.player.core.data.domain.model.track.AudioTrack
import com.sean.ratel.player.core.data.domain.model.track.VideoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import so.smartlab.common.utils.log.RLog
import java.util.TreeMap
import kotlin.math.roundToInt

@UnstableApi
class MediaExoStreamPlayer(
    private val context: Context,
    private val datasourceFactory: DataSource.Factory,
    // userAgentProvider: UserAgentProvider
) : MediaStreamPlayer,
    Player.Listener {
    private var player: ExoPlayer? = null
    private var bufferForPlaybackMs: Int = BUFFER_FOR_PLAYBACK_TIME
    private var bufferForPlaybackAfterRebufferMs: Int = BUFFER_FOR_PLAYBACK_AFTER_REBUFFER
    private var maxBufferMs: Int = MAX_BUFFER_PLAYBACK_TIME
    private var minBufferMs: Int = MIN_BUFFER_PLAYBACK_TIME
    private var isPreparing = false
    private var playerIndex: Int? = null
    private var userAgent: String = "exoplayer" // userAgentProvider.userAgent

    private val defaultBandwidthMeter =
        DefaultBandwidthMeter
            .Builder(context)
            .setResetOnNetworkTypeChange(true)
            /** Network changes invalidate existing data **/
            // .setInitialBitrateEstimate(Integer.MAX_VALUE.toLong()) // later tuning
            .build()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle(playerIndex))
    override val playbackState: Flow<PlaybackState> = _playbackState

    private val _playbackErrorState = MutableStateFlow<PlaybackState>(PlaybackState.Idle(playerIndex))

    override val playbackErrorState: Flow<PlaybackState> =
        _playbackErrorState

    private val _isMute = MutableStateFlow(PlayMuteInfo(0, false))
    override val isMute: StateFlow<PlayMuteInfo> = _isMute

    private val _volume = MutableStateFlow(0f)
    override val volume: StateFlow<Float> = _volume
    private val _lastSystemVolume = MutableStateFlow<Float>(0f)

    private val _maximumVideoQuality = MutableStateFlow<Int>(Int.MAX_VALUE)
    override val maximumVideoQuality: StateFlow<Int> = _maximumVideoQuality

    private val _videoTracks = MutableStateFlow<List<VideoTrack>>(emptyList())
    private val _audioTracks = MutableStateFlow<List<AudioTrack>>(emptyList())

    override val videoTracks: StateFlow<List<VideoTrack>> = _videoTracks
    override val audioTracks: StateFlow<List<AudioTrack>> = _audioTracks

    private val _selectedAudioTrack = MutableStateFlow<AudioTrack?>(null)
    override val selectedAudioTrack: StateFlow<AudioTrack?> = _selectedAudioTrack

    private val _selectedVideoTrack = MutableStateFlow<VideoTrack?>(null)
    override val selectedVideoTrack: StateFlow<VideoTrack?> = _selectedVideoTrack

    private val _seekBackIncrement = MutableStateFlow<Long>(SEEK_BACK_INCREMENTS_MS)
    override val seekBackIncrement: StateFlow<Long> = _seekBackIncrement

    private val _seekForWardIncrement = MutableStateFlow<Long>(SEEK_FOWARD_INCREMENTS_MS)
    override val seekForwardIncrement: StateFlow<Long> = _seekForWardIncrement

    private val _playSpeed = MutableStateFlow<PlaySpeed>(PlaySpeed.PlaySpeed_1_0)
    override val playSpeed: StateFlow<PlaySpeed> = _playSpeed

    private val _mediaType = MutableStateFlow<Set<Int>>(setOf(C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_VIDEO))
    override val mediaType: StateFlow<Set<Int>> = _mediaType

    private val _duration = MutableStateFlow<Long?>(null)
    override val duration: StateFlow<Long?> = _duration

    private val _isShuffleOn = MutableStateFlow<Boolean>(false)
    override val isShuffleOn: StateFlow<Boolean> = _isShuffleOn

    private val _currentPosition = MutableStateFlow<Long?>(null)

    override val currentPosition: StateFlow<Long?> = _currentPosition

    private val _repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.REPEAT_OFF)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private var positionJob: Job? = null
    private var playerInternalScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _currentIndex = MutableStateFlow<Int>(0)
    override val currentIndex: StateFlow<Int> = _currentIndex

    private val _resolution =
        MutableStateFlow(
            Resolution(
                1080,
                720,
                1F,
            ),
        )
    override val resolution: Flow<Resolution> = _resolution

    private val _sampleBandWidth = MutableStateFlow(SampleBandWidth(0, 0L, 0L))
    override val sampleBandWidth: Flow<SampleBandWidth> = _sampleBandWidth

    override fun setPlayerConfig(configurations: Configurations) {
        this.bufferForPlaybackAfterRebufferMs =
            configurations.videoConfig.bufferForPlaybackAfterRebufferMs
                ?: BUFFER_FOR_PLAYBACK_AFTER_REBUFFER // rebuffering
        this.bufferForPlaybackMs =
            configurations.videoConfig.bufferForPlaybackMs ?: BUFFER_FOR_PLAYBACK_TIME // init,seek
        this.maxBufferMs =
            configurations.videoConfig.maxBufferMs ?: MAX_BUFFER_PLAYBACK_TIME // max buffering
        this.minBufferMs =
            configurations.videoConfig.minBufferMs ?: MIN_BUFFER_PLAYBACK_TIME // min buffering

        this._seekBackIncrement.value =
            configurations.videoConfig.seekBackIncrementMs ?: SEEK_BACK_INCREMENTS_MS

        this._seekForWardIncrement.value =
            configurations.videoConfig.seekForwardIncrementMs ?: SEEK_FOWARD_INCREMENTS_MS

        this._maximumVideoQuality.update {
            configurations.videoConfig.maximumVideoQuality ?: MAX_VIDEO_QUALITY
        }
    }

    override fun start(
        uri: Uri,
        cacheKey: String?,
    ) {
        player?.let { release() }
        RLog.d("MediaScreen", "start!!!!!!!!!!!!!!!!!! $uri  $cacheKey")
        _playbackState.update { PlaybackState.Idle(playerIndex) }

        player =
            createPlayer()?.apply {
                initListener(this)
                isPreparing = true
                _playbackState.update {
                    PlaybackState.Preparing(playerIndex)
                }

                setMediaSource(getMediaSource(uri, cacheKey))
                _mediaType.value = detectMediaType(datasourceFactory, uri)

                prepare()
                mutePlay(_isMute.value)
                setShuffleOn(_isShuffleOn.value)
                setRepeat(_repeatMode.value)

                playWhenReady = true
            }

        RLog.d(
            "PLAYER",
            "play = $uri userAgent : $userAgent ,  player : $player , cacheKey : $cacheKey",
        )
    }

    override fun start(
        items: List<MediaItem>,
        startIndex: Int,
        cacheKey: String?,
    ) {
        player?.release()
        RLog.d("MediaScreen", "start!!!!!!!!!!!!!!")
        _playbackState.update { PlaybackState.Idle() }

        player =
            createPlayer()?.apply {
                initListener(this)

                isPreparing = true
                _playbackState.update {
                    PlaybackState.Preparing()
                }

                setMediaItems(items, startIndex, C.TIME_UNSET)

                prepare()
                // _mediaType.value = detectMediaType(datasourceFactory,uri)

                mutePlay(_isMute.value)

                playWhenReady = true
            }
    }

    override fun rePlay(mediaIndex: Int) {
        seekTo(mediaIndex, 0)

        isPreparing = true
        _playbackState.update {
            PlaybackState.Preparing()
        }
        player?.prepare()

        mutePlay(_isMute.value)

        player?.playWhenReady = true
    }

    override fun replaceMediaItem(
        index: Int,
        newMediaItem: MediaItem,
    ) {
        // player?.replaceMediaItem(index, newMediaItem)  todo 안됨

        val wasPlaying = player?.isPlaying == true

        val currentIndex = player?.currentMediaItemIndex ?: return
        val currentPosition = player?.currentPosition ?: 0
        player?.stop()
        player?.removeMediaItem(currentIndex)
        player?.addMediaItem(currentIndex, newMediaItem)
        player?.seekTo(currentIndex, currentPosition)
        isPreparing = true
        _playbackState.update {
            PlaybackState.Preparing()
        }
        player?.prepare()

        player?.playWhenReady = wasPlaying
    }

    override fun start(uri: Uri) {
        start(uri, null)
    }

    override fun isPrevItem(): Boolean = player?.hasPreviousMediaItem() ?: false

    override fun pervPlay(isReset: Boolean) {
        if (isReset) {
            player?.let { p ->
                p.stop()
                p.seekToPreviousMediaItem()
                p.prepare()
                p.play()
            }
            return
        }
        player?.seekToPreviousMediaItem()
    }

    override fun isNextItem(): Boolean = player?.hasNextMediaItem() ?: false

    override fun nextPlay(isReset: Boolean) {
        if (isReset) {
            player?.let { p ->
                p.stop()
                p.seekToNextMediaItem()
                p.prepare()
                p.play()
            }
            return
        }
        player?.seekToNextMediaItem()
    }

    override fun seekForward() {
        player?.seekForward()
    }

    override fun seekBack() {
        player?.seekBack()
    }

    override fun resume() {
        val player = player ?: return
        RLog.d(TAG, "resume() playbackState= ${player.playbackState}")
        if (player.playbackState == ExoPlayer.STATE_IDLE) { // Not ready to play.
            return
        }
        if (!player.playWhenReady) {
            player.playWhenReady = true
        }
        when (player.playbackState) {
            ExoPlayer.STATE_BUFFERING -> {
                if (player.playWhenReady) {
                    _playbackState.update {
                        PlaybackState.Buffering(
                            playerIndex,
                        )
                    }
                }
            }

            ExoPlayer.STATE_READY -> {
                _playbackState.update {
                    PlaybackState.Playing(
                        player = this,
                        playerIndex,
                    )
                }
            }

            else -> {
                Unit
            }
        }
    }

    override fun clearVideoSurface() = setVideoSurfaceView(null)

    override fun setMute(mute: Boolean) {
        _isMute.update { PlayMuteInfo(0, mute) }
    }

    override fun setMute(
        playIndex: Int,
        mute: Boolean,
    ) {
        _isMute.update { PlayMuteInfo(playIndex, mute) }
    }

    override fun setMaximumVideoQuality(
        quality: Int,
        isUserSelect: Boolean,
    ) {
        RLog.d(TAG, "setMaximumVideoQuality() called with: quality = $quality")
        val videoTracks = videoTracks.value
//        if (videoTracks.all { track -> track.height != null }) {
//            // VOD: 해상도 정보가 모두 존재
//            player?.apply {
//                trackSelectionParameters = trackSelectionParameters
//                    .buildUpon()
//                    .setMaxVideoBitrate(Int.MAX_VALUE)
//                    .setMaxVideoSize(Int.MAX_VALUE, quality)
//                    .build()
//            }
//        } else {

//            PlayerLog.d("videoSize","videoTrack : ${videoTracks}")
        // LIVE: 해상도 정보 미제공 -> 화질로 품질 추정
        val bitrateMap = TreeMap<Int, Int>()
        videoTracks.forEach { track ->
            bitrateMap[track.estimatedHeightFromBitrate] = track.bitrate ?: Int.MAX_VALUE
        }
        val bitrate = bitrateMap.ceilingEntry(quality)?.value ?: Int.MAX_VALUE
        player?.apply {
            trackSelectionParameters =
                trackSelectionParameters
                    .buildUpon()
                    .clearVideoSizeConstraints()
                    .setMaxVideoBitrate(bitrate)
                    .build()
        }
        configurations(context) {
            playConfiguration(
                bufferForPlaybackAfterRebufferMs = bufferForPlaybackAfterRebufferMs,
                bufferForPlaybackMs = bufferForPlaybackAfterRebufferMs,
                maxBufferMs = maxBufferMs,
                minBufferMs = minBufferMs,
                maximumVideoQuality = quality,
                seekBackIncrementMs = _seekBackIncrement.value,
                seekForwardIncrementMs = _seekForWardIncrement.value,
            )
        }
    }

    override fun setShuffleOn(isShuffle: Boolean) {
        player?.shuffleModeEnabled = isShuffle
        _isShuffleOn.update { isShuffle }
    }

    override fun setRepeat(repeatMode: RepeatMode) {
        player?.repeatMode = repeatMode.ordinal
        _repeatMode.update { repeatMode }
    }

    override fun setPlaySpeed(playSpeed: PlaySpeed) {
        player?.setPlaybackSpeed(playSpeed.speed)
        _playSpeed.update { playSpeed }
    }

    override fun setVolume(volume: Float) {
        player?.volume = volume
    }

    override fun onDeviceVolumeChanged(
        volume: Int,
        muted: Boolean,
    ) {
        super.onDeviceVolumeChanged(volume, muted)

        RLog.d("PLAYER", "volume : $volume ,muted : $muted")

        // volume: 현재 시스템 볼륨 (정수 0~15)
        // 시스템 볼륨이 위로(+) 눌렸는지 확인
        val isVolumeUp = volume > getLastSystemVolume()

        // 핵심 조건: "앱이 뮤트인데 시스템 볼륨을 키웠는가?"
        if ((player?.volume ?: 0f) <= 0f && isVolumeUp) {
            val maxVol = player?.deviceVolume?.toFloat() ?: 15f
            val newAppVolume = volume / maxVol

            // 소수점 한 자리로 예쁘게 깎아서 앱 볼륨에 강제 주입!
            val rounded = (newAppVolume * 10).roundToInt() / 10.0f
            player?.volume = rounded

            _volume.update { rounded }
        }

        setMute(muted)

        // 시스템 볼륨값 업데이트 (다음 비교를 위해)
        _lastSystemVolume.update { volume.toFloat() }
    }

    override fun pause() {
        player?.takeIf { it.playWhenReady }?.apply {
            playWhenReady = false
            _playbackState.update { PlaybackState.Pause(playerIndex) }
        } ?: return
    }

    override fun stop() {
        player?.takeIf { it.playWhenReady }?.apply {
            stop()
            _playbackState.update { PlaybackState.Stop(playerIndex) }
        } ?: return
    }

    override fun seekTo(msec: Long) {
        player?.seekTo(msec)
    }

    override fun seekTo(
        mediaIndex: Int,
        msec: Long,
    ) {
        player?.seekTo(mediaIndex, msec)
    }

    override fun isPlaying(): Boolean {
        RLog.d("Player", "playbackState : ${player?.playbackState} , playWhenReady : ${player?.playWhenReady}")
        return player != null &&
            player?.playbackState == ExoPlayer.STATE_READY &&
            player?.playWhenReady == true
    }

    override fun isPlayComplete(): Boolean =
        player != null &&
            player?.playbackState == ExoPlayer.STATE_ENDED

    override fun getBufferedPosition(): Long = player?.bufferedPosition ?: 0

    override fun release() {
        RLog.d("MediaScreen", "release() player : $player")
        _playbackState.update { PlaybackState.Release(playerIndex) }
        clearVideoSurface()
        player?.clearMediaItems()
        player
            ?.also {
                it.removeListener(this)
                it.release()
            }.also { player = null }
        player = null
        RLog.d("MediaScreen", "release() release")
        _playbackState.update { PlaybackState.Idle(playerIndex) }
        // todo 캔슬되면 다시 무조건 생성해야함
        playerInternalScope.cancel()
    }

    override fun setVideoSurface(surface: Surface?) {
        player?.setVideoSurface(surface)
    }

    override fun setVideoSurfaceView(surface: SurfaceView?) {
        player?.setVideoSurfaceView(surface)
    }

    @Deprecated("Deprecated in Java")
    override fun onPlayerStateChanged(
        playWhenReady: Boolean,
        playbackState: Int,
    ) {
        RLog.d("MediaScreen", "onPlayerStateChanged playbackState : $playbackState playWhenReady : $playWhenReady")
        when (playbackState) {
            Player.STATE_IDLE -> {
                _playbackState.update { PlaybackState.Idle(playerIndex) }
            }

            Player.STATE_BUFFERING -> {
                if (playWhenReady) _playbackState.update { PlaybackState.Buffering(playerIndex) }
            }

            Player.STATE_READY -> {
                if (playWhenReady) {
                    if (isPreparing) {
                        _playbackState.update { PlaybackState.Prepared(this, playerIndex) }
                        isPreparing = false
                    }
                    _playbackState.update { PlaybackState.Playing(this, playerIndex) }
                    player?.duration?.let {
                        if (it > 0) {
                            _duration.update { player?.duration }
                        }
                    }

                    startPositionUpdates(playerInternalScope)
                } else {
                    _playbackState.update { PlaybackState.Pause(playerIndex) }
                    stopPositionUpdates()
                }
            }

            Player.STATE_ENDED -> {
                if (playWhenReady) _playbackState.update { PlaybackState.Complete(playerIndex) }
                stopPositionUpdates()
            }
        }
    }

    override fun onTimelineChanged(
        timeline: Timeline,
        reason: Int,
    ) {
        if (timeline.isEmpty) return
        val playerIndex = player?.currentMediaItemIndex ?: 0

        val window = Timeline.Window()
        timeline.getWindow(playerIndex, window)

        val duration = window.durationMs

        if (duration > 0 && duration != C.TIME_UNSET) {
            _duration.update { duration }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.e("MediaScreen", "cause : ${error.cause}")
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> {
                // restart play
                val uri = player?.currentMediaItem?.localConfiguration?.uri
                if (uri == null) {
                    dispatchError(error.errorCode, error.cause)
                } else {
                    // todo error  처리
                    val prevVolume = _isMute.value
                    // start(uri, null)
                    _isMute.update { prevVolume }
                }
            }

            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                dispatchError(error.errorCode, error.cause)
            }

            else -> {
                dispatchError(error.errorCode, error.cause)
            }
        }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) =
        _resolution.update {
            Resolution(
                videoSize.width,
                videoSize.height,
                videoSize.pixelWidthHeightRatio,
            )
        }

    override fun onTracksChanged(tracks: Tracks) {
        RLog.d(TAG, "onTracksChanged() tracks = $tracks")
        val videoTracks = mutableListOf<VideoTrack>()
        val audioTracks = mutableListOf<AudioTrack>()
        tracks.groups.forEachIndexed { _, group ->
            when (group.type) {
                C.TRACK_TYPE_VIDEO -> {
                    videoTracks.addAll(getVideoTracks(group))
                }

                C.TRACK_TYPE_AUDIO -> {
                    audioTracks.addAll(getAudioTracks(group))
                }
            }
        }
        _videoTracks.update { videoTracks }
        _audioTracks.update { audioTracks }
    }

    override fun onMediaItemTransition(
        mediaItem: MediaItem?,
        reason: Int,
    ) {
        _currentIndex.value = player?.currentMediaItemIndex ?: 0
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
            Log.d("PLAYER", "이전 아이템 재생 완료 후 자동 전환됨")

            val duration = player?.duration ?: 0L

            if (duration > 0) {
                _duration.update { duration }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(100) // 50ms 딜레이
                    val duration = player?.duration ?: 0L
                    _duration.update { duration }
                }
            }

            _playbackState.update {
                PlaybackState.MediaTransition(
                    mediaItem,
                    MediaStreamTransitionReason.convertToValueToEnum(reason),
                )
            }
        }
    }

    private fun startPositionUpdates(scope: CoroutineScope) {
        positionJob?.cancel()

        // despose ->resume 일때 release 후에 재생성
        // 플레이어가 null 이 되면서 activie ->false 로 재생성
        if (!playerInternalScope.isActive) {
            playerInternalScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        }

        positionJob =
            playerInternalScope.launch {
                while (isActive) {
                    val p = player?.currentPosition ?: 0L
                    _currentPosition.value = p
                    delay(250)
                }
            }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    private fun getVideoTracks(group: Tracks.Group): List<VideoTrack> {
        val tracks = mutableListOf<VideoTrack>()
        for (i in 0 until group.length) {
            group.getTrackFormat(i).apply {
                val track =
                    VideoTrack(
                        id,
                        if (bitrate == Format.NO_VALUE) null else bitrate,
                        if (width == Format.NO_VALUE) null else width,
                        if (height == Format.NO_VALUE) null else height,
                        if (frameRate == Format.NO_VALUE.toFloat()) null else frameRate,
                        language,
                    )
                tracks.add(track)
            }
        }
        return tracks
    }

    private fun getAudioTracks(group: Tracks.Group): List<AudioTrack> {
        val tracks = mutableListOf<AudioTrack>()
        for (i in 0 until group.length) {
            group.getTrackFormat(i).apply {
                val track = AudioTrack(id, language, channelCount)
                tracks.add(track)
            }
        }
        return tracks
    }

    private fun initListener(player: ExoPlayer?) =
        player?.run {
            addListener(this@MediaExoStreamPlayer)
        }

    // 네트워크
    private fun getMediaSource(
        uri: Uri,
        cacheKey: String?,
    ): MediaSource =
        when (Util.inferContentType(uri)) {
            C.CONTENT_TYPE_HLS -> {
                HlsMediaSource
                    .Factory(getDefaultDatasource())
                    .setAllowChunklessPreparation(true)
                    .setLoadErrorHandlingPolicy(
                        DefaultLoadErrorHandlingPolicy(HLS_REQUEST_RETRY_COUNT),
                    ).createMediaSource(getMediaItem(uri, cacheKey))
            }

            else -> {
                ProgressiveMediaSource
                    .Factory(getDefaultDatasource())
                    .createMediaSource(MediaItem.Builder().setUri(uri).build())
            }
        }

    private fun getMediaItem(
        uri: Uri,
        cacheKey: String?,
    ): MediaItem =
        MediaItem
            .Builder()
            .setCustomCacheKey(cacheKey ?: "")
            .setUri(uri)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()

    fun buildMediaItems(
        uris: List<Uri>,
        cacheKey: String?,
    ): List<MediaItem> =
        uris.map { uri ->
            MediaItem
                .Builder()
                .setCustomCacheKey(cacheKey ?: "")
                .setUri(uri)
                .setMimeType(MimeTypes.VIDEO_MP4)
                .build()
        }

//    fun getMediaItem(
//        items: List<Uri>
//    ): List<MediaItem> {
//
//        val mediaSources = items.map { uri ->
//            val uri = uri
//            val isLocal =
//                uri.scheme == ContentResolver.SCHEME_CONTENT ||
//                        uri.scheme == ContentResolver.SCHEME_FILE ||
//                        uri.scheme == null
//
//            if (isLocal) {
//                ProgressiveMediaSource.Factory(
//                    DefaultDataSource.Factory(context)
//                ).createMediaSource(MediaItem.fromUri(uri))
//            } else {
//                when (Util.inferContentType(uri)) {
//                    C.CONTENT_TYPE_HLS -> {
//                        HlsMediaSource.Factory(getDefaultDatasource())
//                            .setAllowChunklessPreparation(true)
//                            .setLoadErrorHandlingPolicy(
//                                DefaultLoadErrorHandlingPolicy(HLS_REQUEST_RETRY_COUNT)
//                            ).createMediaSource(getMediaItem(uri, null))
//                    }
//
//                    else ->
//                        ProgressiveMediaSource.Factory(getDefaultDatasource())
//                            .createMediaSource(MediaItem.fromUri(uri))
//                }
//            }
//        }
//
//        return mediaSources
//    }
    @OptIn(UnstableApi::class)
    private fun createPlayer(): ExoPlayer? {
        val videoTrackSelectionFactory =
            AdaptiveTrackSelection.Factory(
                TARGET_DURATION_MS,
                (TARGET_DURATION_MS * QUALITY_DECREASE_SCALE).toInt(),
                (TARGET_DURATION_MS * QUALITY_DECREASE_SCALE).toInt(),
                AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION,
            )
        val renderersFactory = DefaultRenderersFactory(context)
        renderersFactory.setEnableDecoderFallback(true) // 소프트웨어 코덱 사용

        val trackSelector = DefaultTrackSelector(context, videoTrackSelectionFactory)
        val loadControlBuilder =
            DefaultLoadControl
                .Builder()
                .setAllocator(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
                .setBufferDurationsMs(
                    minBufferMs,
                    maxBufferMs,
                    bufferForPlaybackMs,
                    bufferForPlaybackAfterRebufferMs,
                ).build()

        val mediaDatsourceFactory = DefaultMediaSourceFactory(context).setDataSourceFactory(getDefaultDatasource())
        Log.d("MediaScreen", "datasourceFactory : $datasourceFactory")
        return ExoPlayer
            .Builder(context)
            .setMediaSourceFactory(mediaDatsourceFactory)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(getBandwidthMeter())
            .setLoadControl(loadControlBuilder)
            .setSeekBackIncrementMs(_seekBackIncrement.value ?: SEEK_BACK_INCREMENTS_MS)
            .setSeekForwardIncrementMs(_seekForWardIncrement.value ?: SEEK_BACK_INCREMENTS_MS)
            .setRenderersFactory(renderersFactory)
            .setDeviceVolumeControlEnabled(true) // 디바인스 볼륨 전달
            .build()
    }

    override fun getPlayer(): ExoPlayer? = player

    private fun getBandwidthMeter(): DefaultBandwidthMeter =

        defaultBandwidthMeter.apply {
            addEventListener(
                Handler(Looper.getMainLooper()),
                bandwidthMeterEventListener,
            )
        }

    private fun getDefaultDatasource(): DataSource.Factory = datasourceFactory

    private fun getLastSystemVolume(): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    private val bandwidthMeterEventListener =
        BandwidthMeter.EventListener { elapsedMs: Int, bytes: Long, bitrate: Long ->
            _sampleBandWidth.update { SampleBandWidth(elapsedMs, bytes, bitrate) }
        }

    private fun dispatchError(
        errorCode: Int,
        cause: Throwable?,
    ) {
        _playbackErrorState.update { PlaybackState.Error(errorCode, cause, playerIndex) }
    }

    private fun mutePlay(muteInfo: PlayMuteInfo) {
        val (playIndex, isMute) = muteInfo
        if (playerIndex == playIndex) {
            player?.also {
                it.volume = if (isMute) 0f else 1f
            }
        }
    }

    enum class MediaType {
        AUDIO,
        VIDEO,
        UNKNOWN,
    }

    fun detectMediaTypeFromMediaItem(
        context: Context,
        mediaItem: MediaItem,
    ): MediaType {
        val uri =
            mediaItem.localConfiguration?.uri
                ?: return MediaType.UNKNOWN

        if (uri.scheme == ContentResolver.SCHEME_FILE) {
            return detectByMetadataRetriever(uri)
        }

        return detectByExtractor(context, uri)
    }

    fun detectByExtractor(
        context: Context,
        uri: Uri,
    ): MediaType {
        val dataSourceFactory =
            DefaultDataSourceFactory(context)

        val trackTypes =
            detectMediaType(
                dataSourceFactory,
                uri,
            )

        return when {
            trackTypes.contains(C.TRACK_TYPE_VIDEO) -> MediaType.VIDEO
            trackTypes.contains(C.TRACK_TYPE_AUDIO) -> MediaType.AUDIO
            else -> MediaType.UNKNOWN
        }
    }

    private fun detectMediaType(
        dataSourceFactory: DataSource.Factory,
        uri: Uri,
    ): Set<Int> {
        // C.TRACK_TYPE_UNKNOWN = -1
        // C.TRACK_TYPE_DEFAULT = 0
        // C.TRACK_TYPE_AUDIO   = 1
        // C.TRACK_TYPE_VIDEO   = 2
        // C.TRACK_TYPE_TEXT    = 3
        // C.TRACK_TYPE_METADATA = 4

        val extractors = DefaultExtractorsFactory().createExtractors()
        val dataSource = dataSourceFactory.createDataSource()

        val dataSpec = DataSpec(uri)
        val length = dataSource.open(dataSpec)

        val input =
            DefaultExtractorInput(
                dataSource,
                0,
                length,
            )

        val trackTypes = mutableSetOf<Int>()

        for (extractor in extractors) {
            try {
                if (!extractor.sniff(input)) {
                    input.resetPeekPosition()
                    continue
                }

                extractor.init(
                    object : ExtractorOutput {
                        override fun track(
                            id: Int,
                            type: Int,
                        ): TrackOutput {
                            trackTypes.add(type)
                            return DummyTrackOutput()
                        }

                        override fun endTracks() {}

                        override fun seekMap(seekMap: SeekMap) {}
                    },
                )

                // 🔥 이게 핵심: read()를 실제로 호출
                val positionHolder = PositionHolder()
                repeat(5) {
                    // 몇 프레임만 읽어도 충분
                    extractor.read(input, positionHolder)
                }

                break
            } catch (e: Exception) {
                input.resetPeekPosition()
            }
        }
        dataSource.close()
        return trackTypes
    }

    fun detectByMetadataRetriever(uri: Uri): MediaType {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(uri.path!!)
            val hasVideo =
                retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO,
                )
            if (hasVideo == "yes") MediaType.VIDEO else MediaType.AUDIO
        } catch (e: Exception) {
            MediaType.UNKNOWN
        } finally {
            retriever.release()
        }
    }

    override fun getVideoCapture(
        view: View,
        infoCallback: (Bitmap?) -> Unit,
    ) {
        when {
            view is SurfaceView -> captureSurfaceView(view, infoCallback)
            else -> Unit
        }
    }

    private fun captureSurfaceView(
        surfaceView: SurfaceView,
        infoCallback: (Bitmap?) -> Unit,
    ) {
        // 1. 빈 비트맵 생성 (SurfaceView 크기만큼)
        if (surfaceView.width > 0 && surfaceView.height > 0) {
            val bitmap = createBitmap(surfaceView.width, surfaceView.height)

            // 2. PixelCopy 실행 (SurfaceView 내용을 비트맵으로 복사)
            try {
                if (!surfaceView.holder.surface.isValid) {
                    Log.e("hbungshin", "Surface가 유효하지 않아! 캡처 불가")
                    return
                }
                PixelCopy.request(
                    surfaceView,
                    bitmap,
                    { result ->
                        if (result == PixelCopy.SUCCESS) {
                            infoCallback(bitmap)
                        } else {
                            infoCallback(null)
                        }
                    },
                    Handler(Looper.getMainLooper()),
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            infoCallback(null)
        }
    }

    companion object {
        private const val TAG = "RExoPlayer"
        private const val TARGET_DURATION_MS = 2000
        private const val QUALITY_DECREASE_SCALE = 2.5f
        private const val HLS_REQUEST_RETRY_COUNT = 3
        private const val MIN_BUFFER_PLAYBACK_TIME = 2_000
        private const val MAX_BUFFER_PLAYBACK_TIME = 24_0000
        private const val BUFFER_FOR_PLAYBACK_TIME = 2_000
        private const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER = 2_000
        private const val MAX_VIDEO_QUALITY = Int.MAX_VALUE
        private const val SEEK_BACK_INCREMENTS_MS = 5_000L
        private const val SEEK_FOWARD_INCREMENTS_MS = 5_000L
    }
}
