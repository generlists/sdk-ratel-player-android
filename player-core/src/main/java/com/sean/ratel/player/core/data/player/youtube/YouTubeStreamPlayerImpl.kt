package com.sean.ratel.player.core.data.player.youtube

import android.annotation.SuppressLint
import android.util.Size
import android.view.View
import androidx.lifecycle.Lifecycle
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.sean.ratel.player.core.data.player.youtube.adaptor.YouTubeStreamPlayerAdapter
import com.sean.ratel.player.core.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlaybackRate
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlayerError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class YouTubeStreamPlayerImpl(
    private val lifecycle: Lifecycle,
    private val autoPlay:Boolean,
    private val youtubeStreamPlayerAdapter: YouTubeStreamPlayerAdapter,
    private val iFramePlayerOptions: IFramePlayerOptions,
    private val youtubeStreamPlayerTracker: YouTubePlayerTracker? = null,
) : YouTubePlayerListener,
    FullscreenListener,
    YouTubeStreamPlayer {
    private var youTubeStreamPlayer: YouTubePlayer? = null
    private var initialPlayer: Boolean = false
    private var videoId:String? = null

    private val _playbackState =
        MutableStateFlow<YouTubeStreamPlaybackState>(
            YouTubeStreamPlaybackState.UnKnown,
        )
    override val playbackState: StateFlow<YouTubeStreamPlaybackState> = _playbackState.asStateFlow()

    private val _playbackError = MutableSharedFlow<YouTubeStreamPlayerError>(replay = 1)
    override val playbackError: SharedFlow<YouTubeStreamPlayerError> = _playbackError.asSharedFlow()

    private val _isMute = MutableStateFlow(true)

    private val _duration = MutableStateFlow(0f)
    override val duration: StateFlow<Float> = _duration.asStateFlow()

    private val _currentTime = MutableStateFlow(0f)
    override val currentTime: StateFlow<Float> = _currentTime.asStateFlow()

    override fun getYouTubePlayerView(): View = youtubeStreamPlayerAdapter.getYouTubePlayerView()

    private val _videoSpeedChange = MutableStateFlow<YouTubeStreamPlaybackRate?>(null)
    override val videoSpeedChange: StateFlow<YouTubeStreamPlaybackRate?> = _videoSpeedChange.asStateFlow()

    private val _fullScreenView = MutableStateFlow<View?>(null)
    override val fullScreenView: StateFlow<View?> = _fullScreenView.asStateFlow()

    private val _exitFullScreen = MutableStateFlow<Boolean>(false)
    override val exitFullScreen: StateFlow<Boolean> = _exitFullScreen.asStateFlow()


    override fun initPlayer(networkHandle: Boolean?,videoId: String?) {
        if (initialPlayer) return

        youtubeStreamPlayerAdapter.initialize(
            this,
            networkHandle ?: false,
            iFramePlayerOptions,
            videoId
        )
        this.initialPlayer = true
        this.videoId = videoId
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
        youTubeStreamPlayer = youTubePlayer
        youTubeStreamPlayer?.addListener(this)

        if(autoPlay) videoId?.let{loadOrCueVideo(it, 0f)}

        _playbackState.update { YouTubeStreamPlaybackState.Prepared(this) }
    }

    override fun onStateChange(
        youTubePlayer: YouTubePlayer,
        state: PlayerState,
    ) {
        _playbackState.update { (getConvertPlayerStateToYouTubeStreamPlaybackState(state)) }
    }

    override fun onPlaybackQualityChange(
        youTubePlayer: YouTubePlayer,
        playbackQuality: PlayerConstants.PlaybackQuality,
    ) {

    }

    override fun onPlaybackRateChange(
        youTubePlayer: YouTubePlayer,
        playbackRate: PlaybackRate,
    ) {
        _videoSpeedChange.update {getConvertPlaybackRateToYouTubePlaybackRate(playbackRate)  }
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
    ) {}

    private fun getConvertPlayerStateToYouTubeStreamPlaybackState(state: PlayerState): YouTubeStreamPlaybackState =
        when (state) {
            PlayerState.UNKNOWN -> YouTubeStreamPlaybackState.UnKnown
            PlayerState.UNSTARTED -> YouTubeStreamPlaybackState.UnStarted
            PlayerState.VIDEO_CUED -> YouTubeStreamPlaybackState.Prepared(this)
            PlayerState.PLAYING -> {
                setMute(true)
                YouTubeStreamPlaybackState.Playing
            }
            PlayerState.PAUSED -> YouTubeStreamPlaybackState.Paused
            PlayerState.BUFFERING -> {
                setMute(true)
                YouTubeStreamPlaybackState.Buffering
            }
            PlayerState.ENDED -> YouTubeStreamPlaybackState.Ended
        }

    private fun getConvertPlayerErrorToYouTubeStreamPlayerError(error: PlayerConstants.PlayerError): YouTubeStreamPlayerError =
        when (error) {
            PlayerConstants.PlayerError.UNKNOWN -> YouTubeStreamPlayerError.UNKNOWN
            PlayerConstants.PlayerError.INVALID_PARAMETER_IN_REQUEST -> YouTubeStreamPlayerError.INVALID_PARAMETER_IN_REQUEST
            PlayerConstants.PlayerError.HTML_5_PLAYER -> YouTubeStreamPlayerError.HTML_5_PLAYER
            PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> YouTubeStreamPlayerError.VIDEO_NOT_FOUND
            PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER -> YouTubeStreamPlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER
        }

    private fun getConvertPlaybackRateToYouTubePlaybackRate(rate: PlayerConstants.PlaybackRate): YouTubeStreamPlaybackRate =
        when (rate) {
            PlaybackRate.UNKNOWN -> YouTubeStreamPlaybackRate.UNKNOWN
            PlaybackRate.RATE_0_25 -> YouTubeStreamPlaybackRate.RATE_0_25
            PlaybackRate.RATE_0_5 -> YouTubeStreamPlaybackRate.RATE_0_5
            PlaybackRate.RATE_0_75 -> YouTubeStreamPlaybackRate.RATE_0_75
            PlaybackRate.RATE_1 -> YouTubeStreamPlaybackRate.RATE_1
            PlaybackRate.RATE_1_25 -> YouTubeStreamPlaybackRate.RATE_1_25
            PlaybackRate.RATE_1_5 -> YouTubeStreamPlaybackRate.RATE_1_5
            PlaybackRate.RATE_1_75 -> YouTubeStreamPlaybackRate.RATE_1_75
            PlaybackRate.RATE_2 -> YouTubeStreamPlaybackRate.RATE_2
        }
    private fun getConvertYouTubePlaybackRatYoPlaybackRate(rate: YouTubeStreamPlaybackRate): PlaybackRate =
        when (rate) {
            YouTubeStreamPlaybackRate.UNKNOWN -> PlaybackRate.UNKNOWN
            YouTubeStreamPlaybackRate.RATE_0_25 -> PlaybackRate.RATE_0_25
            YouTubeStreamPlaybackRate.RATE_0_5 -> PlaybackRate.RATE_0_5
            YouTubeStreamPlaybackRate.RATE_0_75 -> PlaybackRate.RATE_0_75
            YouTubeStreamPlaybackRate.RATE_1 -> PlaybackRate.RATE_1
            YouTubeStreamPlaybackRate.RATE_1_25 -> PlaybackRate.RATE_1_25
            YouTubeStreamPlaybackRate.RATE_1_5 -> PlaybackRate.RATE_1_5
            YouTubeStreamPlaybackRate.RATE_1_75 -> PlaybackRate.RATE_1_75
            YouTubeStreamPlaybackRate.RATE_2 -> PlaybackRate.RATE_2
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

    override fun addFullscreenListener(): Boolean  =  youtubeStreamPlayerAdapter.addFullscreenListener(this)


    override fun removeFullscreenListener(): Boolean = youtubeStreamPlayerAdapter.removeFullscreenListener(this)


    override fun onEnterFullscreen(
        fullscreenView: View,
        exitFullscreen: () -> Unit
    ) {
        _fullScreenView.update { fullscreenView }
        _exitFullScreen.update { false }

    }

    override fun onExitFullscreen() {
        _exitFullScreen.update { true }
    }
}
