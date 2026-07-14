package com.sean.ratel.player.demo.ui.cast.notifications

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import com.sean.ratel.player.demo.ui.cast.CastEvent
import com.sean.ratel.player.demo.ui.cast.CastSessionState
import com.sean.ratel.player.demo.ui.cast.YouTubePlayersManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastNotificationManager
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val playersManager: YouTubePlayersManager,
    ) {
        private val _state = MutableStateFlow(CastNotificationState())
        val state: StateFlow<CastNotificationState> = _state.asStateFlow()

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        init {

            scope.launch {

                combine(
                    playersManager.castEventManager.castSession,
                    playersManager.videoList,
                    playersManager.currentIndex,
                    playersManager.castEventManager.playEvents,
                ) { session, videoList, index, playEvent ->

                    Triple(session, videoList?.get(index), playEvent)
                }.collect { (session, video, playEvent) ->

                    Log.d("CastForegroundService", "session : $session")
                    Log.d("CastForegroundService", "video : $video")
                    Log.d("CastForegroundService", "playEvent : $playEvent")

                    when (session) {
                        is CastSessionState.SessionStart -> {
                            val bitmap =
                                loadBitmapWithRightPadding(
                                    context,
                                    video?.videoModel?.thumbNail ?: "",
                                )
                            _state.value =
                                _state.value.copy(
                                    isVisible = true,
                                    title = video?.videoModel?.title ?: "",
                                    deviceName = session.castDevice ?: "알수 없음",
                                    channelName = video?.channelModel?.title ?: "",
                                    thumbnail = bitmap,
                                    isPlaying = playEvent is CastEvent.Play,
                                    isMuted = playEvent is CastEvent.Mute,
                                )
                        }

                        is CastSessionState.SessionEnd -> {
                            _state.value = CastNotificationState()
                        }

                        else -> {}
                    }
                    when (playEvent) {
                        is CastEvent.Play -> {
                            _state.value =
                                _state.value.copy(
                                    isPlaying = false,
                                )
                            playersManager.castPlay(false)
                        }

                        is CastEvent.Pause -> {
                            _state.value =
                                _state.value.copy(
                                    isPlaying = true,
                                )
                            playersManager.castPlay(true)
                        }

                        is CastEvent.Prev -> {
                            playersManager.castPrevPlay()
                        }

                        is CastEvent.Next -> {
                            playersManager.castNextPlay()
                        }

                        else -> {
                        }
                    }
                }
            }
        }

        suspend fun loadBitmapWithRightPadding(
            context: Context,
            url: String,
        ): Bitmap? {
            if (url.isEmpty()) return null

            val loader = ImageLoader(context)

            val request =
                ImageRequest
                    .Builder(context)
                    .data(url)
                    .allowHardware(false) // Bitmap 필요
                    .build()

            val result = loader.execute(request)

            return (result.drawable as? BitmapDrawable)?.bitmap
        }

        fun release() {
            // playersManager.release()
//        sessionManagerListener?.let {
//            CastContext.getSharedInstance(context)
//                .sessionManager
//                .removeSessionManagerListener(it, CastSession::class.java)
//        }
        }
    }
