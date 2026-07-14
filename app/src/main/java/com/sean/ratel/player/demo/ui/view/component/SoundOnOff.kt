package com.sean.ratel.player.demo.ui.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.cast.YouTubePlayersManager

@Composable
@Suppress("ktlint:standard:function-naming")
fun SoundOnOff(
    youTubePlayersManager: YouTubePlayersManager,
    mainViewModel: MainViewModel,
) {
    var isSoundOnOff by remember { mainViewModel.mutePlay }
    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 5.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.LightGray),
        ) {
            Text(
                stringResource(R.string.sound_on_off),
                Modifier
                    .wrapContentSize()
                    .padding(start = 5.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(top = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            youTubePlayersManager.setMute(isMute = isSoundOnOff)
            OnOffRadioButton(
                isOn = isSoundOnOff,
                onToggle = { isSoundOnOff = it },
            )
        }
    }
}
