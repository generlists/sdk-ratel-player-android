package com.sean.ratel.player.ui.control.component.options

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.core.data.domain.model.ContentScale
import com.sean.ratel.player.core.data.domain.model.PlaySpeed
import com.sean.ratel.player.core.data.domain.model.Quality


@OptIn(UnstableApi::class)
@Composable
fun SelectControl(
    currentActionMenu: MediaOptionKey,
    currentSpeed: MediaOptionValue.Speed? = null,
    speeds: List<PlaySpeed>? = null,
    currentScale: MediaOptionValue.Scale? = null,
    scales: Array<ContentScale>? = null,
    currentQuality: MediaOptionValue.VideoQuality? = null,
    qualityList: List<Pair<Quality,String>>? = null,
    onSelected: (Pair<Quality,String>?,PlaySpeed?, ContentScale?) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth().padding(bottom = 16.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        SpeedSelectPanel(
            currentActionMenu = currentActionMenu,
            currentSpeed = currentSpeed,
            currentScale = currentScale,
            currentQuality = currentQuality,
            speeds = speeds,
            scales = scales,
            qualityList = qualityList,
            onSelected = onSelected
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun SpeedSelectPanel(
    currentActionMenu: MediaOptionKey,
    currentSpeed: MediaOptionValue.Speed?,
    currentScale: MediaOptionValue.Scale?,
    currentQuality: MediaOptionValue.VideoQuality?,
    speeds: List<PlaySpeed>?,
    scales: Array<ContentScale>?,
    qualityList: List<Pair<Quality, String>>?,
    onSelected: (Pair<Quality,String>?,PlaySpeed?, ContentScale?) -> Unit
) {


    Column(
        modifier = Modifier
            .background(
                color =MaterialTheme.colorScheme.outlineVariant,

                )
           // .padding(top = 20.dp, end = 20.dp)
            .clickable(enabled = false) {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        FlowRow(
            modifier = Modifier,
            itemVerticalAlignment=Alignment.CenterVertically,

            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentActionMenu) {
                MediaOptionKey.VIDEO_QUALITY -> {

                    qualityList?.forEach { quality ->

                        OptionsButton(
                            null,
                            null,
                            null,
                            null,
                            currentQuality = currentQuality,
                            quality = quality.first,
                            onClick = { onSelected(quality, null, null) }
                        )
                    }
                }
                MediaOptionKey.PLAYBACK_SPEED -> {

                    speeds?.forEach { speed ->

                        OptionsButton(
                            currentSpeed,
                            speed = speed.speed,
                            onClick = { onSelected(null,speed, null) }
                        )
                    }
                }

                MediaOptionKey.SCREEN_SCALE -> {
                    scales?.forEach { scale ->
                        OptionsButton(
                            null,
                            null,
                            currentScale,
                            scale = scale.scaleIndex,
                            onClick = { onSelected(null,null, scale) }
                        )
                    }
                }

                else -> Unit
            }

        }
    }
}

@Composable
private fun OptionsButton(
    currentSpeed: MediaOptionValue.Speed? = null,
    speed: Float? = null,
    currentScale: MediaOptionValue.Scale? = null,
    scale: Int? = null,
    currentQuality: MediaOptionValue.VideoQuality? = null,
    quality: Quality? = null,
    onClick: () -> Unit
) {

    val backgroundColor = when {
        (speed != null && speed == currentSpeed?.value?.speed) ||
                (scale != null && scale == currentScale?.value?.scaleIndex) ||
                (quality != null && quality == currentQuality?.quality?.first) -> MaterialTheme.colorScheme.primary

        else -> MaterialTheme.colorScheme.background

    }
    val text = when {
        speed != null-> "${speed}x"
        scale != null -> LocalContext.current.resources.getString(ContentScale.entries[scale].label)
        quality != null -> "${Quality.valueOf(quality.name)}${quality.screenSize}"
        else -> ""

    }

    val textColor = when {
        (speed != null && speed == currentSpeed?.value?.speed) ||
                (scale != null && scale == currentScale?.value?.scaleIndex) ||
                (quality != null && quality == currentQuality?.quality?.first) -> Color.Black

        else -> MaterialTheme.colorScheme.onPrimary

    }

    val fontWeight = when {
        (speed != null && speed == currentSpeed?.value?.speed) ||
        (scale != null && scale == currentScale?.value?.scaleIndex) ||
        (quality!=null &&      quality == currentQuality?.quality?.first) -> FontWeight.Bold
        else -> FontWeight.Normal

    }
    Box(

        modifier = Modifier.widthIn(75.dp,120.dp)
            .height(40.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier.wrapContentSize().padding(start = 7.dp,end=7.dp),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = fontWeight
        )
    }
}