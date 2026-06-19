package com.sean.ratel.player.core.data.domain.model

import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import com.sean.ratel.player.core.R

@UnstableApi
enum class ContentScale(
    val scaleIndex: Int,
    val label: Int,
) {
    Fit(AspectRatioFrameLayout.RESIZE_MODE_FIT, R.string.scale_fit),
    FillWidth(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH, R.string.scale_fill_width),
    FillHeight(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT, R.string.scale_fill_height),
    Inside(AspectRatioFrameLayout.RESIZE_MODE_FILL, R.string.scale_inside),
    Zoom(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, R.string.scale_crop),
}
