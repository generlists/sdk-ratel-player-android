package com.sean.ratel.player.core.data.player.youtube

import android.annotation.SuppressLint
import android.util.Size
import android.view.View
import androidx.lifecycle.Lifecycle
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlayQuality
import com.sean.ratel.player.core.com.sean.ratel.player.core.util.PlayerUtil.getConvertPlaybackRateToYouTubePlaybackRate
import com.sean.ratel.player.core.com.sean.ratel.player.core.util.PlayerUtil.getConvertPlayerErrorToYouTubeStreamPlayerError
import com.sean.ratel.player.core.com.sean.ratel.player.core.util.PlayerUtil.getConvertYouTubePlaybackRatYoPlaybackRate
import com.sean.ratel.player.core.data.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlayerError
import com.sean.ratel.player.core.data.player.youtube.adaptor.YouTubeStreamPlayerAdapter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import so.smartlab.common.utils.log.RLog

class YouTubeStreamPlayerImpl(
    private val lifecycle: Lifecycle,
    private val autoPlay: Boolean,
    private val youtubeStreamPlayerAdapter: YouTubeStreamPlayerAdapter,
    private val iFramePlayerOptions: IFramePlayerOptions,
    private val youtubeStreamPlayerTracker: YouTubePlayerTracker? = null,
) : YouTubePlayerListener,
    FullscreenListener,
    YouTubeStreamPlayer {
    private var youTubeStreamPlayer: YouTubePlayer? = null
    private var initialPlayer: Boolean = false
    private var videoId: String? = null

    private val _playbackState =
        MutableStateFlow<YouTubeStreamPlaybackState>(
            YouTubeStreamPlaybackState.UnKnown,
        )
    override val playbackState: StateFlow<YouTubeStreamPlaybackState> = _playbackState.asStateFlow()

    private val _playbackError = MutableSharedFlow<YouTubeStreamPlayerError>(replay = 1)
    override val playbackError: SharedFlow<YouTubeStreamPlayerError> = _playbackError.asSharedFlow()

    private val _isMute = MutableStateFlow(true)
    val isMute: StateFlow<Boolean> = _isMute.asStateFlow()

    private val _duration = MutableStateFlow(0f)
    override val duration: StateFlow<Float> = _duration.asStateFlow()

    private val _currentTime = MutableStateFlow(0f)
    override val currentTime: StateFlow<Float> = _currentTime.asStateFlow()

    override fun getYouTubePlayerView(): View = youtubeStreamPlayerAdapter.getYouTubePlayerView()

    private val _videoSpeedChange = MutableStateFlow<YouTubeStreamPlaybackRate?>(null)
    override val videoSpeedChange: StateFlow<YouTubeStreamPlaybackRate?> =
        _videoSpeedChange.asStateFlow()

    private val _videoQualityLevel = MutableStateFlow<List<YouTubeStreamPlayQuality>>(emptyList())
    override val videoQualityLevel: StateFlow<List<YouTubeStreamPlayQuality>> =
        _videoQualityLevel.asStateFlow()

    private val _videoQualityChange =
        MutableStateFlow<YouTubeStreamPlayQuality>(YouTubeStreamPlayQuality.Auto)
    override val videoQualityChange: StateFlow<YouTubeStreamPlayQuality> =
        _videoQualityChange.asStateFlow()

    private val _fullScreenView = MutableStateFlow<View?>(null)
    override val fullScreenView: StateFlow<View?> = _fullScreenView.asStateFlow()

    private val _exitFullScreen = MutableStateFlow<Boolean>(false)
    override val exitFullScreen: StateFlow<Boolean> = _exitFullScreen.asStateFlow()

    private val _captionAvailable = MutableStateFlow<Boolean>(true)
    override val captionAvailable: StateFlow<Boolean> =
        _captionAvailable.asStateFlow()

    override fun initPlayer(
        networkHandle: Boolean?,
        videoId: String?,
    ) {
        RLog.d(TAG, "videoId : $videoId")

        if (initialPlayer) return

        youtubeStreamPlayerAdapter.initialize(
            this,
            networkHandle ?: false,
            iFramePlayerOptions,
            videoId,
        )
        this.initialPlayer = true
        this.videoId = videoId
        RLog.d(TAG, "[LIB] initialPlayer : $initialPlayer")
    }

    @SuppressLint("RestrictedApi")
    override fun loadOrCueVideo(
        videoId: String,
        startTime: Float?,
    ) {
        youTubeStreamPlayer?.loadOrCueVideo(
            lifecycle,
            videoId,
            startTime ?: 0f,
        )
    }

    override fun loadVideo(
        videoId: String,
        startTime: Float?,
    ) {
        youTubeStreamPlayer?.loadVideo(videoId, startTime ?: 0f)
    }

    override fun cueVideo(
        videoId: String,
        startTime: Float?,
    ) {
        youTubeStreamPlayer?.cueVideo(videoId, startTime ?: 0f)
    }

    override fun start() {
        youTubeStreamPlayer?.let { player ->
//            if (youtubeStreamPlayerTracker.state == PlayerState.PLAYING) player.pause()
//            else player.play()
            player.play()
            _playbackState.update { YouTubeStreamPlaybackState.Playing }
        }
    }

    override fun seekTo(msec: Float) {
        youTubeStreamPlayer?.seekTo(msec)
    }

    override fun resume() {}

    override fun pause() {
        youTubeStreamPlayer?.pause()
        _playbackState.update { YouTubeStreamPlaybackState.Paused }
    }

    override fun stop() {}

    override fun isPlaying(): Boolean = _playbackState.value is YouTubeStreamPlaybackState.Playing

    override fun release() {
        youtubeStreamPlayerAdapter.release()
        _playbackState.update { YouTubeStreamPlaybackState.RELEASE }
    }

    override fun setMute(mute: Boolean) {
        if (mute) youTubeStreamPlayer?.mute() else youTubeStreamPlayer?.unMute()

        _isMute.update { mute }
    }

    override fun setPlaybackRate(playbackRate: YouTubeStreamPlaybackRate) {
        RLog.d(TAG, "[LIB] $playbackRate , speed : ${getConvertYouTubePlaybackRatYoPlaybackRate(playbackRate)}",
        )
        youTubeStreamPlayer?.setPlaybackRate(getConvertYouTubePlaybackRatYoPlaybackRate(playbackRate))
    }

    override fun getVideoSize(): Size? =
        if (_playbackState.value == YouTubeStreamPlaybackState.Playing ||
            _playbackState.value == YouTubeStreamPlaybackState.Paused ||
            _playbackState.value == YouTubeStreamPlaybackState.Buffering
        ) {
            Size(
                youtubeStreamPlayerAdapter.getYouTubePlayerView().width,
                youtubeStreamPlayerAdapter.getYouTubePlayerView().height,
            )
        } else {
            null
        }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        //Log.d("CHROME", "[onReady] videoId : $videoId , youTubePlayer : $youTubePlayer , autoPlay : $autoPlay")
        youTubeStreamPlayer = youTubePlayer
        youTubeStreamPlayer?.addListener(this)

        if (autoPlay) videoId?.let { loadOrCueVideo(it, 0f) }

        _playbackState.update { YouTubeStreamPlaybackState.Prepared(this) }
    }

    override fun onStateChange(
        youTubePlayer: YouTubePlayer,
        state: PlayerState,
    ) {
        RLog.d(TAG, "[LIB], state : $state")
        _playbackState.update { (getConvertPlayerStateToYouTubeStreamPlaybackState(state)) }
    }

    override fun onPlaybackQualityChange(
        youTubePlayer: YouTubePlayer,
        playbackQuality: PlayerConstants.PlaybackQuality,
    ) {
        RLog.d(TAG, "[LIB] playbackQuality : $playbackQuality")

        when (playbackQuality) {
            PlayerConstants.PlaybackQuality.SMALL, PlayerConstants.PlaybackQuality.MEDIUM, PlayerConstants.PlaybackQuality.LARGE,
            -> {
                _videoQualityChange.update { YouTubeStreamPlayQuality.Auto }
            }

            PlayerConstants.PlaybackQuality.HD720 -> {
                _videoQualityChange.update { YouTubeStreamPlayQuality.P720 }
            }

            PlayerConstants.PlaybackQuality.HD1080 -> {
                _videoQualityChange.update { YouTubeStreamPlayQuality.P1080 }
            }

            else -> {
                _videoQualityChange.update { YouTubeStreamPlayQuality.Auto }
            }
        }
    }

    override fun onAvailableQualityLevels(
        youTubePlayer: YouTubePlayer,
        qualityLevels: List<PlayerConstants.PlaybackQuality>,
    ) {
        RLog.d(TAG, "[LIB] qualityLevels : $qualityLevels")

        val result = mutableListOf<YouTubeStreamPlayQuality>()

        if (qualityLevels.any {
                it == PlayerConstants.PlaybackQuality.DEFAULT ||
                    it == PlayerConstants.PlaybackQuality.SMALL ||
                    it == PlayerConstants.PlaybackQuality.MEDIUM ||
                    it == PlayerConstants.PlaybackQuality.LARGE ||
                    it == PlayerConstants.PlaybackQuality.UNKNOWN
            }

        ) {
            result.add(YouTubeStreamPlayQuality.Auto)
        }

        // 720 존재 여부

        if (qualityLevels.contains(PlayerConstants.PlaybackQuality.HD720)) {
            result.add(YouTubeStreamPlayQuality.P720)
        }

        // 1080 존재 여부

        if (qualityLevels.contains(PlayerConstants.PlaybackQuality.HD1080)) {
            result.add(YouTubeStreamPlayQuality.P1080)
        }
        _videoQualityLevel.update { result }
    }

    override fun onCaptionAvailable(
        youTubePlayer: YouTubePlayer,
        available: Boolean,
    ) {
        _captionAvailable.update { available }
    }

    override fun enableCaptions(languageCode: String) {
        youTubeStreamPlayer?.enableCaptions(languageCode)
    }

    override fun disableCaptions() {
        youTubeStreamPlayer?.disableCaptions()
    }

    override fun getChromeCastContext(): ChromecastYouTubePlayerContext? {
        return null
    }

    override fun chromeCastRelease() {}

    override fun endCurrentSession() {}
    override fun onCaptionAvailable() {}

    override fun onPlaybackRateChange(
        youTubePlayer: YouTubePlayer,
        playbackRate: PlaybackRate,
    ) {
        RLog.d(TAG, "[LIB] youTubePlayer : $youTubePlayer , $playbackRate")
        _videoSpeedChange.update { getConvertPlaybackRateToYouTubePlaybackRate(playbackRate) }
    }

    override fun onError(
        youTubePlayer: YouTubePlayer,
        error: PlayerConstants.PlayerError,
    ) {
        _playbackError.tryEmit(getConvertPlayerErrorToYouTubeStreamPlayerError(error))
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {
    }

    override fun onCurrentSecond(
        youTubePlayer: YouTubePlayer,
        second: Float,
    ) {
        _currentTime.update { second }
    }

    override fun onVideoDuration(
        youTubePlayer: YouTubePlayer,
        duration: Float,
    ) {
        _duration.update { duration }
    }

    override fun onVideoLoadedFraction(
        youTubePlayer: YouTubePlayer,
        loadedFraction: Float,
    ) {
    }

    override fun onVideoId(
        youTubePlayer: YouTubePlayer,
        videoId: String,
    ) {
    }

    private fun getConvertPlayerStateToYouTubeStreamPlaybackState(state: PlayerState): YouTubeStreamPlaybackState =
        when (state) {
            PlayerState.UNKNOWN -> {
                YouTubeStreamPlaybackState.UnKnown
            }

            PlayerState.UNSTARTED -> {
                YouTubeStreamPlaybackState.UnStarted
            }

            PlayerState.VIDEO_CUED -> {
                YouTubeStreamPlaybackState.Prepared(this)
            }

            PlayerState.PLAYING -> {
                setMute(true)
                YouTubeStreamPlaybackState.Playing
            }

            PlayerState.PAUSED -> {
                YouTubeStreamPlaybackState.Paused
            }

            PlayerState.BUFFERING -> {
                setMute(true)
                YouTubeStreamPlaybackState.Buffering
            }

            PlayerState.ENDED -> {
                YouTubeStreamPlaybackState.Ended
            }
        }


    override fun toggleFullscreen() {
        youTubeStreamPlayer?.toggleFullscreen()
    }

    override fun setLoop(loop: Boolean) {
        youTubeStreamPlayer?.setLoop(loop)
    }

    override fun setShuffle(shuffle: Boolean) {
        youTubeStreamPlayer?.setShuffle(shuffle)
    }

    override fun setQuality(quality: YouTubeStreamPlayQuality) {
        if (quality == YouTubeStreamPlayQuality.Auto) {
            youTubeStreamPlayer?.setQuality(PlayerConstants.PlaybackQuality.DEFAULT)
        } else if (quality == YouTubeStreamPlayQuality.P720) {
            youTubeStreamPlayer?.setQuality(PlayerConstants.PlaybackQuality.HD720)
        } else if (quality == YouTubeStreamPlayQuality.P1080) {
            youTubeStreamPlayer?.setQuality(PlayerConstants.PlaybackQuality.HD1080)
        }
    }

    override fun addFullscreenListener(): Boolean = youtubeStreamPlayerAdapter.addFullscreenListener(this)

    override fun removeFullscreenListener(): Boolean = youtubeStreamPlayerAdapter.removeFullscreenListener(this)

    override fun onEnterFullscreen(
        fullscreenView: View,
        exitFullscreen: () -> Unit,
    ) {
        _fullScreenView.update { fullscreenView }
        _exitFullScreen.update { false }
    }

    override fun onExitFullscreen() {
        _exitFullScreen.update { true }
    }
    companion object{

        private val TAG ="YouTubeStreamPlayerImpl"
    }

}
