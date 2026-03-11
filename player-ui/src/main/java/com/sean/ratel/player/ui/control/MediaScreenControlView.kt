package com.sean.ratel.player.ui.control


import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.ui.control.component.CustomSeekBar
import com.sean.ratel.player.ui.control.component.NextButton
import com.sean.ratel.player.ui.control.component.PlayPauseButton
import com.sean.ratel.player.ui.control.component.PreviousButton
import com.sean.ratel.player.ui.control.component.SeekBackButton
import com.sean.ratel.player.utils.log.formatTimeFromFloat


@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun MediaScreenControlView(
    modifier: Modifier,
    viewModel: PlayerViewModel
) {

    Box(
        modifier
            .fillMaxSize()
           .background(Color.Transparent)
    ) {

        BottomSeekBarArea(modifier, viewModel)
    }
}


@Suppress("ktlint:standard:function-naming")
@Composable
fun BottomSeekBarArea(modifier: Modifier, viewModel: PlayerViewModel) {

    val currentTime by viewModel.currentTimeMs.collectAsState()
    val duration by viewModel.durationMs.collectAsStateWithLifecycle()


    val progressRate =
        if (duration == 0L) 0f else (currentTime.toFloat() / duration.toFloat())

    var progress by remember { mutableFloatStateOf(0.0f) } // 초기값 30%
    LaunchedEffect(progressRate) {
        progress = progressRate
    }


    Column(
        Modifier
            .fillMaxSize(), verticalArrangement = Arrangement.Bottom
    ) {

        TimeArea(duration, currentTime)
        CustomSeekBar(
            progress = progress,
            onSeekPreview = { newProgress ->
                viewModel.pause()
                viewModel.isSeek(true)
                progress = newProgress
            },

                onSeekCommit = { finalProgress ->

                    progress = finalProgress

                    if (duration > 0L) {
                        val seekMs = (finalProgress * duration).toLong()
                        viewModel.seekTo(seekMs)
                        viewModel.play()

                        viewModel.isSeek(false)
                    }
                },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentSize()
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp)
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SeekBackButton(viewModel)
            Spacer(Modifier.width(5.dp))
            PreviousButton(viewModel)
            Spacer(Modifier.width(10.dp))
            PlayPauseButton(viewModel)
            Spacer(Modifier.width(10.dp))
            NextButton(viewModel)
            Spacer(Modifier.width(5.dp))
            SeekForwardButton(viewModel)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun TimeArea(duration: Long, currentTime: Long) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 10.dp),
    ) {
        Text(
            text = if (currentTime > 0.0f) formatTimeFromFloat(currentTime.toFloat()) else "",
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
            text = if (currentTime > 0.0f) "/" else "",
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
            text = if (currentTime > 0.0f) formatTimeFromFloat(duration.toFloat()) else "00:00",
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
