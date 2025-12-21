package com.sean.ratel.player.demo.ui.screen

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.ui.control.EndPlayer
import dagger.hilt.android.UnstableApi
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@OptIn(UnstableApi::class)
@Composable
fun EndPlayerScreen(
    urls: String,
    startIndex: Int,
    viewModel: PlayerViewModel = hiltViewModel()
) {

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val urlDecodeList = (URLDecoder.decode(urls, StandardCharsets.UTF_8.toString())).split(",")

        EndPlayer(Modifier, urlDecodeList, startIndex) { onclick ->
            MediaScreenControlView(Modifier, viewModel, onclick)
        }
    }

}