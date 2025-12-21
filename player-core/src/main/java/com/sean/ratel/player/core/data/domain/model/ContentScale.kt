package com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model

import android.annotation.SuppressLint
import androidx.media3.ui.AspectRatioFrameLayout

@SuppressLint("UnsafeOptInUsageError")
val CONTENT_SCALES =
    listOf(
        "Fit" to AspectRatioFrameLayout.RESIZE_MODE_FIT,
        "FillWidth" to AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH,
        "FillHeight" to AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT,
        "Inside" to AspectRatioFrameLayout.RESIZE_MODE_FILL,
        "Zoom" to AspectRatioFrameLayout.RESIZE_MODE_ZOOM,

    )