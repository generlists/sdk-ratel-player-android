package com.sean.ratel.player.ui.control.component.options

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun ReplayButton(onclick:()->Unit) {
    IconButton(onClick = onclick) {
      Icon(Icons.Default.Replay , contentDescription = "replay", modifier  = Modifier
        .width(38.dp)
        .height(38.dp),
        tint = Color.White)
    }

}
