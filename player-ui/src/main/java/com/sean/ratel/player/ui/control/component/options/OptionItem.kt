package com.sean.ratel.player.ui.control.component.options

import androidx.compose.ui.graphics.vector.ImageVector


data class OptionItem(
    val key: MediaOptionKey,
    val value:MediaOptionValue,
    val icon: ImageVector,
    val label: String,
    val enabled: Boolean,
    val locked: Boolean,
    val onClick: () -> Unit
)