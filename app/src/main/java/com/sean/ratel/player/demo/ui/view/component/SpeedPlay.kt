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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.cast.YouTubePlayersManager
import com.sean.ratel.player.demo.ui.view.CastPlayerFragment.Companion.speedList

@Composable
@Suppress("ktlint:standard:function-naming")
fun SpeedPlay(youTubePlayersManager: YouTubePlayersManager) {
    var selectedItem by remember { mutableStateOf(YouTubeStreamPlaybackRate.RATE_1) }
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
                stringResource(R.string.speed_play),
                Modifier
                    .wrapContentSize()
                    .padding(start = 5.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(
            Modifier
                .wrapContentWidth()
                .height(70.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SelectBox(
                speedList,
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it },
                modifier =
                    Modifier
                        .wrapContentSize()
                        .padding(16.dp),
            )
            // youTubePlayersManager.setPlaybackRate(selectedItem)
        }
    }
}
