package com.sean.ratel.player.core.data.player.pip

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import android.util.Size
import androidx.lifecycle.ViewModel
import com.sean.ratel.player.utils.PlayerUtils.hasPipPermission
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import so.smartlab.common.utils.log.RLog

/**
 * PIP View 모델
 */
class PIPViewModel : ViewModel() {
    private val _pipClick =
        MutableSharedFlow<PIPTarget>(
            replay = 1,
            extraBufferCapacity = 1,
        )
    val pipClick: SharedFlow<PIPTarget> = _pipClick.asSharedFlow()

    fun enterPipMode(
        context: Context?,
        videoSize: Size?,
        rect: Rect,
        isPlaying: Boolean,
    ): PipResult {
        val pipContext = context ?: return PipResult.UnKnownReason
        if (videoSize == null) return PipResult.UnKnownReason

        val hasPipSystemFeature =
            pipContext.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        if (!hasPipSystemFeature) return PipResult.NoSystemFeature

        if (!pipContext.hasPipPermission()) return PipResult.NoPermission
        updatePipParamsAndGo(context, isPlaying, videoSize, rect)

        return PipResult.Success
    }

    private fun updatePipParamsAndGo(
        context: Context?,
        isPlaying: Boolean,
        videoSize: Size?,
        rect: Rect,
    ) = updatePipParams(context, isPlaying, videoSize, rect, true)

    fun updatePipParams(
        context: Context?,
        isPlaying: Boolean,
        videoSize: Size?,
        rect: Rect,
        enter: Boolean = false,
    ) {
        val pipContext = (context as? Activity) ?: return
        val aspectRatio = getPipAspectRatio(videoSize) ?: return
        val playAction = PipAction.getRemoteAction(pipContext, isPlaying)
        val playPrevAction = PipAction.getRemoteAction(pipContext, PipAction.SKIP_PREVIOUS)
        val playNextAction = PipAction.getRemoteAction(pipContext, PipAction.SKIP_NEXT)

        RLog.d(
            "MainActivity",
            "[PIP] updatePipParams isPlaying $isPlaying ," +
                " aspectRatio : $aspectRatio, enter : $enter",
        )

        val params =
            PictureInPictureParams
                .Builder()
                .setActions(listOf(playPrevAction, playAction, playNextAction))
                .setAspectRatio(aspectRatio)
                .setSourceRectHint(rect)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // The screen automatically turns into the picture-in-picture mode when it is hidden
            // by the "Home" button.
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
        _pipClick.tryEmit(PIPTarget(pageId, isPipClick))
    }

    companion object {
        val TAG = "PIPViewModel"
        private val MAX_PIP_ASPECT_RATIO = Rational(239, 100)
        private val MIN_PIP_ASPECT_RATIO = Rational(100, 239)
    }
}
