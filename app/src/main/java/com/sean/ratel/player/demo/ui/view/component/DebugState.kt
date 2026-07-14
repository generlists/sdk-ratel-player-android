package com.sean.ratel.player.demo.ui.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.R
import java.util.Locale

@Composable
@Suppress("ktlint:standard:function-naming")
fun DebugState(mainViewModel: MainViewModel) {
    val playstate = mainViewModel.playState.collectAsState()
    val currentTime = mainViewModel.currentTime.collectAsState()
    val duration = mainViewModel.duration.collectAsState()

    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.LightGray),
        ) {
            Text(
                stringResource(R.string.play_state),
                Modifier
                    .wrapContentSize()
                    .padding(start = 5.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                playstate.value,
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 5.dp, end = 5.dp, top = 5.dp),
                color = Color.White,
            )
            Text(
                String.format("currentTime :  %s", formatSecondsToTime(currentTime.value)),
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 5.dp, end = 5.dp),
                color = Color.White,
            )
            Text(
                String.format("duration :  %s", formatSecondsToTime(duration.value)),
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
                color = Color.White,
            )
        }
    }
}

fun formatSecondsToTime(seconds: Float): String {
    val totalSeconds = seconds.toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60

    return String.format(Locale.KOREA, "%02d:%02d:%02d", hours, minutes, secs)
}
