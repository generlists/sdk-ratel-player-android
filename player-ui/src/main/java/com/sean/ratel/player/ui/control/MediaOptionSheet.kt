@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.sean.ratel.player.ui.control

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.BurstMode
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOneOn
import androidx.compose.material.icons.filled.RunCircle
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material.icons.outlined.BurstMode
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.PictureInPicture
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.RunCircle
import androidx.compose.material.icons.outlined.SwitchVideo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.core.data.domain.model.ContentScale
import com.sean.ratel.player.core.data.domain.model.PlaySpeed
import com.sean.ratel.player.core.data.domain.model.PreviewInfoData
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.core.data.domain.model.RepeatMode
import com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.ui.MediaOptions
import com.sean.ratel.player.ui.R
import com.sean.ratel.player.ui.ThemeMode
import com.sean.ratel.player.ui.control.component.options.MediaOptionKey
import com.sean.ratel.player.ui.control.component.options.MediaOptionValue
import com.sean.ratel.player.ui.control.component.options.OptionItem
import com.sean.ratel.player.ui.control.component.options.OptionSection
import kotlinx.coroutines.flow.StateFlow

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun MediaOptionSheet(
    playerViewModel: PlayerViewModel,
    themeMode: ThemeMode,
    mediaOptions: StateFlow<MediaOptions>,
    onOptionChanged: (MediaOptionKey, MediaOptionValue) -> Unit,
    onInfo: (PreviewInfoData?) -> Unit,
    onDismiss: () -> Unit,
) {
    val videoQuality by playerViewModel.videoQualityChanged.collectAsState()

    val qualityList by playerViewModel.qualityList.collectAsState()

    val mediaOptions by mediaOptions.collectAsState()

    val isHWYAccelerated by playerViewModel.isHWYAccelerated.collectAsState()
    val context = LocalContext.current
    val captureFailString = stringResource(R.string.player_control_capture_audio_fail)

    var openLayer by remember {
        mutableStateOf(
            mapOf(
                MediaOptionKey.VIDEO_QUALITY to false,
                MediaOptionKey.PLAYBACK_REPEAT to false,
                MediaOptionKey.SHUFFLE to false,
                MediaOptionKey.PLAYBACK_SPEED to false,
                MediaOptionKey.SCREEN_SCALE to false,
                MediaOptionKey.SCREEN_BRIGHT to false,
                MediaOptionKey.SCREEN_VOLUME to false,
                MediaOptionKey.SCREEN_PIP to false,
            ),
        )
    }

    val playOptionItems =
        listOf(
            OptionItem(
                key = MediaOptionKey.VIDEO_QUALITY,
                value = MediaOptionValue.VideoQuality(videoQuality),
                icon = Icons.Outlined.SwitchVideo,
                label = String.format(stringResource(R.string.player_control_quality), videoQuality.first), // "배속 설정(${playSpeed.value.speed}x)",
                enabled = true,
                locked = false,
            ) {
                openLayer =
                    openLayer.mapValues { (key, value) ->
                        if (key == MediaOptionKey.VIDEO_QUALITY) {
                            !value
                        } else {
                            false
                        }
                    }
            },
            OptionItem(
                key = MediaOptionKey.PLAYBACK_REPEAT,
                value = MediaOptionValue.Repeat(mediaOptions.repeatMode),
                icon = repeatModeIcon(mediaOptions.repeatMode),
                label = stringResource(R.string.player_control_repeat),
                enabled = true,
                locked = false,
            ) {
                openLayer =
                    openLayer.mapValues { (key, value) ->
                        if (key == MediaOptionKey.PLAYBACK_REPEAT) {
                            !value
                        } else {
                            false
                        }
                    }

                val repeatMode = playerViewModel.getRepeatToggleSequence()
                playerViewModel.setRepeatMode(repeatMode)

                onOptionChanged(
                    MediaOptionKey.PLAYBACK_REPEAT,
                    MediaOptionValue.Repeat(repeatMode),
                )
            },
            OptionItem(
                key = MediaOptionKey.PLAYBACK_SPEED,
                value = MediaOptionValue.Speed(mediaOptions.speed),
                icon = if (mediaOptions.speed == PlaySpeed.PlaySpeed_1_0) Icons.Outlined.RunCircle else Icons.Filled.RunCircle,
                label = String.format(stringResource(R.string.player_control_speed), mediaOptions.speed.speed),
                enabled = true,
                locked = false,
            ) {
                openLayer =
                    openLayer.mapValues { (key, value) ->
                        if (key == MediaOptionKey.PLAYBACK_SPEED) {
                            !value
                        } else {
                            false
                        }
                    }
            },
            OptionItem(
                key = MediaOptionKey.SHUFFLE,
                value = MediaOptionValue.Toggle(mediaOptions.shuffle.enabled),
                icon = if (mediaOptions.shuffle.enabled) Icons.Default.ShuffleOn else Icons.Default.Shuffle,
                label = stringResource(R.string.player_control_suffle),
                enabled = true,
                locked = false,
            ) {
                playerViewModel.setShuffle(!mediaOptions.shuffle.enabled)

                openLayer =
                    openLayer.mapValues { (key, value) ->
                        if (key == MediaOptionKey.SHUFFLE) {
                            !value
                        } else {
                            false
                        }
                    }

                onOptionChanged(
                    MediaOptionKey.SHUFFLE,
                    MediaOptionValue.Toggle(!mediaOptions.shuffle.enabled),
                )
            },
            OptionItem(
                key = MediaOptionKey.HARDWARE_ACCEL,
                value = MediaOptionValue.Toggle(false),
                icon = if (false) Icons.Filled.BurstMode else Icons.Outlined.BurstMode,
                label = stringResource(R.string.player_control_hw_accelate),
                enabled = false,
                locked = true,
            ) {
                playerViewModel.setHWAccelerated(!isHWYAccelerated)

                onOptionChanged(
                    MediaOptionKey.HARDWARE_ACCEL,
                    MediaOptionValue.Toggle(isHWYAccelerated),
                )
            },
        )

    val screenOptionItems =
        listOf(
            OptionItem(
                key = MediaOptionKey.SCREEN_BRIGHT,
                value = MediaOptionValue.FloatValue(mediaOptions.screenBrightness.value),
                icon =
                    when {
                        mediaOptions.screenBrightness.value <= 0f -> Icons.Filled.BrightnessLow
                        mediaOptions.screenBrightness.value < 0.5f -> Icons.Filled.BrightnessMedium
                        else -> Icons.Filled.BrightnessHigh
                    },
                label =
                    when {
                        mediaOptions.screenBrightness.value <= 0f -> stringResource(R.string.player_control_brightness_low)
                        mediaOptions.screenBrightness.value < 0.7f -> stringResource(R.string.player_control_brightness_middle)
                        else -> stringResource(R.string.player_control_brightness_high)
                    },
                enabled = true,
                locked = false,
            ) {
                openLayer =
                    openLayer.mapValues { (key, value) ->
                        if (key == MediaOptionKey.SCREEN_BRIGHT) {
                            !value
                        } else {
                            false
                        }
                    }
            },
            OptionItem(
                key = MediaOptionKey.SCREEN_VOLUME,
                value = MediaOptionValue.FloatValue(mediaOptions.volume.value),
                icon =
                    when {
                        mediaOptions.volume.value == 0f -> Icons.AutoMirrored.Default.VolumeOff
                        mediaOptions.volume.value < 0.3f -> Icons.AutoMirrored.Default.VolumeMute
                        mediaOptions.volume.value > 0.3f && mediaOptions.volume.value > 0.7f -> Icons.AutoMirrored.Default.VolumeDown
                        else -> Icons.AutoMirrored.Default.VolumeUp
                    },
                label =
                    when {
                        mediaOptions.volume.value == 0f -> stringResource(R.string.player_control_volume_off)
                        mediaOptions.volume.value < 0f -> stringResource(R.string.player_control_volume_low)
                        mediaOptions.volume.value < 0.7f -> stringResource(R.string.player_control_volume_middle)
                        else -> stringResource(R.string.player_control_volume_high)
                    },
                enabled = true,
                locked = false,
            ) {
                // volumeDelta
                openLayer =
                    openLayer.mapValues { (key, value) ->
                        if (key == MediaOptionKey.SCREEN_VOLUME) {
                            !value
                        } else {
                            false
                        }
                    }

                onOptionChanged(
                    MediaOptionKey.SCREEN_VOLUME,
                    MediaOptionValue.FloatValue(mediaOptions.volume.value),
                )
            },
            OptionItem(
                key = MediaOptionKey.SCREEN_CAPTURE,
                value = MediaOptionValue.Toggle(true),
                icon = Icons.Default.CropFree,
                label = stringResource(R.string.player_control_screen_shot),
                enabled = true,
                locked = false,
            ) {
                if (videoQuality.first == Quality.AUDIO) {
                    Toast.makeText(context, captureFailString, Toast.LENGTH_LONG).show()
                    onDismiss()
                    return@OptionItem
                }
                playerViewModel.getScreenCapture(onInfo = { info ->
                    run {
                        onInfo(info)
                    }
                }, speed = mediaOptions.speed)
                onDismiss()
            },
            OptionItem(
                key = MediaOptionKey.SCREEN_SCALE,
                value =
                    MediaOptionValue.Scale(mediaOptions.contentScale),
                icon = Icons.Default.AspectRatio,
                label =
                    String.format(
                        stringResource(R.string.player_control_screen_size),
                        stringResource(mediaOptions.contentScale.label),
                    ),
                enabled = true,
                locked = false,
            ) {
                openLayer =
                    openLayer.mapValues { (key, value) ->
                        if (key == MediaOptionKey.SCREEN_SCALE) {
                            !value
                        } else {
                            false
                        }
                    }

                onOptionChanged(
                    MediaOptionKey.SCREEN_SCALE,
                    MediaOptionValue.Scale(
                        mediaOptions.contentScale,
                    ),
                )
            },
            OptionItem(
                key = MediaOptionKey.SCREEN_PIP,
                value = MediaOptionValue.Toggle(mediaOptions.pip.enabled),
                icon = if (mediaOptions.pip.enabled) Icons.Filled.PictureInPicture else Icons.Outlined.PictureInPicture,
                label = stringResource(R.string.player_control_pip),
                enabled = true,
                locked = false,
            ) {
                //playerViewModel.setIsPIP(!mediaOptions.pip.enabled)

                openLayer =
                    openLayer.mapValues { (key, value) ->
                        if (key == MediaOptionKey.SCREEN_PIP) {
                            !value
                        } else {
                            false
                        }
                    }

                onOptionChanged(
                    MediaOptionKey.SCREEN_PIP,
                    MediaOptionValue.Toggle(!mediaOptions.pip.enabled),
                )
            },
        )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
        dragHandle = null,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                OptionSection(
                    title = stringResource(R.string.player_control_options),
                    themeMode = themeMode,
                    isSystemVolumeMute = playerViewModel.isSystemVolumeMute,
                    items = playOptionItems,
                    openLayer = openLayer,
                    speeds = playerViewModel.speedSelection,
                    qualityList = qualityList,
                    scaleOptions = ContentScale.entries.toTypedArray(),
                    onChangeLayer = { b ->
                        openLayer = openLayer + (MediaOptionKey.PLAYBACK_SPEED to b)

                        openLayer = openLayer + (MediaOptionKey.VIDEO_QUALITY to b)

                        onDismiss()
                    },
                    onSelect = { quality, speed, _ ->

                        quality?.let {
                            playerViewModel.setVideoQualityChanged(
                                qualityChanged = true,
                                videoQuality = Pair(it.first, it.second),
                            )

                            onOptionChanged(
                                MediaOptionKey.VIDEO_QUALITY,
                                MediaOptionValue.VideoQuality(Pair(it.first, it.second)),
                            )
                        }

                        speed?.let {
                            playerViewModel.setPlaySpeedMode(speed)
                            onOptionChanged(
                                MediaOptionKey.PLAYBACK_SPEED,
                                MediaOptionValue.Speed(speed),
                            )
                        }
                    },
                )

                OptionSection(
                    title = stringResource(R.string.player_control_screen_options),
                    themeMode = themeMode,
                    isSystemVolumeMute = playerViewModel.isSystemVolumeMute,
                    items = screenOptionItems,
                    openLayer = openLayer,
                    speeds = playerViewModel.speedSelection,
                    scaleOptions = ContentScale.entries.toTypedArray(),
                    onChangeLayer = { b ->
                        openLayer = openLayer + (MediaOptionKey.SCREEN_SCALE to b)
                    },
                    onSelect = { _, _, scale ->

                        onOptionChanged(
                            MediaOptionKey.SCREEN_SCALE,
                            MediaOptionValue.Scale(scale ?: ContentScale.FillHeight),
                        )
                    },
                    brightSoundDelta = { brightness, volume ->
                        brightness?.let {
                            onOptionChanged(
                                MediaOptionKey.SCREEN_BRIGHT,
                                MediaOptionValue.FloatValue(it),
                            )
                        }
                        volume?.let {
                            // 플레이어로
                            playerViewModel.setVolume(it)
                            onOptionChanged(
                                MediaOptionKey.SCREEN_VOLUME,
                                MediaOptionValue.FloatValue(it),
                            )
                        }
                    },
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun repeatModeIcon(repeatMode: RepeatMode): ImageVector =
    when (repeatMode) {
        RepeatMode.REPEAT_OFF -> Icons.Default.Repeat
        RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOneOn
        else -> Icons.Default.RepeatOn
    }

@Composable
private fun repeatModeContentDescription(repeatMode: RepeatMode): String =
    when (repeatMode) {
        RepeatMode.REPEAT_OFF -> stringResource(R.string.repeat_button_repeat_off)
        RepeatMode.REPEAT_ONE -> stringResource(R.string.repeat_button_repeat_one)
        else -> stringResource(R.string.repeat_button_repeat_all)
    }
