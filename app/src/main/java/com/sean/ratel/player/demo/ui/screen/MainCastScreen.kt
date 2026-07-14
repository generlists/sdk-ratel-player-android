package com.sean.ratel.player.demo.ui.screen

import android.util.Log
import android.view.LayoutInflater
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.mediarouter.app.MediaRouteButton
import androidx.mediarouter.media.MediaRouteSelector
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastButtonFactory
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.cast.CastSessionState
import com.sean.ratel.player.demo.ui.cast.YouTubePlayersManager
import com.sean.ratel.player.demo.ui.navigation.Destination
import com.sean.ratel.player.demo.ui.theme.Background_op_20

@Composable
@Suppress("ktlint:standard:function-naming")
fun MainCastScreen(
    mainViewModel: MainViewModel,
    youTubePlayersManager: YouTubePlayersManager,
) {
    val isConnected by youTubePlayersManager.castEventManager.castSession.collectAsStateWithLifecycle(CastSessionState.SessionUnKnown)
    val videoList by mainViewModel.youtubeGoogleCastModelList.collectAsStateWithLifecycle()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(360.dp),
        // .padding(16.dp),
    ) {
        val context = LocalContext.current

        Text(
            text =
                if (isConnected is CastSessionState.SessionStart) {
                    "🟢 Connected(${(isConnected as CastSessionState.SessionStart).castDevice})"
                } else {
                    "🔴 Not Connected"
                },
            Modifier.padding(12.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            contentAlignment = Alignment.CenterStart,
        ) {
            AndroidView(
                factory = { context ->
                    try {
                        val button =
                            LayoutInflater
                                .from(context)
                                .inflate(R.layout.cast_button, null) as MediaRouteButton
                        CastButtonFactory.setUpMediaRouteButton(context, button)
                        button.setBackgroundColor(android.graphics.Color.WHITE)
                        val selector =
                            MediaRouteSelector
                                .Builder()
                                .addControlCategory(CastMediaControlIntent.categoryForCast("07130C4B"))
                                .build()

                        button.routeSelector = selector
                        button
                    } catch (e: Throwable) {
                        Log.e("CastScreen", "root cause: ${e.cause?.cause ?: e.cause ?: e}", e)
                        throw e
                    }
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(start = 7.dp, end = 7.dp),
        ) {
            items(count = videoList?.videoList?.size ?: 0) { index ->
                val item = videoList?.videoList
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Background_op_20)
                        .height(60.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    videoList?.videoList?.get(index)?.let {
                        Text(
                            it.videoModel.title,
                            Modifier.weight(0.7f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(0.3f)
                            .height(60.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Button(onClick = {
                            mainViewModel.navigator.navigateTo(
                                Destination.CastPlayer.dynamicRoute(
                                    contentId = item?.get(index)?.videoModel?.videoId ?: "",
                                    startIndex = index,
                                ),
                            )
                        }) {
                            Text(stringResource(R.string.play))
                        }
                    }
                }
                Box(Modifier.height(1.dp).background(Color.Gray))
            }
        }
    }
}
