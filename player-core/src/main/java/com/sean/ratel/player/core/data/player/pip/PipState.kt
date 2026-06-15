package com.sean.ratel.player.core.data.player.pip

import android.graphics.Rect
import android.util.Size

data class PIPState(
    val pipAction: PipAction,
    val screenRect: Rect,
    val screenSize: Size,
    val pipTarget: PIPTarget,
)
