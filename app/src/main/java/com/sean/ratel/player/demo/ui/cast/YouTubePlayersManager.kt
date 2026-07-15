package com.sean.ratel.player.demo.ui.cast

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.sean.ratel.player.core.data.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlayerError
import com.sean.ratel.player.core.data.player.youtube.YouTubeCastPlayerAdapterImpl
import com.sean.ratel.player.core.data.player.youtube.YouTubeCastPlayerImpl
import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModel
import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModelList
import com.sean.ratel.player.demo.ui.cast.notifications.CastForegroundService
import com.sean.ratel.player.demo.ui.cast.notifications.CastNotificationAction.ACTION_STOP_CAST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubePlayersManager
    @Inject
    constructor(
        val castEventManager: CastEventManager,
    ) : ChromecastConnectionListener {
        private var youTubeStreamPlayer: YouTubeStreamPlayer? = null
        private var youTubeCastPlayer: YouTubeStreamPlayer? = null

        private val _playState =
            MutableStateFlow<YouTubeStreamPlaybackState>(YouTubeStreamPlaybackState.UnKnown)
        val playState = _playState.asStateFlow()

        private val _duration = MutableStateFlow<Float?>(null)
        val duration = _duration.asStateFlow()

        private val _currentTime = MutableStateFlow<Float?>(null)
        val currentTime = _currentTime.asStateFlow()

        private val _mute = MutableStateFlow<Boolean>(false)
        val mute = _mute.asStateFlow()

        private val _captionOnOff = MutableStateFlow<Boolean>(false)
        val captionOnOff = _captionOnOff.asStateFlow()

        private val _captionAvailable = MutableStateFlow<Boolean?>(null)
        val captionAvailable = _captionAvailable.asStateFlow()

        private val _debugString =
            MutableStateFlow<String>(YouTubeStreamPlaybackState.UnKnown.toString())
        val debugString = _debugString.asStateFlow()

        private val _localPlayerInit =
            MutableSharedFlow<Boolean>(
                extraBufferCapacity = 1,
            )

        val localPlayerInit = _localPlayerInit.asSharedFlow()

        private val _videoId = MutableStateFlow<String>("")
        val videoId = _videoId.asStateFlow()

        private val _currentIndex = MutableStateFlow<Int>(0)
        val currentIndex = _currentIndex.asStateFlow()

        private var activityRef: WeakReference<ComponentActivity>? = null
        private var chromecastYoutubePlayerContextRef: ChromecastYouTubePlayerContext? = null
        private var firstCaption = false

        private val _castPlayState =
            MutableStateFlow<YouTubeStreamPlaybackState>(YouTubeStreamPlaybackState.UnKnown)
        val castPlayState = _castPlayState.asStateFlow()

        private val _videoList = MutableStateFlow<List<YouTubeModel>?>(null)
        val videoList = _videoList.asStateFlow()

        private val _currentPlaySpeed =
            MutableStateFlow<YouTubeStreamPlaybackRate>(YouTubeStreamPlaybackRate.RATE_1)
        val currentPlaySpeed = _currentPlaySpeed.asStateFlow()

        fun bind(activity: FragmentActivity) {
            activityRef = WeakReference(activity)
            initChromecast(activity)
        }

        fun setVideoList(videoList: YouTubeModelList?) {
            _videoList.value = videoList?.videoList?.toList()
        }

        fun initChromecast(activity: ComponentActivity) {
            Log.d(
                "CHROME",
                "initChromecast(sessionManager) ${CastContext.getSharedInstance(activity).sessionManager}",
            )

            ChromecastYouTubePlayerContext(CastContext.getSharedInstance(activity).sessionManager, this)
        }

        fun onCastPlaybackState(scope: CoroutineScope) {
            scope.launch {
                youTubeCastPlayer
                    ?.playbackState

                    ?.combine(castEventManager.castSession) { playbackState, session ->

                        playbackState to session
                    }?.collect { (playbackState, session) ->

                        Log.d(
                            "SSSSSSSSSMMMMMMMM",
                            "playbackState : $playbackState , session : $session ,${_currentTime.value}",
                        )

                        _castPlayState.value = playbackState

                        when (playbackState) {
                            is YouTubeStreamPlaybackState.Ready -> {
                                if (session is CastSessionState.SessionStart) {
                                    Log.d("YouTubeCastPlayerImpl", "Ready videoId : ${videoId.value}")
//                                if(videoId.value.isNotEmpty())
//                                    loadCastVideo(videoId.value, 0f)
                                }
                            }

                            is YouTubeStreamPlaybackState.Prepared -> {
                            }

                            YouTubeStreamPlaybackState.UnStarted -> {
                                delay(500)
                                youTubeCastPlayer?.start()
                            }

                            YouTubeStreamPlaybackState.Playing -> {
                                setMute(true)
                            }

                            else -> {}
                        }
                    }
            }
        }

        fun setLocalPlayer(
            currentIndex: Int,
            localPlayer: YouTubeStreamPlayer?,
        ) {
            youTubeStreamPlayer = localPlayer
            _videoId.value = _videoList.value
                ?.get(currentIndex)
                ?.videoModel
                ?.videoId ?: ""
            _currentIndex.value = currentIndex

            val activity = activityRef?.get()

            if (activity != null) {
                activity.lifecycleScope.launch {
                    activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        youTubeStreamPlayer?.currentTime?.collect {
                            _currentTime.value = it
                        }
                    }
                }
                activity.lifecycleScope.launch {
                    activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        youTubeStreamPlayer?.duration?.collect {
                            _duration.value = it
                        }

                        youTubeCastPlayer?.duration?.collect {
                            _duration.value = it
                        }
                    }
                }
            }
        }

        fun onLocalPlaybackState(scope: CoroutineScope) {
            scope.launch {
                youTubeStreamPlayer?.playbackState?.collect { state ->

                    when (state) {
                        is YouTubeStreamPlaybackState.Prepared -> {
                            videoId.value.let {
//                            if (castEventManager.playerState.value == PlayerState.CAST) {
//                                loadVideo(
//                                    it,
//                                    0f,
//                                )
//                            }
                                _localPlayerInit.tryEmit(true)
                            }
                        }

                        YouTubeStreamPlaybackState.UnStarted -> {
                            // delay 로 시작 시간 확보
                            launch {
                                delay(500)
//                            castEventManager.playerState.collect { playerstate ->
//                                if (playerstate == PlayerState.CAST) {
//                                    youTubeStreamPlayer?.start()
//                                } else {
//                                    youTubeStreamPlayer?.pause()
//                                }
//
//                            }
                            }
                        }

                        YouTubeStreamPlaybackState.Buffering -> {}

                        YouTubeStreamPlaybackState.Paused -> {}

                        YouTubeStreamPlaybackState.Playing -> {
                            setMute(true)
                        }

                        YouTubeStreamPlaybackState.Ended -> {}

                        else -> {}
                    }

                    _debugString.value =
                        if (state is YouTubeStreamPlaybackState.Prepared) "Prepared" else state.toString()
                    _playState.value = state
                }
            }
        }

        fun setMute(isMute: Boolean) {
            // local
            youTubeStreamPlayer?.setMute(isMute)

            // cast
            youTubeCastPlayer?.setMute(isMute)

            _mute.value = isMute
        }

        fun loadVideo(
            videoId: String,
            startTime: Float?,
        ) {
            youTubeStreamPlayer?.loadOrCueVideo(videoId, startTime)
            if (castEventManager.castSession.value !is CastSessionState.SessionStart) {
                castEventManager.setPlayerState(PlayerState.LOCAL)
            }
        }

        fun castPlay(isPlaying: Boolean) {
            Log.d("SSSSSSSSS", "startVideID ${videoId.value}")
            if (isPlaying) {
                youTubeCastPlayer?.pause()
                castEventManager.send(CastEvent.Pause)
            } else {
                youTubeCastPlayer?.start()
                castEventManager.send(CastEvent.Play(videoId.value))
            }
        }

        fun castPrevPlay() {
            _currentIndex.value -= 1
            val prevVideoID =
                _videoList.value
                    ?.get(_currentIndex.value)
                    ?.videoModel
                    ?.videoId ?: ""
            _videoId.value = prevVideoID
            Log.d("SSSSSSSSS", "prevVideoID $prevVideoID")
            loadCastVideo(prevVideoID, 0f)
            castEventManager.send(CastEvent.Prev)
            firstCaption = false
        }

        fun castNextPlay() {
            _currentIndex.value += 1
            val nextVideoId =
                _videoList.value
                    ?.get(_currentIndex.value)
                    ?.videoModel
                    ?.videoId ?: ""
            _videoId.value = nextVideoId
            Log.d("SSSSSSSSS", "nextVideoId : $nextVideoId")
            loadCastVideo(nextVideoId, 0f)
            castEventManager.send(CastEvent.Next)
            firstCaption = false
        }

        fun loadCastVideo(
            videoId: String,
            startTime: Float?,
        ) {
            Log.d(
                "YouTubeCastPlayerImpl",
                "loadCastVideo $videoId youTubeCastYouTubePlayer : $youTubeCastPlayer",
            )
            youTubeCastPlayer?.loadOrCueVideo(videoId, startTime)
            castEventManager.setPlayerState(PlayerState.CAST)
        }

        fun setCasetMute(isMute: Boolean) {
            youTubeCastPlayer?.setMute(isMute)
            _mute.value = isMute
        }

        fun setCaptionOnOff(captionOnOff: Boolean) {
            Log.d("setCaptionOnOff", "set captionOnOff : $captionOnOff")

            if (captionOnOff) {
                youTubeCastPlayer?.enableCaptions(Locale.getDefault().language)
            } else {
                youTubeCastPlayer?.disableCaptions()
            }

            _captionOnOff.value = captionOnOff
        }

        fun onPlayError(
            context: Context,
            scope: CoroutineScope,
        ) {
            scope.launch {
                youTubeStreamPlayer?.playbackError?.collect { state ->
                    if (state != YouTubeStreamPlayerError.UNKNOWN) {
                        Toast.makeText(context, "$state", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        fun onPlaySpeedRate(scope: CoroutineScope) {
            scope.launch {
                youTubeStreamPlayer?.videoSpeedChange?.collect { rate ->
                    rate?.let {
                        _currentPlaySpeed.value = rate
                    }
                }
            }
            scope.launch {
                youTubeCastPlayer?.videoSpeedChange?.collect { rate ->
                    rate?.let {
                        _currentPlaySpeed.value = rate
                        Log.d("SPPPPED", "change rate :: $rate")
                    }
                }
            }
        }

        fun speedUp() {
            val localRateIndex = YouTubeStreamPlaybackRate.entries.indexOf(_currentPlaySpeed.value)
            if (localRateIndex < YouTubeStreamPlaybackRate.entries.lastIndex) {
                youTubeStreamPlayer?.setPlaybackRate(YouTubeStreamPlaybackRate.entries[localRateIndex + 1])
            }

            val castIndex = YouTubeStreamPlaybackRate.entries.indexOf(_currentPlaySpeed.value)
            if (castIndex < YouTubeStreamPlaybackRate.entries.lastIndex) {
                youTubeCastPlayer?.setPlaybackRate(YouTubeStreamPlaybackRate.entries[castIndex + 1])
            }
            Log.d("SPPPPED", "_currentPlaySpeed : ${_currentPlaySpeed.value} castIndex :: $castIndex")
        }

        fun speedDown() {
            val localRateIndex = YouTubeStreamPlaybackRate.entries.indexOf(_currentPlaySpeed.value)
            if (localRateIndex > 0) {
                youTubeStreamPlayer?.setPlaybackRate(YouTubeStreamPlaybackRate.entries[localRateIndex - 1])
            }
            val remoteRateIndex = YouTubeStreamPlaybackRate.entries.indexOf(_currentPlaySpeed.value)
            if (localRateIndex > 0) {
                youTubeCastPlayer?.setPlaybackRate(YouTubeStreamPlaybackRate.entries[remoteRateIndex - 1])
            }
        }

        fun captionAvailable(coroutineScope: CoroutineScope) {
            coroutineScope.launch {
                youTubeCastPlayer?.let { player ->
                    combine(
                        player.captionAvailable,
                        player.playbackState,
                        player.currentTime,
                    ) { available, state, currentTime ->
                        if (state == YouTubeStreamPlaybackState.Playing && currentTime > 3f) {
                            available
                        } else {
                            null
                        }
                    }.collect { available ->
                        if (!firstCaption && available != null) {
                            _captionAvailable.value = available
                            _captionOnOff.value = available
                            firstCaption = true
                        } else {
                            // _captionAvailable.value = null
                        }
                    }
                }
            }
        }

        override fun onChromecastConnecting() {
            Log.d("CHROME", "onChromecastConnecting...")
        }

        override fun onChromecastConnected(
            castDevice: String?,
            chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext,
        ) {
            Log.d("SISISISISISISISII", "onChromecastConnected")

            val activity = activityRef?.get()
            if (activity != null) {
                castEventManager.setCastSessionState(CastSessionState.SessionStart(castDevice))
                chromecastYoutubePlayerContextRef = chromecastYouTubePlayerContext

                initializeCastPlayer(activity.lifecycle)
                ContextCompat.startForegroundService(
                    activity,
                    Intent(activity, CastForegroundService::class.java),
                )
            }
        }

        override fun onChromecastDisconnected() {
            Log.d("CHROME", "onChromecastDisconnected")

            castEventManager.setCastSessionState(CastSessionState.SessionEnd)
            castEventManager.setPlayerState(PlayerState.LOCAL)
            val activity = activityRef?.get()
            val intent =
                Intent(activity, CastForegroundService::class.java).apply {
                    action = ACTION_STOP_CAST
                }
            activity?.stopService(intent)

            // youTubeCastYouTubePlayer?.release()
            // val intent = Intent(activity, CastForegroundService::class.java).apply {
            //  action = ACTION_STOP_CAST
            // }
            //   activity.startService(intent)
        }

        fun initializeCastPlayer(lifecycle: Lifecycle) {
            Log.d(
                "YouTubeCastPlayerImpl",
                "initializeCastPlayer videoId : $youTubeCastPlayer , context $chromecastYoutubePlayerContextRef",
            )

            chromecastYoutubePlayerContextRef?.let {
                Log.d(
                    "SISISISISISISISII",
                    "initializeCastPlayer context: $chromecastYoutubePlayerContextRef",
                )
                youTubeCastPlayer =
                    YouTubeCastPlayerImpl(
                        lifecycle = lifecycle,
                        autoPlay = true,
                        youtubeCastPlayerAdapter = YouTubeCastPlayerAdapterImpl(it),
                    )

                youTubeCastPlayer?.initPlayer(videoId = videoId.value)
                youTubeCastPlayer?.setMute(mute = true)
            }
        }

        fun isFirst(): Boolean = _currentIndex.value == 0

        fun isLast(): Boolean = _currentIndex.value == (_videoList.value?.size ?: (0 - 1))

        fun release() {
            chromecastYoutubePlayerContextRef?.release()
        }
    }
