package com.sean.ratel.player.demo.ui.screen


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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.data.download.domain.DownloadBland
import com.sean.ratel.player.demo.ui.download.VideoDownloadViewModel
import com.sean.ratel.player.demo.ui.navigation.Destination
import dagger.hilt.android.UnstableApi
import so.smartlab.video.player.ad.admob.data.model.AdMobInitState
import so.smartlab.video.player.ad.admob.ui.kind.AdaptiveInLineBannerView
import java.net.URLEncoder
import kotlin.math.floor

@OptIn(UnstableApi::class)
@Composable
fun DownloadFacebook(
    mainViewModel: MainViewModel,
    viewModel: VideoDownloadViewModel,
    requestInLineBannerView: suspend () -> Unit
) {

    var buttonClick = remember { mutableStateOf<String>("테스트 영상") }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentSize()
        ) {
            Box(
                Modifier
                    .wrapContentSize()
                    .weight(1.0f)
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
                    .weight(1.0f)
            ) {
                Button(onClick = {
                    buttonClick.value = "다운로드한 목록"
                }, Modifier.padding(5.dp)) {
                    Text("다운로드한 목록")
                }
            }
        }

        if (buttonClick.value == "테스트 영상") {
            FaceBookDownload(mainViewModel, viewModel, requestInLineBannerView)
        } else if (buttonClick.value == "다운로드한 목록") {
            DownloadListFacebook(viewModel)
        }

    }

}

@Composable
fun FaceBookDownload(
    mainViewModel: MainViewModel,
    viewModel: VideoDownloadViewModel,
    requestInLineBannerView: suspend () -> Unit
) {
    val items = viewModel.downloadFaceBookList.collectAsState()
    val statusText = viewModel.downloads.collectAsState()
    val adaptiveInLineBannerState = mainViewModel.adaptiveInlineBannerState.collectAsState()
    val adMobInitStateState = mainViewModel.adMobinitState.collectAsState()
    val location = floor(Math.random() * items.value.size);


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        items(
            count = items.value.size

        ) { index ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(start = 7.dp, end = 7.dp)
            ) {
                val requestId = items.value[index].requestId
                val url = items.value[index].url

                if (index == location.toInt()) {

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (adMobInitStateState.value == AdMobInitState.InitComplete) {
                            LaunchedEffect(adaptiveInLineBannerState) {
                                requestInLineBannerView()
                            }
                        }
                        AdaptiveInLineBannerView(
                            Color.Black,
                            Color.White,
                            adMobBannerState = adaptiveInLineBannerState.value
                        )

                    }

                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            items.value[index].url,
                            Modifier.weight(0.7f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .weight(0.3f)
                                .height(60.dp), contentAlignment = Alignment.CenterEnd
                        ) {


                            val status = items.value[index].downloadState ?: "Ready"

                            Button(onClick = {
                                viewModel.requestFacebookDownloadUrl(requestId, url)


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
}

@Composable
fun StateCheck(
    requestId: String,
    downloadInfo: Map<String, DownloadInfo>?,
    viewModel: VideoDownloadViewModel
) {

    val state = downloadInfo?.get(requestId)?.state


    if (state == null) return



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
                DownloadState.COMPLETED.toString()
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

        else -> Unit
    }
}

@OptIn(UnstableApi::class)
@Composable
fun DownloadListFacebook(viewModel: VideoDownloadViewModel) {

    val items =
        viewModel.downloadedList.collectAsState().value.filter { it.downloadBrand == DownloadBland.FACEBOOK }


    Box(
        Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEachIndexed { index, info ->
                if(index  ==1){
                    info.screen = listOf<Quality>(Quality.AUDIO)
                }else{
                    info.screen = listOf<Quality>(Quality.SD, Quality.HD, Quality.AUDIO)
                }

            }

            items(
                count = items.size

            ) { text ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(end = 7.dp)
                        .clickable {

                            var index =0
                            var name ="AUDIO"

                            // items: List<Pair<String, List<Pair<Quality, String>>>>
                            val navArgument = items.joinToString(",") { q ->
                                val encodedId = URLEncoder.encode(q.requestId, "UTF-8")

                               val list  =listOf(Pair("AUDIO","16ZowBcqWN_SD"))
                                val listPart = q.screen.joinToString("|") { quality ->

//                                    if(q.requestId == "1DXoSc2873"){
//
//                                        var encodedUrl =  URLEncoder.encode(q.requestId + "_SD", "UTF-8")
//                                        if(index ==0){
//                                            name = "SD"
//                                            encodedUrl = "1052137116925296_SD"
//
//                                        }else if(index ==1){
//                                            name = "HD"
//                                            encodedUrl = "1052137116925296_SD"
//                                        }else{
//                                            name = "AUDIO"
//                                            encodedUrl = "16ZowBcqWN_SD"
//                                        }
//                                        index++
//                                        "${name}@$encodedUrl"
//
//                                    }else{
                                        val encodedUrl = if (quality.name == "AUDIO") {
                                            URLEncoder.encode(q.requestId + "_SD", "UTF-8")

                                        } else if (quality.name == "HD") {
                                            URLEncoder.encode(q.requestId + "_SD", "UTF-8")
                                        } else if (quality.name == "SD") {
                                            URLEncoder.encode(q.requestId + "_SD", "UTF-8")
                                        } else {
                                        }
                                        "${quality.name}@$encodedUrl"
                                   // }


                                }
                                "$encodedId:$listPart"

                            }


                            viewModel.navigator.navigateTo(
                                Destination.EndPlayer.dynamicRoute(
                                    contentId = navArgument,
                                    startIndex = text
                                )
                            )
                        }
                ) {
                    val videoThumbnail = items.get(text).downloadResponse.thumbnail

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        videoThumbnail.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.wrapContentSize(),
                            )
                        }
                        Text(
                            items[text].downloadResponse.title,
                            Modifier
                                .weight(0.7f)
                                .padding(start = 5.dp),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

            }
        }
    }

}
