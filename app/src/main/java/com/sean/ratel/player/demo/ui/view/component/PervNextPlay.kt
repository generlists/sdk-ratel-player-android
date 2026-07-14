package com.sean.ratel.player.demo.ui.view.component

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.cast.YouTubePlayersManager

@Composable
@Suppress("ktlint:standard:function-naming")
fun PervNextPlay(
    context: Context,
    videoList: List<String>?,
    youTubePlayersManager: YouTubePlayersManager,
    index: Int,
    currentIndex: (Int) -> Unit,
) {
    var currentIndex = index
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
                stringResource(R.string.video_move),
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
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = {
                val playIndex = currentIndex - 1
                if (playIndex >= 0 && playIndex < (videoList?.size ?: 0)) {
                    videoList
                        ?.get(playIndex)
                        ?.let { youTubePlayersManager.loadVideo(it, 0f) }
                    currentIndex -= 1
                    currentIndex(currentIndex)
                } else {
                    Toast.makeText(context, R.string.first_video, Toast.LENGTH_SHORT).show()
                }
            }, Modifier.padding(start = 10.dp, end = 10.dp)) {
                Text(stringResource(R.string.prev_play))
            }
            Button(onClick = {
                val playIndex = currentIndex + 1
                if (playIndex < (videoList?.size ?: 0)) {
                    videoList
                        ?.get(playIndex)
                        ?.let { youTubePlayersManager.loadVideo(it, 0f) }
                    currentIndex += 1
                    currentIndex(currentIndex)
                } else {
                    Toast.makeText(context, R.string.last_video, Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(stringResource(R.string.next_play))
            }
        }
    }
}
