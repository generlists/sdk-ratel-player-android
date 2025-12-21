package com.sean.ratel.player.ui.control

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.core.data.domain.model.PlaybackState


@OptIn(UnstableApi::class)
@Composable
fun EndPlayer(
    modifier: Modifier = Modifier,
    urls: List<String>,
    startIndex:Int,
    viewModel: PlayerViewModel = hiltViewModel(),
    content: @Composable BoxScope.(onClick:()->Unit) -> Unit
) {
    //https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8
    val context = LocalContext.current
    var currentContentScaleIndex by remember { viewModel.currentContentScaleIndex }

    val playerView = remember {  PlayerView(context).apply {
        useController = false
        resizeMode = viewModel.getCurrentContentScaleIndex(currentContentScaleIndex)
        viewModel.mediaStreamPlayer.start(
            items =urls.map { viewModel.mediaItem(it, it) },
            startIndex = startIndex)
        this.player = viewModel.mediaStreamPlayer.getPlayer()

        viewModel.setDuration()
        viewModel.setCurrentTime()
    } }

    var showControls by remember { viewModel.showControls}

    // PlayerView를 Compose에 삽입
    AndroidView(
        modifier = modifier.clickable{
            showControls = !showControls
        },

        factory = { _ ->playerView },
        update = { view ->
           if (view.player !== viewModel.mediaStreamPlayer.getPlayer()) {
                view.player = viewModel.mediaStreamPlayer.getPlayer()
            }
        }
    )

    if (showControls) {
        Box(Modifier.fillMaxSize()) {
            content {
                playerView.resizeMode = currentContentScaleIndex
            }
        }
    }

    // 화면에서 내려갈 때 해제
    DisposableEffect(Unit) {
        onDispose {
            viewModel.mediaStreamPlayer.release()

        }
    }

    LaunchedEffect(viewModel.mediaStreamPlayer.playbackState) {
        viewModel.mediaStreamPlayer.playbackState.collect { state ->
            when (state) {
                is PlaybackState.Prepared -> {}
                is PlaybackState.Playing -> {
                    viewModel.setPlaying(isPlaying = true)
                }

                else -> {
                    viewModel.setPlaying(isPlaying = false)
                }
            }
        }
    }
}