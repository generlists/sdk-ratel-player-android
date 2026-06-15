package com.sean.ratel.player.core.data.player.pip

import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.sean.ratel.player.utils.PlayerUtils.hasPipPermission
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import so.smartlab.common.utils.log.RLog
import java.lang.ref.WeakReference
import java.util.UUID
import javax.inject.Inject

@ActivityRetainedScoped
class PIPManager
    @Inject
    constructor() : DefaultLifecycleObserver {
        private var activityRef: WeakReference<ComponentActivity>? = null
        private val _pipClick =
            MutableSharedFlow<PIPTarget>(
                replay = 1,
                extraBufferCapacity = 1,
            )
        val pipClick: SharedFlow<PIPTarget> = _pipClick.asSharedFlow()

        private val _pipAction =
            MutableSharedFlow<PipAction>(
                replay = 1,
                extraBufferCapacity = 1,
            )

        val pipAction: SharedFlow<PipAction> = _pipAction.asSharedFlow()

        private val _screenRect = MutableStateFlow<Rect>(Rect())
        val screenRect = _screenRect.asStateFlow()

        private val _isPlaying = MutableStateFlow<Boolean>(false)
        val isPlaying = _screenRect.asStateFlow()

        private val _videoSize = MutableStateFlow<Size>(Size(0, 0))
        val videoSize = _videoSize.asStateFlow()

        private val _isFirst = MutableStateFlow<Boolean>(false)
        val isFirst = _isFirst.asStateFlow()

        private val _isLast = MutableStateFlow<Boolean>(false)
        val isLast = _isLast.asStateFlow()

        private val pipActionReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent,
                ) {
                    intent ?: return
                    RLog.d(
                        "PIP_CLICK",
                        "onReceive() , intent=$intent, action=${intent.action} extra : ${
                            intent.getIntExtra(
                                "splay.intent.extra.pip",
                                0,
                            )
                        }",
                    )

                    when {
                        PipAction.isPauseAction(intent) -> _pipAction.tryEmit(PipAction.PAUSE)
                        PipAction.isPlayAction(intent) -> _pipAction.tryEmit(PipAction.PLAY)
                        PipAction.isPreviousAction(intent) -> _pipAction.tryEmit(PipAction.SKIP_PREVIOUS)
                        PipAction.isNextAction(intent) -> _pipAction.tryEmit(PipAction.SKIP_NEXT)
                        PipAction.isReplayAction(intent) -> _pipAction.tryEmit(PipAction.REPLAY)
                    }
                }
            }

        fun bind(activity: ComponentActivity) {
            activityRef = WeakReference(activity)
            activity.lifecycle.addObserver(this)
        }

        override fun onStart(owner: LifecycleOwner) {
            val activity = (owner as ComponentActivity)
            activity.registerReceiver(
                pipActionReceiver,
                PipAction.getIntentFilter(),
                RECEIVER_NOT_EXPORTED,
            )
        }

        override fun onStop(owner: LifecycleOwner) {
            (owner as ComponentActivity).unregisterReceiver(pipActionReceiver)
        }

        override fun onDestroy(owner: LifecycleOwner) {
        }

        fun enterPipMode(
            videoSize: Size?,
            rect: Rect,
            isPlaying: Boolean,
            isFirst: Boolean,
            isLast: Boolean,
        ): PipResult {
            val pipContext = activityRef?.get() ?: return PipResult.UnKnownReason
            if (videoSize == null) return PipResult.UnKnownReason

            val hasPipSystemFeature =
                pipContext.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            if (!hasPipSystemFeature) return PipResult.NoSystemFeature

            if (!pipContext.hasPipPermission()) return PipResult.NoPermission

            RLog.d("PIP_CLICK", "enterPipMode isFirst : $isFirst isLast : $isLast")
            updatePipParamsAndGo(
                isPlaying = isPlaying,
                videoSize = videoSize,
                rect = rect,
                isFirst = isFirst,
                isLast = isLast,
            )

            return PipResult.Success
        }

        private fun updatePipParamsAndGo(
            isPlaying: Boolean,
            videoSize: Size?,
            rect: Rect,
            isFirst: Boolean,
            isLast: Boolean,
        ) = updatePipParams(
            isPlaying = isPlaying,
            videoSize = videoSize,
            rect = rect,
            enter = true,
            isFirst = isFirst,
            isLast = isLast,
        )

        fun updatePipParams(
            isPlaying: Boolean,
            videoSize: Size?,
            rect: Rect,
            enter: Boolean = false,
            endPlay: Boolean = false,
            isFirst: Boolean = false,
            isLast: Boolean = false,
        ) {
            _isPlaying.value = isPlaying
            _videoSize.value = videoSize ?: Size(0, 0)
            _screenRect.value = rect
            _isFirst.value = isFirst
            _isLast.value = isLast

            val pipContext = activityRef?.get() ?: return
            val aspectRatio = getPipAspectRatio(videoSize) ?: return
            val playAction = PipAction.getRemoteActionPlayPause(pipContext, isPlaying, endPlay)
            val playPrevAction = PipAction.getRemoteActionSkipPrevious(pipContext, isFirst)
            val playNextAction = PipAction.getRemoteActionSkipNext(pipContext, isLast)

            RLog.d(
                "PIP_CLICK",
                "[PIP] updatePipParams $playPrevAction, isPlaying $isPlaying ," +
                    " aspectRatio : $aspectRatio, enter : $enter rect : $rect isFirst : $isFirst",
            )

            val params =
                PictureInPictureParams
                    .Builder()
                    .setActions(listOf(playPrevAction, playAction, playNextAction))
                    .setAspectRatio(aspectRatio)
                    .setSourceRectHint(rect)

            RLog.d(
                "KKKKKKKKK",
                "actions size=${listOf(playPrevAction, playAction, playNextAction).size}",
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                params.setAutoEnterEnabled(true)
                params.setSeamlessResizeEnabled(true)
            }

            val updateParam =
                params.build().also {
                    pipContext.setPictureInPictureParams(it)
                }

            if (enter) {
                pipContext.enterPictureInPictureMode(updateParam)
            } else {
                pipContext.setPictureInPictureParams(updateParam)
            }
        }

        private fun getPipAspectRatio(videoSize: Size?): Rational? {
            val size = videoSize ?: return null

            if (size.width <= 0 || size.height <= 0) {
                return null
            }

            val aspectRatio = Rational(size.width, size.height)

            return when {
                aspectRatio.toFloat() > MAX_PIP_ASPECT_RATIO.toFloat() -> MAX_PIP_ASPECT_RATIO
                aspectRatio.toFloat() < MIN_PIP_ASPECT_RATIO.toFloat() -> MIN_PIP_ASPECT_RATIO
                else -> aspectRatio
            }
        }

        fun setPIPClick(
            pageId: String,
            isPipClick: Boolean,
        ) {
            val target = PIPTarget(pageId, isPipClick)
            _pipClick.tryEmit(target)
        }

        fun refreshActions() {
            RLog.d("PIP", "_isPlaying : ${_isPlaying.value} , _videoSize : ${_videoSize.value}")
            updatePipParams(
                isPlaying = _isPlaying.value,
                videoSize = _videoSize.value,
                rect = _screenRect.value,
                enter = false,
                isFirst = _isFirst.value,
                isLast = _isLast.value,
            )
        }

        companion object {
            val TAG = "PIPViewModel"
            private val MAX_PIP_ASPECT_RATIO = Rational(239, 100)
            private val MIN_PIP_ASPECT_RATIO = Rational(100, 239)
        }
    }
