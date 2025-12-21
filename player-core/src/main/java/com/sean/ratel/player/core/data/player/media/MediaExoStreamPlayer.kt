package com.sean.ratel.player.core.data.player.media

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import androidx.annotation.OptIn
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
import com.sean.ratel.player.core.Configurations
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.PlaySpeed
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.RepeatMode
import com.sean.ratel.player.core.configurations
import com.sean.ratel.player.core.data.domain.MediaStreamPlayer
import com.sean.ratel.player.core.data.domain.model.PlayMuteInfo
import com.sean.ratel.player.core.data.domain.model.PlaybackState
import com.sean.ratel.player.core.data.domain.model.Resolution
import com.sean.ratel.player.core.data.domain.model.SampleBandWidth
import com.sean.ratel.player.core.data.domain.model.track.AudioTrack
import com.sean.ratel.player.core.data.domain.model.track.VideoTrack
import com.sean.ratel.player.utils.log.RLog
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
import java.util.TreeMap

@UnstableApi
class MediaExoStreamPlayer(
    private val context: Context,
    private val datasourceFactory:DataSource.Factory,
   // userAgentProvider: UserAgentProvider
):MediaStreamPlayer,
  Player.Listener{

    private var player: ExoPlayer? = null
    private var bufferForPlaybackMs: Int = BUFFER_FOR_PLAYBACK_TIME
    private var bufferForPlaybackAfterRebufferMs: Int = BUFFER_FOR_PLAYBACK_AFTER_REBUFFER
    private var maxBufferMs: Int = MAX_BUFFER_PLAYBACK_TIME
    private var minBufferMs: Int = MIN_BUFFER_PLAYBACK_TIME
    private var isPreparing = false
    private var playerIndex: Int? = null
    private var userAgent: String = "exoplayer"//userAgentProvider.userAgent

    private val defaultBandwidthMeter =
        DefaultBandwidthMeter.Builder(context)
            .setResetOnNetworkTypeChange(true)
            /** Network changes invalidate existing data **/
            // .setInitialBitrateEstimate(Integer.MAX_VALUE.toLong()) // later tuning
            .build()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle(playerIndex))
    override val playbackState: Flow<PlaybackState> = _playbackState

    private  val _isMute = MutableStateFlow(PlayMuteInfo(0,false))
    override val isMute: StateFlow<PlayMuteInfo> = _isMute

    private val _maximumVideoQuality =  MutableStateFlow<Int>(Int.MAX_VALUE)
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


    private val _duration = MutableStateFlow<Long?>(null)
    override val  duration: StateFlow<Long?> = _duration


    private val _isShuffleOn = MutableStateFlow<Boolean>(false)
    override val isShuffleOn: StateFlow<Boolean> = _isShuffleOn


    private val _currentPosition= MutableStateFlow<Long?>(null)

    override val  currentPosition:StateFlow<Long?> = _currentPosition

    private val _repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.REPEAT_OFF)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private var positionJob: Job? = null
    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    fun startPositionUpdates(scope: CoroutineScope) {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                val p = player?.currentPosition ?: 0L
                _currentPosition.value = p
                delay(250) // 200~500ms 보통
            }
        }
    }

    fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    private val _resolution = MutableStateFlow(
        Resolution(
            1080,
            720,
            1F
        )
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

    override fun start(uri: Uri, cacheKey: String?) {

        player?.let { release() }

        _playbackState.update { PlaybackState.Idle(playerIndex) }


        player = createPlayer()?.apply {
            initListener(this)
            isPreparing = true
            _playbackState.update {
                PlaybackState.Preparing(playerIndex)
            }
            setMediaSource(getMediaSource(uri, cacheKey))
            prepare()

            mutePlay(_isMute.value)
            setShuffleOn(_isShuffleOn.value)
            setRepeat(_repeatMode.value)

            playWhenReady = true
        }

        Log.d(
            "KKKKKKKK",
            "play = ${uri} userAgent : $userAgent ,  player : $player , cacheKey : ${cacheKey}"
        )

    }


    override fun start(
        items: List<MediaItem>,
        startIndex:Int,
        cacheKey: String?
    ) {

        player?.release()

        _playbackState.update { PlaybackState.Idle()}

        player = createPlayer()?.apply {
            initListener(this)

            isPreparing = true
            _playbackState.update {
                PlaybackState.Preparing()
            }

            setMediaItems(items,startIndex,0)

            prepare()
            mutePlay(_isMute.value)

            playWhenReady = true
        }

    }
    override fun start(uri: Uri) {

        start(uri,null)
    }

    override fun isPrevItem(): Boolean
        = player?.hasPreviousMediaItem() ?: false


    override fun pervPlay() {
        player?.seekToPreviousMediaItem()
    }

    override fun isNextItem(): Boolean
        = player?.hasNextMediaItem() ?: false


    override fun nextPlay() {
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
            ExoPlayer.STATE_BUFFERING -> if (player.playWhenReady) _playbackState.update {
                PlaybackState.Buffering(
                    playerIndex
                )
            }

            ExoPlayer.STATE_READY -> _playbackState.update {
                PlaybackState.Playing(
                    player = this,
                    playerIndex
                )
            }

            else -> Unit
        }
    }
    override fun clearVideoSurface() = setVideoSurfaceView(null)

    override fun setMute(mute: Boolean) {
        _isMute.update { PlayMuteInfo(0, mute) }
    }
    override fun setMute(playIndex: Int, mute: Boolean) {
        _isMute.update { PlayMuteInfo(playIndex, mute) }
    }
    override fun setMaximumVideoQuality(quality: Int, isUserSelect: Boolean) {
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
            trackSelectionParameters = trackSelectionParameters
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
                seekForwardIncrementMs = _seekForWardIncrement.value
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

    override fun isPlaying(): Boolean {
        Log.d("KKKKKKKK","playbackState : ${player?.playbackState} , playWhenReady : ${player?.playWhenReady}")
        return player != null &&
                player?.playbackState == ExoPlayer.STATE_READY &&
                player?.playWhenReady == true
    }

    override fun isPlayComplete(): Boolean = player != null &&
            player?.playbackState == ExoPlayer.STATE_ENDED




    override fun getBufferedPosition(): Long = player?.bufferedPosition ?: 0

    override fun release() {
        Log.d("hbungshin","release() player : $player")
        _playbackState.update { PlaybackState.Release(playerIndex) }
        player?.also {
            it.removeListener(this)
            it.release()
        }.also { player = null }
        player = null
        _playbackState.update { PlaybackState.Idle(playerIndex) }
        playerScope.cancel()
    }

    override fun setVideoSurface(surface: Surface?) {
        player?.setVideoSurface(surface)
    }

    override fun setVideoSurfaceView(surface: SurfaceView?) {
        player?.setVideoSurfaceView(surface)
    }
    @Deprecated("Deprecated in Java")
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Log.d("KKKKKKKK","onPlayerStateChanged playbackState : $playbackState playWhenReady : ${playWhenReady}")
        when (playbackState) {
            Player.STATE_IDLE -> _playbackState.update { PlaybackState.Idle(playerIndex) }
            Player.STATE_BUFFERING -> if (playWhenReady) _playbackState.update { PlaybackState.Buffering(playerIndex) }
            Player.STATE_READY -> {
                if (playWhenReady) {
                    if (isPreparing) {
                        _playbackState.update { PlaybackState.Prepared(this,playerIndex) }
                        isPreparing = false
                    }
                    _playbackState.update { PlaybackState.Playing(this,playerIndex) }
                    player?.duration?.let {
                        if (it > 0)
                            _duration.update { player?.duration }
                    }

                    startPositionUpdates(playerScope)
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
    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (timeline.isEmpty) return
        val playerIndex = player?.currentMediaItemIndex?:0

        val window = Timeline.Window()
        timeline.getWindow(playerIndex, window)

        val duration = window.durationMs

        if (duration > 0 && duration != C.TIME_UNSET) {
            _duration.update{duration}
            Log.d("hbungshin","1111 duration : $duration")
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        RLog.e(TAG,"cause : ${error.cause}")
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> {
                // restart play
                val uri = player?.currentMediaItem?.localConfiguration?.uri
                if (uri == null) {
                    dispatchError(error.errorCode, error.cause)
                } else {
                    //todo error  처리
                    val prevVolume = _isMute.value
                    start(uri, null)
                    _isMute.update { prevVolume }
                }
            }

            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> dispatchError(error.errorCode, error.cause)

            else -> {
                dispatchError(error.errorCode, error.cause)
            }
        }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) = _resolution.update {
        Resolution(
            videoSize.width,
            videoSize.height,
            videoSize.pixelWidthHeightRatio
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
    private fun getVideoTracks(group: Tracks.Group): List<VideoTrack> {
        val tracks = mutableListOf<VideoTrack>()
        for (i in 0 until group.length) {
            group.getTrackFormat(i).apply {
                val track = VideoTrack(
                    id,
                    if (bitrate == Format.NO_VALUE) null else bitrate,
                    if (width == Format.NO_VALUE) null else width,
                    if (height == Format.NO_VALUE) null else height,
                    if (frameRate == Format.NO_VALUE.toFloat()) null else frameRate,
                    language
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

    private fun getMediaSource(uri: Uri,cacheKey:String?): MediaSource =
        when (Util.inferContentType(uri)) {
            C.CONTENT_TYPE_HLS -> {
                HlsMediaSource.Factory(getDefaultDatasource()).setAllowChunklessPreparation(true)
                    .setLoadErrorHandlingPolicy(
                        DefaultLoadErrorHandlingPolicy(HLS_REQUEST_RETRY_COUNT)
                    ).createMediaSource(getMediaItem(uri,cacheKey))
            }

            else -> ProgressiveMediaSource.Factory(getDefaultDatasource())
                .createMediaSource(MediaItem.Builder().setUri(uri).build())
        }


    private fun getMediaItem(uri: Uri,cacheKey:String?): MediaItem {
        return MediaItem.Builder()
            .setCustomCacheKey(cacheKey?:"")
            .setUri(uri)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()
    }
    @OptIn(UnstableApi::class)
    private fun createPlayer(): ExoPlayer? {
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(
            TARGET_DURATION_MS,
            (TARGET_DURATION_MS * QUALITY_DECREASE_SCALE).toInt(),
            (TARGET_DURATION_MS * QUALITY_DECREASE_SCALE).toInt(),
            AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION
        )
        val renderersFactory = DefaultRenderersFactory(context)
        renderersFactory.setEnableDecoderFallback(true) // 소프트웨어 코덱 사용

        val trackSelector = DefaultTrackSelector(context, videoTrackSelectionFactory)
        val loadControlBuilder = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
            .setBufferDurationsMs(
                minBufferMs,
                maxBufferMs,
                bufferForPlaybackMs,
                bufferForPlaybackAfterRebufferMs
            ).
            build()

        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(getDefaultDatasource()))
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(getBandwidthMeter())
            .setLoadControl(loadControlBuilder)
            .setSeekBackIncrementMs(_seekBackIncrement.value?:SEEK_BACK_INCREMENTS_MS)
            .setSeekForwardIncrementMs(_seekForWardIncrement.value?:SEEK_BACK_INCREMENTS_MS)

            .setRenderersFactory(renderersFactory)
            .build()
    }

    override fun getPlayer(): ExoPlayer? {
        return player
    }


    private fun getBandwidthMeter(): DefaultBandwidthMeter =

        defaultBandwidthMeter.apply {
            addEventListener(
                Handler(Looper.getMainLooper()),
                bandwidthMeterEventListener
            )
        }

    private fun getDefaultDatasource(): DataSource.Factory

        = datasourceFactory

       // DefaultHttpDataSource.Factory().setUserAgent(userAgent)

    private val bandwidthMeterEventListener =
        BandwidthMeter.EventListener { elapsedMs: Int, bytes: Long, bitrate: Long ->
            //TLog.d(TAG, "elapsedMs : $elapsedMs , bytes : $bytes , bitrate : $bitrate")
            _sampleBandWidth.update { SampleBandWidth(elapsedMs, bytes, bitrate) }
        }

    private fun dispatchError(errorCode: Int, cause: Throwable?) {
        _playbackState.update { PlaybackState.Error(errorCode, cause,playerIndex) }
    }

    private fun mutePlay(muteInfo: PlayMuteInfo) {
        val (playIndex, isMute) = muteInfo
        if (playerIndex == playIndex) {
            player?.also {
                it.volume = if (isMute) 0f else 1f
            }
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