package com.sean.ratel.player.demo.ui.screen

import PlayPauseButton
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.CONTENT_SCALES
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel

import com.sean.ratel.player.core.com.sean.ratel.player.core.ui.control.CustomSeekBar
import com.sean.ratel.player.core.com.sean.ratel.player.core.ui.control.MoreButton
import com.sean.ratel.player.core.com.sean.ratel.player.core.ui.control.NextButton
import com.sean.ratel.player.core.com.sean.ratel.player.core.ui.control.PlaybackSpeedPopUpButton
import com.sean.ratel.player.core.com.sean.ratel.player.core.ui.control.PreviousButton
import com.sean.ratel.player.core.com.sean.ratel.player.core.ui.control.RepeatButton
import com.sean.ratel.player.core.com.sean.ratel.player.core.ui.control.SeekBackButton
import com.sean.ratel.player.core.com.sean.ratel.player.core.ui.control.SeekForwardButton
import com.sean.ratel.player.core.com.sean.ratel.player.core.ui.control.ShuffleButton
import dagger.hilt.android.UnstableApi
import java.util.Locale


@OptIn(UnstableApi::class)
@Composable
fun MediaScreenControlView(
    modifier: Modifier,
    viewModel: PlayerViewModel,
    onClick:()->Unit
) {
    var showControls  by remember {viewModel.showControls}
    var currentContentScaleIndex by remember { viewModel.currentContentScaleIndex }
    val scale = viewModel.getCurrentContentScaleIndex(currentContentScaleIndex)

    Box(modifier
        .fillMaxSize()
        .background(Color.Transparent)
        .clickable {
            showControls = !showControls
        }) {

        MoreButton(viewModel)

        MinimalControls(
            viewModel,
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.Transparent)
        )

        BottomSeekBarArea(modifier, viewModel)

        Button(
            onClick = {
                currentContentScaleIndex = currentContentScaleIndex.inc() % CONTENT_SCALES.size
                onClick()
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
        ) {
            Text("ContentScale is ${CONTENT_SCALES[currentContentScaleIndex].first}")
        }

    }
}
@Composable
internal fun MinimalControls(viewModel: PlayerViewModel, modifier: Modifier = Modifier) {

    Column(Modifier
        .fillMaxWidth()
        .fillMaxHeight(), verticalArrangement = Arrangement.Center) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), horizontalArrangement = Arrangement.Center) {
            Spacer(Modifier.width(10.dp))
            SeekBackButton(viewModel)
            PreviousButton(viewModel)
            Spacer(Modifier.width(10.dp))
            PlayPauseButton(viewModel)
            Spacer(Modifier.width(10.dp))
            SeekForwardButton(viewModel)
            NextButton(viewModel)
            Spacer(Modifier.width(10.dp))
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), horizontalArrangement = Arrangement.Center) {
            ShuffleButton(viewModel)
            RepeatButton(viewModel)
            PlaybackSpeedPopUpButton(viewModel)
        }

    }


}

@Suppress("ktlint:standard:function-naming")
@Composable
fun BottomSeekBarArea(modifier: Modifier,viewModel: PlayerViewModel) {

    val currentTime by viewModel.currentTimeMs.collectAsState()
    val duration by viewModel.durationMs.collectAsStateWithLifecycle()

    val progressRate =
        if (duration == 0L) 0f else (currentTime.toFloat() / duration.toFloat()) // kotlin.math.ceil((currentTime / duration))

    var progress by remember { mutableFloatStateOf(0.0f) } // 초기값 30%
    LaunchedEffect(progressRate) {
        progress = progressRate
    }
    Box(Modifier
        .fillMaxSize()
        .padding(bottom = 50.dp), contentAlignment = Alignment.BottomStart){
        TimeArea(duration,currentTime )
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter,
    ) {
        CustomSeekBar(
            progress = progress,
            onSeekPreview = { newProgress ->
                viewModel.pause()
                progress = newProgress
            },

            onSeekCommit = { finalProgress ->

                progress = finalProgress

                if (duration > 0L) {
                    val seekMs = (finalProgress * duration).toLong()
                    viewModel.play()
                    viewModel.seekTo(seekMs)
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentSize()
                    .padding(start = 10.dp, end = 10.dp, bottom = 20.dp),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun TimeArea(duration: Long, currentTime: Long) {
    Log.d("OKJSP","durtiondurtiondurtiondurtiondurtion : ${duration}")
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 10.dp),
    ) {
        Text(
            text =if (currentTime > 0.0f) formatTimeFromFloat(currentTime.toFloat()) else "",
            Modifier
                .wrapContentSize()
                .padding(start = 15.dp, top = 5.dp),
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = if (LocalInspectionMode.current) Color.Black else Color.White,
        )
        Text(
            text =if (currentTime > 0.0f) "/" else "",
            Modifier
                .wrapContentSize()
                .padding(start = 5.dp, top = 5.dp),
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = if (LocalInspectionMode.current) Color.Black else Color.White,
        )
        Text(
            text =if (currentTime > 0.0f) formatTimeFromFloat(duration.toFloat()?:0f) else "",
            Modifier
                .wrapContentSize()
                .padding(start = 5.dp, top = 5.dp),
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = if (LocalInspectionMode.current) Color.Black else Color.White,
        )
    }
}

fun formatTimeFromFloat(floatTime: Float): String {
    // 소숫점을 올림 처리
    val totalSeconds = kotlin.math.ceil(floatTime).toInt()/1000

    // 분과 초로 변환
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}