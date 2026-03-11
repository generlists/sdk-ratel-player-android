package com.sean.ratel.player.ui.control.component.options

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.view.Window
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.player.ui.R
import dagger.hilt.android.internal.managers.FragmentComponentManager.findActivity
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionSeekControl(
    key: MediaOptionKey,
    isSystemVolumeMute: StateFlow<Boolean>,
    currentBrightnessValue: Float? = null,
    currentVolumeValue: Float? = null,
    brightVolumeDelta: (Float?, Float?) -> Unit
) {

    val context = LocalContext.current
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)


    var brightness by remember {
        mutableStateOf(currentBrightnessValue)
    }

    var volume by remember { mutableStateOf(currentVolumeValue) }
    val systemVolume by isSystemVolumeMute.collectAsState()
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentSize()
            .padding(bottom = 16.dp)
    ) {
        if (systemVolume && key == MediaOptionKey.SCREEN_VOLUME) {
            Text(
                text = stringResource(R.string.player_control_system_volume_zero),
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.error,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
            )
        }

        Box(
            Modifier.fillMaxWidth()
        ) {

            Slider(
                modifier = Modifier
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant), thumb = {

                Box(modifier = Modifier
                    .size(24.dp)
                    .drawBehind {
                        // 1. 회색 번짐(Shadow/Glow) 효과 그리기
                        drawCircle(
                            brush = Brush.radialGradient(
                                0.0f to Color.Gray.copy(alpha = 0.5f),
                                1.0f to Color.Transparent,
                                center = center,
                                radius = size.maxDimension / 1.5f
                            ), radius = size.maxDimension / 1.5f
                        )
                    }
                    .padding(3.dp) // 번짐 효과 안쪽으로 흰색 원 배치
                    .background(Color.White, CircleShape)
                    // 2. 아주 얇은 회색 테두리로 경계선 살짝 잡아주기
                    .border(0.5.dp, Color.LightGray, CircleShape),
                    contentAlignment = Alignment.Center) {
                    // 내부 비워둠
                }
            }, track = { sliderState ->

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                ) {
                    // 1. 배경 바
                    drawRoundRect(
                        color = color, size = size, cornerRadius = CornerRadius(size.height / 2)
                    )

                    // 2. 진행 바 (Blue) - 현재 값만큼만 채움

                    val progress =
                        (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                    drawRoundRect(
                        color = colors.primary,
                        size = size.copy(width = size.width * progress),
                        cornerRadius = CornerRadius(size.height / 2)
                    )
                }
            },

                steps = 0, value = when (key) {
                    MediaOptionKey.SCREEN_BRIGHT -> brightness ?: 0f
                    MediaOptionKey.SCREEN_VOLUME -> volume ?: 0f
                    else -> 0f
                }, onValueChange = { v ->

                    when (key) {
                        MediaOptionKey.SCREEN_BRIGHT -> {
                            brightness = v
                        }

                        MediaOptionKey.SCREEN_VOLUME -> {
                            volume = v
                        }

                        else -> Unit
                    }

                }, onValueChangeFinished = {

                    val context = findActivity(context)
                    when (key) {
                        MediaOptionKey.SCREEN_BRIGHT -> {

                            brightVolumeDelta(
                                changeBrightness(
                                    (context as Activity).window, context, brightness ?: 0f
                                ), null
                            )

                        }

                        MediaOptionKey.SCREEN_VOLUME -> {

                            val delta = ((volume ?: 0f) * 10).roundToInt() / 10.0f

                            brightVolumeDelta(
                                null, delta
                            )
                        }

                        else -> Unit
                    }
                }, valueRange = 0f..1f, colors = appSliderColors()

            )
        }
    }

}

@Composable
private fun appSliderColors(): SliderColors {
    val colors = MaterialTheme.colorScheme

    return SliderDefaults.colors(
        //Thumb (동그라미)
        thumbColor = Color.White,
        disabledThumbColor = Color.White.copy(alpha = 0.4f),

        // 진행된 영역
        activeTrackColor = colors.primary,
        activeTickColor = Color.Transparent,

        // 진행 안 된 영역 (딤 처리)
        inactiveTrackColor = colors.primary.copy(alpha = 0.3f),
        inactiveTickColor = Color.Transparent,

        // Disabled 상태
        disabledActiveTrackColor = colors.primary.copy(alpha = 0.4f),
        disabledInactiveTrackColor = colors.primary.copy(alpha = 0.2f),
        disabledActiveTickColor = Color.Transparent,
        disabledInactiveTickColor = Color.Transparent
    )
}

private fun getSystemBrightness01(context: Context): Float {
    return try {
        val b = Settings.System.getInt(
            context.contentResolver, Settings.System.SCREEN_BRIGHTNESS
        ) // 0..255
        (b / 255f).coerceIn(0f, 1f)
    } catch (_: Exception) {
        0.5f
    }
}


private fun applyWindowBrightness(activity: Activity?, value01: Float) {

    val act = activity ?: return
    val lp = act.window.attributes

    lp.screenBrightness = when {
        value01 <= 0.01f -> 0f        // 확실히 어둡게
        value01 >= 0.99f -> -1f       // 시스템 밝기 사용(여기가 포인트)
        else -> value01               // 중간만 window로
    }

    act.window.attributes = lp
}

private fun changeBrightness(
    window: Window, context: Context, brightnessDelta: Float
): Float {

    val params = window.attributes

    // 0.01 ~ 1.0 사이로 제한 (0이면 아예 꺼질 수 있어서 0.01 권장)
    val brightness = brightnessDelta.coerceIn(0.01f, 1f)

    params.screenBrightness = brightness
    window.attributes = params

    return (brightness * 10).roundToInt() / 10.0f

}

