package com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.youtube

import android.util.Size
import android.view.View
import androidx.lifecycle.Lifecycle
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayerBridge
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlayQuality
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.youtube.adaptor.YouTubeCastPlayerAdapter
import com.sean.ratel.player.core.com.sean.ratel.player.core.util.PlayerUtil.getConvertPlaybackRateToYouTubePlaybackRate
import com.sean.ratel.player.core.com.sean.ratel.player.core.util.PlayerUtil.getConvertPlayerErrorToYouTubeStreamPlayerError
import com.sean.ratel.player.core.com.sean.ratel.player.core.util.PlayerUtil.getConvertYouTubePlaybackRatYoPlaybackRate
import com.sean.ratel.player.core.data.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlayerError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import so.smartlab.common.utils.log.RLog

class YouTubeCastPlayerImpl(
    private val lifecycle: Lifecycle,
    private val autoPlay: Boolean,
    private val youtubeCastPlayerAdapter: YouTubeCastPlayerAdapter,
) : AbstractYouTubePlayerListener(),
    YouTubeStreamPlayer, YouTubePlayerBridge.YouTubePlayerBridgeCallbacks {

    private var youTubeCastPlayer: YouTubePlayer? = null
    private var videoId: String? = null


    override val fullScreenView: StateFlow<View?> =MutableStateFlow<View?>(null)
    override val exitFullScreen: StateFlow<Boolean> = MutableStateFlow<Boolean>(false)


    override fun getYouTubePlayerView(): View? = null

    private val _playbackState =
        MutableStateFlow<YouTubeStreamPlaybackState>(
            YouTubeStreamPlaybackState.UnKnown,
        )
    override val playbackState: StateFlow<YouTubeStreamPlaybackState> = _playbackState.asStateFlow()
    private val _videoQualityChange =
        MutableStateFlow<YouTubeStreamPlayQuality>(YouTubeStreamPlayQuality.Auto)
    override val videoQualityChange: StateFlow<YouTubeStreamPlayQuality> =
        _videoQualityChange.asStateFlow()

    private val _videoSpeedChange = MutableStateFlow<YouTubeStreamPlaybackRate?>(null)
    override val videoSpeedChange: StateFlow<YouTubeStreamPlaybackRate?> =
        _videoSpeedChange.asStateFlow()

    private val _playbackError = MutableSharedFlow<YouTubeStreamPlayerError>(replay = 1)
    override val playbackError: SharedFlow<YouTubeStreamPlayerError> = _playbackError.asSharedFlow()


    private val _currentTime = MutableStateFlow(0f)
    override val currentTime: StateFlow<Float> = _currentTime.asStateFlow()

    private val _duration = MutableStateFlow(0f)
    override val duration: StateFlow<Float> = _duration.asStateFlow()

    private val _isMute = MutableStateFlow(true)
    val isMute: StateFlow<Boolean> = _isMute.asStateFlow()

    private val _captionAvailable = MutableStateFlow<Boolean>(false)
    override val captionAvailable: StateFlow<Boolean> =
        _captionAvailable.asStateFlow()

    private val _videoQualityLevel = MutableStateFlow<List<YouTubeStreamPlayQuality>>(emptyList())
    override val videoQualityLevel: StateFlow<List<YouTubeStreamPlayQuality>> =
        _videoQualityLevel.asStateFlow()

    override fun initPlayer( networkHandle: Boolean? ,videoId: String?) {

        RLog.d(TAG,"시발 크롬 캐스트 초기화 initPlayer ChormeCast ${youtubeCastPlayerAdapter.getChromeCastContext()}, youTubePlayer : $videoId , youTubeCastPlayer : $youTubeCastPlayer")

        youtubeCastPlayerAdapter.initialize(this,this)
        this.videoId = videoId
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        RLog.d(TAG,"LIB 내플레이어 onReady youTubePlayer : $youTubePlayer")
        youTubeCastPlayer = youTubePlayer
        youTubeCastPlayer?.addListener(this)

         _playbackState.update { YouTubeStreamPlaybackState.Ready }
    }

    override fun onStateChange(
        youTubePlayer: YouTubePlayer,
        state: PlayerState,
    ) {
        RLog.d(TAG, "[LIB] onStateChange : $state")

        val converted = getConvertPlayerStateToYouTubeStreamPlaybackState(state)

        RLog.d(TAG, """[LIB] origin=$state current=${_playbackState.value} next=$converted equal=${_playbackState.value == converted}

    """.trimIndent()

        )
        _playbackState.update { (getConvertPlayerStateToYouTubeStreamPlaybackState(state)) }
    }

    override fun onPlaybackQualityChange(
        youTubePlayer: YouTubePlayer,
        playbackQuality: PlayerConstants.PlaybackQuality,
    ) {
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

    override fun onPlaybackRateChange(
        youTubePlayer: YouTubePlayer,
        playbackRate: PlayerConstants.PlaybackRate,
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
    ) {}

    override fun onVideoId(
        youTubePlayer: YouTubePlayer,
        videoId: String,
    ) {}

    override fun onApiChange(youTubePlayer: YouTubePlayer) {}

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
        RLog.d(TAG,"[LIB] youTubePlayer $youTubePlayer ,  available : $available")
        _captionAvailable.update { available }
    }

    override fun enableCaptions(languageCode: String) {
        youTubeCastPlayer?.enableCaptions(languageCode)
    }

    override fun disableCaptions() {
        youTubeCastPlayer?.disableCaptions()
    }


    override fun getChromeCastContext(): ChromecastYouTubePlayerContext = getChromeCastContext()

    override fun chromeCastRelease() {
        getChromeCastContext().release()
    }

    override fun endCurrentSession() {

        getChromeCastContext().endCurrentSession()
    }

    override fun onCaptionAvailable() {
      youTubeCastPlayer?.checkCaptionAvailable()
    }


    override fun loadOrCueVideo(
        videoId: String,
        startTime: Float?,
    ) {
        RLog.d(TAG,"[LIB] loadOrCueVideo $videoId , youTubeCastPlayer : $youTubeCastPlayer")

        youTubeCastPlayer?.loadOrCueVideo(
            lifecycle,
            videoId,
            startTime ?: 0f,
        )
        _playbackState.update { YouTubeStreamPlaybackState.Prepared(this) }
    }

    override fun loadVideo(
        videoId: String,
        startTime: Float?,
    ) {
        youTubeCastPlayer?.loadVideo(videoId, startTime ?: 0f)

        RLog.d(TAG,"[LIB] loadVideo $videoId , youTubeCastPlayer : $youTubeCastPlayer")

    }

    override fun cueVideo(
        videoId: String,
        startTime: Float?,
    ) {
        youTubeCastPlayer?.cueVideo(videoId, startTime ?: 0f)
    }

    override fun start() {
        youTubeCastPlayer?.let { player ->
            player.play()
            _playbackState.update { YouTubeStreamPlaybackState.Playing }
        }
    }

    override fun seekTo(msec: Float) {
        youTubeCastPlayer?.seekTo(msec)
    }

    override fun resume() {}

    override fun pause() {
        youTubeCastPlayer?.pause()
        _playbackState.update { YouTubeStreamPlaybackState.Paused }
    }

    override fun stop() {}

    override fun isPlaying(): Boolean {
        return _playbackState.value is YouTubeStreamPlaybackState.Playing
    }

    override fun release() {
        youtubeCastPlayerAdapter.release()
        _playbackState.update { YouTubeStreamPlaybackState.RELEASE }
    }

    override fun setMute(mute: Boolean) {
        if (mute) youTubeCastPlayer?.mute() else youTubeCastPlayer?.unMute()

        _isMute.update { mute }
    }

    override fun setPlaybackRate(playbackRate: YouTubeStreamPlaybackRate) {
        RLog.d(
            TAG,
            "[LIB] $playbackRate , speed : ${getConvertYouTubePlaybackRatYoPlaybackRate(playbackRate)}",
        )
        youTubeCastPlayer?.setPlaybackRate(getConvertYouTubePlaybackRatYoPlaybackRate(playbackRate))
    }

    override fun getVideoSize(): Size? = null


    override fun setLoop(loop: Boolean) {
        youTubeCastPlayer?.setLoop(loop)
    }

    override fun setShuffle(shuffle: Boolean) {
        youTubeCastPlayer?.setShuffle(shuffle)
    }

    override fun toggleFullscreen() {}

    override fun addFullscreenListener(): Boolean =false

    override fun removeFullscreenListener(): Boolean = false

    override fun setQuality(quality: YouTubeStreamPlayQuality) {
        if (quality == YouTubeStreamPlayQuality.Auto) {
            youTubeCastPlayer?.setQuality(PlayerConstants.PlaybackQuality.DEFAULT)
        } else if (quality == YouTubeStreamPlayQuality.P720) {
            youTubeCastPlayer?.setQuality(PlayerConstants.PlaybackQuality.HD720)
        } else if (quality == YouTubeStreamPlayQuality.P1080) {
            youTubeCastPlayer?.setQuality(PlayerConstants.PlaybackQuality.HD1080)
        }
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

    override val listeners: Collection<YouTubePlayerListener> = arrayListOf(this)

    override fun getInstance(): YouTubePlayer
       =  youtubeCastPlayerAdapter.getChromeCastContext().getYouTubePlayer()


    override fun onYouTubeIFrameAPIReady() {
        RLog.d(TAG,"LIB 내 플레이어 [YouTubeCastPlayerImpl]   onYouTubeIFrameAPIReady ${getInstance()}")
        youTubeCastPlayer = getInstance()
//        youTubeCastPlayer?.addListener(this)

        _playbackState.update { YouTubeStreamPlaybackState.IFrameReady }
        //RLog.d(TAG,"[LIB] YouTubeCastPlayerImpl onYouTubeIFrameAPIReady ${getInstance()}")
        //_playbackState.update { YouTubeStreamPlaybackState.RELEASE }

    }
    companion object {
        private const val TAG ="YouTubeCastPlayerImpl"
    }
}
