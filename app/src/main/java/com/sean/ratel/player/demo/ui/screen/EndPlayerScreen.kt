package com.sean.ratel.player.demo.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.sean.ratel.player.core.data.domain.model.InfoType
import com.sean.ratel.player.core.data.domain.model.PreviewInfoData
import com.sean.ratel.player.core.data.player.pip.PIPManager
import com.sean.ratel.player.core.data.player.pip.PipResult
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
    pipManager: PIPManager,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    endViewModel: VideoEndViewModel = hiltViewModel(),
    downloadViewModel: VideoDownloadViewModel,
) {
    val context = LocalContext.current
    val activity = context as Activity
    var visibleRect by remember { mutableStateOf(Rect()) }
    var screenSize by remember { mutableStateOf(Size(0, 0)) }
    val shareLauncher = GetShareLauncher(activity)

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
    LaunchedEffect(playerViewModel.mediaStreamPlayer) {
        playerViewModel.mediaStreamPlayer.resolution.collect { resolution ->
            if (resolution.width > 0 && resolution.height > 0) {
                endViewModel.setResolution(resolution)
            }
        }
    }

    LaunchedEffect(downloadedItemList) {
        endViewModel.fetchList(startIndex, downloadedItemList)
    }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates ->
                val positionInWindow = layoutCoordinates.positionInWindow()
                screenSize = Size(layoutCoordinates.size.width, layoutCoordinates.size.height)
                visibleRect =
                    Rect(
                        positionInWindow.x.toInt(),
                        positionInWindow.y.toInt(),
                        (positionInWindow.x + layoutCoordinates.size.width).toInt(),
                        (positionInWindow.y + layoutCoordinates.size.height).toInt(),
                    )
            }.background(Color.Black),
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
                topBar = { onInfoClick ->
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
                        infoClick = { result ->
                            onInfoClick(
                                endViewModel.getDummyData(
                                    it[startIndex].first,
                                    "memo 좋은날!!",
                                ),
                            ) { confirmed ->
                                result(confirmed)
                            }
                        },
                        bookMarkClick = {
                            onClickPipAction(
                                context = context,
                                visibleRect = visibleRect,
                                screenSize = screenSize,
                                pipManager = pipManager,
                                playerViewModel = playerViewModel,
                                endPlayerViewModel = endViewModel,
                            )
                        },
                    )
                },
                onOptionChanged = { k, v ->
                    Log.d("LOG_TAG", "optionChanged k = $k v = $v")
                    endViewModel.setUpdateMediaOption(k, v)
                },
                updateMemo = { type, id, memo ->
                },
            )
        }

        val scope = rememberCoroutineScope()
        var openShareSheet by remember { mutableStateOf<Pair<Boolean, Uri?>>(Pair(false, null)) }

        if (shareButtonClick.first) {
            shareButtonClick.second?.let { previewInfoData ->
                PrevViewInfoDialog(previewInfoData, themeMode, onMemoChange = { a, b, c -> }, onConfirm = { infoType ->
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
                                    Log.d("hbungshin", "ssssssssss")
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
                        Log.d("공유!!!", "$it")
                    },
                    shareLauncher = shareLauncher,
                )
            }
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun GetShareLauncher(activity: Activity?): ManagedActivityResultLauncher<Intent, ActivityResult>? {
    activity?.let {
        // Compose에서 ActivityResultLauncher 등록
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            Log.d("shareAppLinkButton", "result : $result")
            if (result.resultCode == Activity.RESULT_OK) {
            }
        }
    }
    return null
}

fun onClickPipAction(
    context: Context,
    visibleRect: Rect,
    screenSize: Size,
    pipManager: PIPManager,
    playerViewModel: PlayerViewModel,
    endPlayerViewModel: VideoEndViewModel,
) {
    val isPlaying = playerViewModel.isPlaying
    val resolution = endPlayerViewModel.resolution
    val isFirst = playerViewModel.isFirst()
    val isLast = playerViewModel.isLast()
    val pageId = playerViewModel.currentItemIndex.value

    Log.d("PIP_CLICK", "isPlaying : ${isPlaying.value} , resolution : ${resolution.value} visibleRect : $visibleRect $screenSize")

    val enterPipMode =
        pipManager.enterPipMode(
            screenSize,
            visibleRect,
            isPlaying = isPlaying.value,
            isFirst = isFirst,
            isLast = isLast,
        )

    when (enterPipMode) {
        PipResult.NoSystemFeature -> {
            Toast
                .makeText(
                    context,
                    "시스템 오류",
                    Toast.LENGTH_LONG,
                ).show()
        }

        PipResult.NoPermission -> {
            // permission 으로 이동
        }

        PipResult.Success -> {
            pipManager.setPIPClick(pageId.toString(), true)
        }

        else -> {}
    }
}
