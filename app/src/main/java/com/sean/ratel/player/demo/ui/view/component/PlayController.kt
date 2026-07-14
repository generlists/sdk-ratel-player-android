package com.sean.ratel.player.demo.ui.view.component

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.ui.cast.CastSessionState
import com.sean.ratel.player.demo.ui.cast.YouTubePlayersManager

@Composable
@Suppress("ktlint:standard:function-naming")
fun PlayController(
    mainViewModel: MainViewModel,
    youTubePlayersManager: YouTubePlayersManager,
    index: Int,
    videoList: ArrayList<String>?,
    playIndex: (Int) -> Unit,
) {
    val context = LocalContext.current
    val isConnect by youTubePlayersManager.castEventManager.castSession.collectAsStateWithLifecycle(
        initialValue = CastSessionState.SessionUnKnown,
    )
    val castModel by mainViewModel.youtubeGoogleCastModelList.collectAsStateWithLifecycle()
    val playEvent by youTubePlayersManager.castEventManager.playEvents.collectAsStateWithLifecycle()
    val castPlayState by youTubePlayersManager.castPlayState.collectAsStateWithLifecycle()
    val currentIndex by youTubePlayersManager.currentIndex.collectAsStateWithLifecycle()
    val isMute by youTubePlayersManager.mute.collectAsStateWithLifecycle()
    val playSpeed by youTubePlayersManager.currentPlaySpeed.collectAsStateWithLifecycle()
    val captionOnOff by youTubePlayersManager.captionOnOff.collectAsStateWithLifecycle()

    Log.d("PlayController", "captionOnOff=$captionOnOff")
    Scaffold(
        topBar = {},
    ) { innerPadding ->

        Box(
            Modifier
                .fillMaxWidth()
                .padding(innerPadding),
        ) {
            Column(
                Modifier
                    .wrapContentSize()
                    .align(Alignment.TopStart)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DebugState(mainViewModel)
                PervNextPlay(context, videoList, youTubePlayersManager, index, playIndex)
                SpeedPlay(youTubePlayersManager)
                SoundOnOff(youTubePlayersManager, mainViewModel)
                if (isConnect is CastSessionState.SessionStart) {
                    val videoList =
                        castModel
                            ?.videoList
                            ?.get(currentIndex)
                    Log.d("KKSSSSS###", "$videoList , index : $currentIndex")

                    CastControlBar(
                        playerManager = youTubePlayersManager,
                        title =
                            castModel
                                ?.videoList
                                ?.get(currentIndex)
                                ?.videoModel
                                ?.title ?: "",
                        thumbNail =
                            castModel
                                ?.videoList
                                ?.get(index)
                                ?.videoModel
                                ?.thumbNail ?: "",
                        onPlayPause = {
                            youTubePlayersManager.castPlay(castPlayState == YouTubeStreamPlaybackState.Playing)
                        },
                        onPrevious = {
                            youTubePlayersManager.castPrevPlay()
                        },
                        onNext = {
                            youTubePlayersManager.castNextPlay()
                        },
                        onToggleMute = {
                            youTubePlayersManager.setCasetMute(!isMute)
                        },
                        onToggleSubtitle = {
                            youTubePlayersManager.setCaptionOnOff(!captionOnOff)
                        },
                        onSpeedUp = {
                            youTubePlayersManager.speedUp()
                        },
                        onSpeedDown = {
                            youTubePlayersManager.speedDown()
                        },
                    )
                }
            }
        }
    }
}
