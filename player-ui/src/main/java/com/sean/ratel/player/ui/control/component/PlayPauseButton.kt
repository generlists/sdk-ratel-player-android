package com.sean.ratel.player.ui.control.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.ui.R

@Composable
fun PlayPauseButton(viewModel: PlayerViewModel) {

    val isPlaying = viewModel.isPlaying.collectAsState()
    val isPlayEnd = viewModel.isPlayEnd.collectAsState()


    val icon = if (isPlayEnd.value) {
        Icons.Default.Replay
    } else {
        if (isPlaying.value) Icons.Default.Pause else {
            Icons.Default.PlayArrow
        }

    }
    val contentDescription =
        if (isPlayEnd.value) {
            stringResource(R.string.playpause_button_replay)
        } else {
            if (isPlaying.value) stringResource(R.string.playpause_button_pause) else {
                stringResource(R.string.playpause_button_play)
            }
        }

    Icon(
        icon,
        contentDescription = contentDescription,
        modifier = Modifier
            .width(58.dp)
            .height(58.dp)
            .clickable {
                if (isPlayEnd.value) {
                    viewModel.rePlay(mediaIndex = 0)
                } else {
                    if (isPlaying.value) viewModel.pause() else {
                        viewModel.play()
                    }
                }
            },
        tint = Color.White
    )

}
