package com.sean.ratel.player.demo.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.ui.download.VideoDownloadViewModel

@Composable
@Suppress("ktlint:standard:function-naming")
fun DownLoadSample(
    mainViewModel: MainViewModel,
    viewModel: VideoDownloadViewModel,
) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(10.dp))
        DownloadPixaBay(
            mainViewModel,
            viewModel,
        )
        viewModel.loadSampleData()
    }
}
