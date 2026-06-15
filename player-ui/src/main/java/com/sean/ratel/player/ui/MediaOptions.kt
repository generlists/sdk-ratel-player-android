package com.sean.ratel.player.ui

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.core.data.domain.model.ContentScale
import com.sean.ratel.player.core.data.domain.model.PlaySpeed
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.core.data.domain.model.RepeatMode
import com.sean.ratel.player.ui.control.component.options.MediaOptionValue.FloatValue
import com.sean.ratel.player.ui.control.component.options.MediaOptionValue.Toggle

@OptIn(UnstableApi::class)
data class MediaOptions(
    val currentQuality: Quality = Quality.AUDIO,
    val repeatMode: RepeatMode = RepeatMode.REPEAT_OFF,
    val speed: PlaySpeed = PlaySpeed.PlaySpeed_1_0,
    val shuffle: Toggle = Toggle(enabled = false),
    val contentScale: ContentScale = ContentScale.FillHeight,
    val screenBrightness: FloatValue = FloatValue(value = 0.5f),
    val volume: FloatValue = FloatValue(value = 0.5f),
    val pip: Toggle = Toggle(enabled = true),
)
