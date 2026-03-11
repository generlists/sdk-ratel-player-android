package com.sean.ratel.player.ui.control.component.share

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ShareItem(
    val label: String,
    val packageName: String,
    val icon: Painter,
    val size: Dp = 48.dp,
    val action: (Context, Uri) -> Unit
)