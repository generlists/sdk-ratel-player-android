package com.sean.ratel.player.demo.ui.screen

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sean.ratel.player.core.data.domain.model.DownloadInfo
import com.sean.ratel.player.core.data.domain.model.DownloadState
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.data.download.domain.DownloadBland
import com.sean.ratel.player.demo.ui.download.VideoDownloadViewModel
import com.sean.ratel.player.demo.ui.navigation.Destination
import dagger.hilt.android.UnstableApi

@OptIn(UnstableApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun DownloadPixaBay(
    mainViewModel: MainViewModel,
    viewModel: VideoDownloadViewModel,
) {
    val buttonClick = remember { mutableStateOf<String>("테스트 영상") }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentSize(),
        ) {
            Box(
                Modifier
                    .wrapContentSize()
                    .weight(1.0f),
            ) {
                Button(onClick = {
                    buttonClick.value = "테스트 영상"
                }, Modifier.padding(5.dp)) {
                    Text("테스트 영상")
                }
            }
            Box(
                Modifier
                    .wrapContentSize()
                    .weight(1.0f),
            ) {
                Button(onClick = {
                    buttonClick.value = "다운로드한 목록"
                }, Modifier.padding(5.dp)) {
                    Text("다운로드한 목록")
                }
            }
        }

        if (buttonClick.value == "테스트 영상") {
            SampleDownloadList(mainViewModel, viewModel)
        } else if (buttonClick.value == "다운로드한 목록") {
            SampleDownloadedList(viewModel)
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun SampleDownloadList(
    mainViewModel: MainViewModel,
    viewModel: VideoDownloadViewModel,
) {
    // val items = viewModel.downloadFaceBookList.collectAsState()
    val item by viewModel.pixcabayVideoList.collectAsState()
    val statusText = viewModel.downloads.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            count = item.size,
        ) { index ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(start = 7.dp, end = 7.dp),
            ) {
                val item = item[index]
                val requestId = item.id
                val name = item.name
                val url = item.videos.large.url

                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        item.videos.large.url,
                        Modifier.weight(0.7f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(0.3f)
                            .height(60.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        val status = item.downloadState ?: "Ready"

                        Button(onClick = {
                            viewModel.requestFreeDownload(requestId, name, url, item)
                        }) {
                            Text(status, fontSize = 12.sp)
                        }
                    }
                }

                StateCheck(requestId, statusText.value, viewModel)
            }
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun StateCheck(
    requestId: String,
    downloadInfo: Map<String, DownloadInfo>?,
    viewModel: VideoDownloadViewModel,
) {
    val state = downloadInfo?.get(requestId)?.state

    when (state) {
        DownloadState.COMPLETED -> {
//            viewModel.notificationHelper.showCompleted(
//                DownloadNotificationData(
//                    title = "완료",
//                    message = "다운로드 완료되었습니다.",
//                    smallIcon = com.sean.ratel.player.core.R.drawable.ic_download
//                )
//            )
            viewModel.updateDownloadState(
                requestId,
                DownloadState.COMPLETED.toString(),
            )
            viewModel.completeDownload(
                requestId,
                DownloadState.COMPLETED.toString(),
            )
        }

        DownloadState.STOPPED -> {
            // 다운로드 실패 시 알림 생성
            //  viewModel.notificationHelper.cancel(12345)
            viewModel.updateDownloadState(requestId, DownloadState.STOPPED.toString())
        }

        DownloadState.FAILED -> {
//            viewModel.notificationHelper.showFailed(
//                DownloadNotificationData(
//                    title = "실패",
//                    message = "다운로드 실패",
//                    smallIcon = com.sean.ratel.player.core.R.drawable.ic_download
//                )
//            )
            viewModel.updateDownloadState(requestId, DownloadState.FAILED.toString())
        }

        DownloadState.DOWNLOADING -> {
//            viewModel.notificationHelper.showProgress(
//                DownloadNotificationData(
//                    title = "다운로드 중",
//                    message = "진행률: progress%",
//                    progress = 10,
//                    smallIcon = com.sean.ratel.player.core.R.drawable.ic_download
//                )
//            )
            viewModel.updateDownloadState(requestId, DownloadState.DOWNLOADING.toString())
        }

        else -> {
            Unit
        }
    }
}

@OptIn(UnstableApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun SampleDownloadedList(viewModel: VideoDownloadViewModel) {
    val items =
        viewModel.downloadedList
            .collectAsState()
            .value
            .filter { it.downloadBrand == DownloadBland.PIXABAY }

    Log.d("hbungshin", "size : ${items.size}")

    Box(
        Modifier
            .fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                count = items.size,
            ) { text ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(end = 7.dp)
                        .clickable {
                            viewModel.navigator.navigateTo(
                                Destination.EndPlayer.dynamicRoute(
                                    contentId = items[text].requestId,
                                    startIndex = text,
                                ),
                            )
                        },
                ) {
                    val videoThumbnail =
                        items
                            .get(text)
                            .downloadResponse
                            ?.videos
                            ?.large
                            ?.thumbnail

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        videoThumbnail.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.wrapContentSize(),
                            )
                        }
                        Text(
                            items[text].downloadResponse?.name ?: "",
                            Modifier
                                .weight(0.7f)
                                .padding(start = 5.dp),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
