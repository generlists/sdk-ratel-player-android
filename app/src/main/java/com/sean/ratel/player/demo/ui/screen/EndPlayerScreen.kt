package com.sean.ratel.player.demo.ui.screen

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.demo.data.download.domain.DownloadBland
import com.sean.ratel.player.demo.ui.download.VideoDownloadViewModel
import com.sean.ratel.player.ui.ThemeMode
import com.sean.ratel.player.ui.control.MediaScreen
import com.sean.ratel.player.ui.control.component.options.PrevViewInfoDialog
import com.sean.ratel.player.ui.control.component.share.ShareBottomSheet
import dagger.hilt.android.UnstableApi
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun EndPlayerScreen(
    modifier: Modifier,
    themeMode: ThemeMode,
    urls: String,
    startIndex: Int,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    endViewModel: VideoEndViewModel = hiltViewModel(),
    downloadViewModel: VideoDownloadViewModel,
) {
    val context = LocalContext.current
    var shareButtonClick by remember {
        mutableStateOf<Pair<Boolean, PreviewInfoData?>>(
            Pair(
                false,
                null,
            ),
        )
    }
    val downloadedItemList =
        downloadViewModel.downloadedList
            .collectAsState()
            .value
            .filter { it.downloadBrand == DownloadBland.PIXABAY }

    val mediaList by endViewModel.endTransferList.collectAsState()

    LaunchedEffect(downloadedItemList) {
        endViewModel.fetchList(startIndex, downloadedItemList)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        mediaList?.let {
            MediaScreen(
                modifier = Modifier,
                mediaOptions = endViewModel.mediaOptions,
                themeMode = themeMode,
                mediaList = it,
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
                        optionClick = { onOptionClick() },
                    )
                },
                onOptionChanged = { k, v ->
                    Log.d("LOG_TAG", "optionChanged k = $k v = $v")
                    endViewModel.setUpdateMediaOption(k, v)
                },
            )
        }

        val scope = rememberCoroutineScope()
        var openShareSheet by remember { mutableStateOf<Pair<Boolean, Uri?>>(Pair(false, null)) }

        if (shareButtonClick.first) {
            shareButtonClick.second?.let { previewInfoData ->
                PrevViewInfoDialog(previewInfoData, themeMode, onConfirm = { infoType ->
                    scope.launch {
                        previewInfoData.bitmap?.let { bitmap ->

                            playerViewModel.saveCaptureFile(
                                context,
                                previewInfoData.infoType,
                                bitmap,
                                saveComplete = { file, uri ->
                                    val uri =
                                        file?.let {
                                            FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file,
                                            )
                                        } ?: run {
                                            uri
                                        }

                                    openShareSheet = Pair(true, uri)
                                },
                            )
                        } ?: run {
                            if (infoType == InfoType.ScreenShot) {
                                Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show()
                            }
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
                    },
                )
            }
        }
    }
}
