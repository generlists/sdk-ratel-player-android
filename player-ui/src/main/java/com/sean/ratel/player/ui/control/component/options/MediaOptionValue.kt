package com.sean.ratel.player.ui.control.component.options

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.core.data.domain.model.ContentScale
import com.sean.ratel.player.core.data.domain.model.PlaySpeed
import com.sean.ratel.player.core.data.domain.model.PreviewInfoData
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.core.data.domain.model.RepeatMode

@OptIn(UnstableApi::class)
sealed class MediaOptionValue {
    data class Toggle(val enabled: Boolean) : MediaOptionValue()
    data class Speed(val value: PlaySpeed) : MediaOptionValue()
    data class Repeat(val value: RepeatMode) : MediaOptionValue()
    data class Scale(val value: ContentScale): MediaOptionValue()
    data class IntValue(val value: Int) : MediaOptionValue()
    data class FloatValue(val value: Float) : MediaOptionValue()
    data class VideoQuality(val quality: Pair<Quality,String>) : MediaOptionValue()
    data class ObjectValue(val value: PreviewInfoData) : MediaOptionValue()
}