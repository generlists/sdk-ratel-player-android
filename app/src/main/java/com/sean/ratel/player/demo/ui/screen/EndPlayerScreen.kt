package com.sean.ratel.player.demo.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.sean.ratel.player.core.data.domain.model.InfoType
import com.sean.ratel.player.core.data.domain.model.PreviewInfoData
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.ui.ThemeMode
import com.sean.ratel.player.ui.control.MediaScreen
import com.sean.ratel.player.ui.control.component.options.PrevViewInfoDialog
import com.sean.ratel.player.ui.control.component.share.ShareBottomSheet
import com.sean.ratel.player.utils.log.RLog
import dagger.hilt.android.UnstableApi
import kotlinx.coroutines.launch
import java.net.URLDecoder
import kotlin.collections.emptyList


@OptIn(UnstableApi::class)
@Composable
fun EndPlayerScreen(
    modifier: Modifier,
    themeMode: ThemeMode,
    urls: String,
    startIndex: Int,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    endViewModel: VideoEndViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var shareButtonClick by remember {
        mutableStateOf<Pair<Boolean, PreviewInfoData?>>(
            Pair(
                false,
                null
            )
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

         endViewModel.initInterstitialAd(context)

        // detail_screen에서 인자를 받았을 때
        val receivedArg = urls

        val decodedArg = URLDecoder.decode(receivedArg, "UTF-8")

        val restoredList = decodedArg.split(",").map { itemStr ->
            val parts = itemStr.split(":")
            val id = parts[0]
            val qList = parts[1].split("|").map { qStr ->
                val qParts = qStr.split("@")
                // Quality Enum 복구와 URL
                Pair(Quality.valueOf(qParts[0]), qParts[1])
            }

            Pair(id, qList)
        }

        // (1052137116925296, [(SD, 1052137116925296_SD), (HD, 1052137116925296_SD), (AUDIO, 1052137116925296_SD)])
        val urlDecodeList =
            restoredList // (URLDecoder.decode(urls, StandardCharsets.UTF_8.toString())).split(",")
        val localContent = listOf("content://media/external/file/1000000052")
        //val localFile = listOf("file:///storage/emulated/0/Download/kakaotallk/14902146_1080_1920_25fps.mp4")
//        restoredList.forEach {
//            Log.d("SHINHN","11 it : $it startIndex : $startIndex")
//        }
        // list: List<Pair<String, List<Pair<Quality, String>>>>
        val localList = listOf(
            "123445566" to emptyList<Pair<Quality,String>>()
        )
        val reorder = reorderMediaList(urlDecodeList+localList, startIndex)

        reorder.forEach {
            RLog.d("LOG_TAG", "222 it : $it startIndex : $startIndex")
        }
        MediaScreen(
            modifier = Modifier,
            mediaOptions = endViewModel.mediaOptions,
            themeMode = themeMode,
            mediaList = reorder,
            startIndex = 0,
            listStartIndex = startIndex,
            qualityStartIndex = 0,
            topBar = { onInfoClick, onOptionClick ->
                EndTopBar(
                    modifier = Modifier,
                    historyBack = {},
                    shareButtonClick = {
                        playerViewModel.getShareScreenCapture(onInfo = { shareData ->
                            shareData?.let {
                                shareButtonClick = Pair(true, shareData)

                            }

                        }, endViewModel.mediaOptions.value.speed)
                    },
                    infoClick = { onInfoClick(endViewModel.getDummyData()) },
                    bookMarkClick = {},
                    optionClick = { onOptionClick() }
                )
            },
            onOptionChanged = { k, v ->
                RLog.d("LOG_TAG", "optionChanged k = $k v = $v")
                endViewModel.setUpdateMediaOption(k, v)
            }
        )
        val scope = rememberCoroutineScope()
        var openShareSheet by remember { mutableStateOf<Pair<Boolean, Uri?>>(Pair(false, null)) }


        if (shareButtonClick.first) {
            shareButtonClick.second?.let { previewInfoData ->
                PrevViewInfoDialog(previewInfoData, themeMode, onConfirm = { infoType ->
                    scope.launch {

                        previewInfoData.bitmap?.let { bitmap ->

                            playerViewModel.saveCaptureFile(
                                context, previewInfoData.infoType, bitmap,
                                saveComplete = { file, uri ->
                                    val uri = file?.let {
                                        FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                    } ?: run {
                                        uri
                                    }

                                    openShareSheet = Pair(true, uri)

                                })

                        } ?: run {
                            if (infoType == InfoType.ScreenShot)
                                Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show()
                        }
                    }

                    shareButtonClick = Pair(false, null)

                }, onDismiss = {
                    shareButtonClick = Pair(false, null)
                })
            }

        }
        if (openShareSheet.first) {
            openShareSheet.second?.let {
                ShareBottomSheet(
                    uri = it,
                    themeMode,
                    imageShareManager = endViewModel.imageShareManager,
                    onDismiss = { openShareSheet = Pair(false, null) },
                    onClick = {
                        RLog.d("hbungshin", "라벨 ${it}")

                    }
                )
            }
        }
    }


}

fun reorderMediaList(
    list: List<Pair<String, List<Pair<Quality, String>>>>,
    startIndex: Int
): List<Pair<String, List<Pair<Quality, String>>>> {
    if (list.isEmpty() || startIndex !in list.indices) return list

    // 1. startIndex부터 끝까지 (예: [1, 2])
    val head = list.subList(startIndex, list.size)

    // 2. 처음부터 startIndex 전까지 (예: [0])
    val tail = list.subList(0, startIndex)

    // 3. 둘이 합치기! ([1, 2] + [0])
    return head + tail
}

