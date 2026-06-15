package com.sean.ratel.player.ui.control.component.options

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
@Suppress("ktlint:standard:function-naming")
fun OptionButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(end = 24.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color.White,
            )
        }
    }
}
