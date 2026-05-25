package com.sean.ratel.player.ui.control.component.options

import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.core.data.domain.model.ContentScale
import com.sean.ratel.player.core.data.domain.model.PlayMediaItem
import com.sean.ratel.player.core.data.domain.model.PlaySpeed
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.ui.R
import com.sean.ratel.player.ui.ThemeMode
import com.sean.ratel.player.ui.control.component.options.MediaOptionValue.Speed
import kotlinx.coroutines.flow.StateFlow

@OptIn(UnstableApi::class)
@Composable
internal fun OptionSection(
    title: String,
    themeMode: ThemeMode,
    isSystemVolumeMute: StateFlow<Boolean>,
    items: List<OptionItem>,
    speeds: List<PlaySpeed> = listOf(),
    qualityList: List<Pair<Quality, PlayMediaItem>> = listOf(),
    scaleOptions: Array<ContentScale>? = null,
    openLayer: Map<MediaOptionKey, Boolean> = mapOf(),
    onChangeLayer: (Boolean) -> Unit = {},
    onSelect: (Pair<Quality, PlayMediaItem>?, PlaySpeed?, ContentScale?) -> Unit = { q, p, c -> },
    brightSoundDelta: (Float?, Float?) -> Unit = { s, v -> }
) {

    var currentQualityValue by remember {
        mutableStateOf<MediaOptionValue.VideoQuality?>(null)
    }

    var currentSpeedValue by remember {
        mutableStateOf<MediaOptionValue.Speed?>(null)
    }

    var currentScaleValue by remember {
        mutableStateOf<MediaOptionValue.Scale?>(null)
    }
    var currentBrightnessValue by remember {
        mutableStateOf<Float?>(null)
    }

    var currentVolumeValue by remember {
        mutableStateOf<Float?>(null)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp), // 카드 바깥 여백
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = if (themeMode == ThemeMode.DARK) 0.dp else 4.dp)


        ) {

            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            FlowRow(
                Modifier.padding(start = 16.dp, bottom = 16.dp),
                maxItemsInEachRow = 5,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items.forEach { item ->
                    OptionItemView(item)

                    if (item.key == MediaOptionKey.VIDEO_QUALITY) {

                        currentQualityValue = when {
                            item.value is MediaOptionValue.VideoQuality -> item.value
                            else -> null
                        }

                    }

                    if (item.key == MediaOptionKey.PLAYBACK_SPEED) {

                        currentSpeedValue = when {
                            item.value is Speed -> item.value
                            else -> null
                        }

                    }
                    if (item.key == MediaOptionKey.SCREEN_SCALE) {
                        currentScaleValue = when {
                            item.value is MediaOptionValue.Scale -> item.value
                            else -> null
                        }
                    }
                    if (item.key == MediaOptionKey.SCREEN_BRIGHT) {
                        currentBrightnessValue = when {
                            item.value is MediaOptionValue.FloatValue -> item.value.value
                            else -> null
                        }
                    }

                    if (item.key == MediaOptionKey.SCREEN_VOLUME) {
                        currentVolumeValue = when {
                            item.value is MediaOptionValue.FloatValue -> item.value.value
                            else -> null
                        }
                    }
                }
            }
            val currentActionMenu = openLayer.filter { (_, value) -> value }
            if (currentActionMenu.isNotEmpty()) {
                when (title) {
                    stringResource(R.string.player_control_screen_options) -> {

                        if (currentActionMenu.keys.first() == MediaOptionKey.SCREEN_SCALE) {


                            SelectControl(
                                currentActionMenu = currentActionMenu.keys.first(),
                                currentSpeed = currentSpeedValue,
                                speeds = speeds,
                                currentScale = currentScaleValue,
                                scales = scaleOptions,
                                currentQuality = currentQualityValue,
                                qualityList = qualityList,
                                onSelected = { _, _, scale ->
                                    onSelect(null, null, scale)
                                    onChangeLayer(
                                        !(openLayer[MediaOptionKey.SCREEN_SCALE] ?: false)
                                    )
                                },
                                onDismiss = {
                                    onChangeLayer(
                                        !(openLayer[MediaOptionKey.SCREEN_SCALE] ?: false)
                                    )
                                }
                            )
                        }

                        if (currentActionMenu.keys.first() == MediaOptionKey.SCREEN_BRIGHT || currentActionMenu.keys.first() == MediaOptionKey.SCREEN_VOLUME) {
                            OptionSeekControl(
                                key = currentActionMenu.keys.first(),
                                isSystemVolumeMute = isSystemVolumeMute,
                                currentBrightnessValue = currentBrightnessValue,
                                currentVolumeValue = currentVolumeValue,
                                brightVolumeDelta = brightSoundDelta
                            )
                        }

                    }

                    stringResource(R.string.player_control_options) -> {
                        if (currentActionMenu.keys.first() == MediaOptionKey.VIDEO_QUALITY ||
                            currentActionMenu.keys.first() == MediaOptionKey.PLAYBACK_SPEED
                        ) {
                            SelectControl(
                                currentActionMenu.keys.first(),
                                currentSpeedValue,
                                speeds,
                                currentScaleValue,
                                scaleOptions,
                                currentQuality = currentQualityValue,
                                qualityList = qualityList,
                                onSelected = { quality, speed, _ ->

                                    speed?.let {
                                        onSelect(null, speed, null)
                                        onChangeLayer(
                                            !(openLayer[MediaOptionKey.PLAYBACK_SPEED] ?: false)
                                        )
                                    }
                                    quality?.let {

                                        onSelect(quality, null, null)
                                        onChangeLayer(
                                            !(openLayer[MediaOptionKey.VIDEO_QUALITY] ?: false)
                                        )
                                    }

                                },
                                onDismiss = {
                                    onChangeLayer(
                                        !(openLayer[MediaOptionKey.PLAYBACK_SPEED] ?: false)
                                    )
                                    onChangeLayer(
                                        !(openLayer[MediaOptionKey.VIDEO_QUALITY] ?: false)
                                    )
                                }
                            )
                        }

                    }

                }
            }
        }
    }
}

