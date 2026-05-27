package com.sean.ratel.player.ui.control

import android.content.Context
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.sean.ratel.player.core.data.domain.model.InfoType
import com.sean.ratel.player.core.data.domain.model.MediaStreamTransitionReason
import com.sean.ratel.player.core.data.domain.model.PlayMediaItem
import com.sean.ratel.player.core.data.domain.model.PlaybackState
import com.sean.ratel.player.core.data.domain.model.PreviewInfoData
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.ui.MediaOptions
import com.sean.ratel.player.ui.R
import com.sean.ratel.player.ui.ThemeMode
import com.sean.ratel.player.ui.control.component.PlayerErrorDialog
import com.sean.ratel.player.ui.control.component.options.MediaOptionKey
import com.sean.ratel.player.ui.control.component.options.MediaOptionValue
import com.sean.ratel.player.ui.control.component.options.PrevViewInfoDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import so.smartlab.common.utils.log.RLog

@OptIn(UnstableApi::class)
@Composable
fun MediaScreen(
    modifier: Modifier = Modifier,
    themeMode: ThemeMode,
    mediaOptions: StateFlow<MediaOptions>,
    mediaList: List<Pair<String, List<Pair<Quality, PlayMediaItem>>>>,
    startIndex: Int,
    listStartIndex: Int = 0,
    qualityStartIndex: Int,
    topBar: @Composable (onInfoClick: (PreviewInfoData) -> Unit, onOptionClick: () -> Unit) -> Unit,
    onOptionChanged: (MediaOptionKey, MediaOptionValue) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    viewModel.setMediaList(mediaList)

    // https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8

    val isHWYAccelerated by viewModel.isHWYAccelerated.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var showOption by remember { mutableStateOf(false) }
    val showInfoDialog = remember { mutableStateOf(Pair<Boolean, PreviewInfoData?>(false, null)) }

    var hideControls by remember { mutableStateOf(true) }
    val controlDelayTime = stringResource(R.string.player_control_delay_time)

    val audioOnly by viewModel.audioOnly.collectAsState()

    val scale = mediaOptions.collectAsState()

    val isEndVideo = viewModel.isPlayEnd.collectAsState()
    val isSeeking by viewModel.isSeek.collectAsState()

    val currentIndex = viewModel.mediaStreamPlayer.currentIndex.collectAsState()

    var forceUpdate by remember { mutableStateOf(false) }

    val playList = viewModel.mediaList.collectAsState()

    var playerView =
        remember { derivedStateOf { getPlayerView(context = context, viewModel = viewModel) } }

    var errorState by remember { mutableStateOf(Pair(false, Pair<Int, String?>(200, null))) }

    // 콘트롤러가 바뀌면 리컴파일 됨  필요
    LaunchedEffect(Unit) {
        RLog.d("TAG", "$playList ,$qualityStartIndex , $startIndex")
        start(
            viewModel = viewModel,
            qualityStartIndex = qualityStartIndex,
            startIndex = startIndex,
            connectFailError = false,
            playList = playList.value,
        )

        // 초기값
        viewModel.setVideoQualityChanged(
            qualityChanged = false,
            videoQuality = playList.value[startIndex].second[qualityStartIndex],
        ) // 최초  첫번째 현재 재생중 퀄러티

        playerView.value.player = viewModel.mediaStreamPlayer.getPlayer()

        viewModel.setCurrentTime()
    }

    LaunchedEffect(viewModel.currentItemIndex) {
        val currentIndex = viewModel.mediaStreamPlayer.currentIndex
        val mediaItem = viewModel.videoQualityChanged
        val optionQualityChanged = viewModel.optionQualityChanged
        val qualityList = viewModel.qualityList

        combine(currentIndex, mediaItem) { currentIndex, mediaItem ->

            viewModel.setCurrentIndex(currentIndex)

            if (optionQualityChanged.value) {
                (
                    viewModel.mediaStreamPlayer.replaceMediaItem(
                        currentIndex,
                        viewModel.buildMediaItem(mediaItem, isConnectError = false),
                    )
                )
                viewModel.audioOnly(false)
            }

            if (playList.value.isNotEmpty()) {
                viewModel.setQuality(playList.value[currentIndex].second)
                RLog.d(
                    "MediaScreen",
                    "입력 퀄러티 값   : ${playList.value[currentIndex].second} $currentIndex",
                )
            }

            Triple(currentIndex, mediaItem, qualityList)
        }.collect {
            RLog.d("MediaScreen", "현재 퀄리티   : ${it.second}")
            RLog.d(
                "MediaScreen",
                "현재 퀄리티  : ${it.second?.first} , currentIndex : ${currentIndex.value} startIndex : $startIndex : ${it.third.value[startIndex]}",
            )
            RLog.d(
                "MediaScreen",
                "오디오모드 사용여부 체크: ${!optionQualityChanged.value && it.third.value[startIndex].first == Quality.AUDIO}",
            )
            if (it.second.first == Quality.AUDIO ||
                (!optionQualityChanged.value && it.third.value[qualityStartIndex].first == Quality.AUDIO)
            ) {
                hideControls = false
                viewModel.audioOnly(true)
            } else {
                viewModel.audioOnly(false)
            }
            viewModel.setOptionQualityChanged(false)

            buttonEnabled(playList.value.size, currentIndex.value, viewModel)
        }
    }

    LaunchedEffect(!hideControls, isSeeking) {
        if (!hideControls && !isSeeking) {
            delay(controlDelayTime.toLong())
            hideControls = true
        }
    }

    // View
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        // PlayerView를 Compose에 삽입
        // BG->FG
        key(forceUpdate) {
            AndroidView(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                factory = { _ ->
                    val view = playerView.value
                    view.setBackgroundColor(android.graphics.Color.BLACK)
                    (view.parent as? ViewGroup)?.removeView(view)

                    view.player = viewModel.mediaStreamPlayer.getPlayer()

                    view
                },
                update = { view ->
                    if (view.player !== viewModel.mediaStreamPlayer.getPlayer()) {
                        view.player = viewModel.mediaStreamPlayer.getPlayer()
                    }
                },
            )
        }

        Box(
            Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    hideControls = !hideControls
                },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = if (isEndVideo.value) true else !hideControls,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                MediaScreenControlView(
                    Modifier.background(Color.Transparent),
                    viewModel,
                )
            }
        }

        // 외부 뷰 넘기기
        topBar({ infoData ->
            showInfoDialog.value = Pair(true, infoData)
        }, {
            showOption = true
        })

        if (showOption) {
            Box(
                Modifier
                    .fillMaxSize(),
            ) {
                val failStr = stringResource(R.string.player_control_capture_fail)

                MediaOptionSheet(
                    playerViewModel = viewModel,
                    themeMode = themeMode,
                    mediaOptions = mediaOptions,
                    onOptionChanged = onOptionChanged,
                    onDismiss = {
                        showOption = false
                    },
                    onInfo = { info ->
                        info?.let {
                            showInfoDialog.value = Pair(true, it)
                        } ?: run {
                            Toast.makeText(context, failStr, Toast.LENGTH_LONG).show()
                        }
                    },
                )
            }
        }

        playerView.value.resizeMode = scale.value.contentScale.scaleIndex

        if (audioOnly) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.AudioFile,
                    contentDescription = null,
                    Modifier
                        .width(84.dp)
                        .height(84.dp),
                    tint = Color.White,
                )
            }
        }

        if (showInfoDialog.value.first) {
            showInfoDialog.value.second?.let { previewInfoData ->
                val fileSaveFail = stringResource(R.string.player_control_file_Save_fail)
                PrevViewInfoDialog(
                    data = previewInfoData,
                    themeMode = themeMode,
                    onConfirm = { infoType ->
                        scope.launch {
                            previewInfoData.bitmap?.let { bitmap ->

                                if (infoType == InfoType.ScreenShot) {
                                    viewModel.saveCaptureFile(
                                        context,
                                        previewInfoData.infoType,
                                        bitmap,
                                    )

                                    // 옵션 변경
                                    onOptionChanged(
                                        MediaOptionKey.SCREEN_CAPTURE,
                                        MediaOptionValue.ObjectValue(previewInfoData.copy(bitmap = bitmap)),
                                    )
                                }
                            } ?: run {
                                if (infoType == InfoType.ScreenShot) {
                                    Toast.makeText(context, fileSaveFail, Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        showInfoDialog.value = Pair(false, null)
                    },
                    onDismiss = {
                        showInfoDialog.value = Pair(false, null)
                    },
                )
            }
        }
    }

    // 종료 체크
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->

                when (event) {
                    Lifecycle.Event.ON_STOP -> {
                        Log.d(
                            "MediaScreen",
                            "홈 버튼 눌렀네? 여기서 release 하든 pause 하든 해야 해! ${viewModel.videoQualityChanged.value}",
                        )
                        // 여기 호출되면 잔상이 남음..
                        viewModel.setSaveTimeMs(viewModel.mediaStreamPlayer.currentPosition.value ?: 0L)
                        viewModel.setSaveCurrentId(playList.value[viewModel.currentItemIndex.value].first)
                        viewModel.audioOnly(viewModel.audioOnly.value)

                        // resume 후 stop 처리
                        if (!viewModel.isPlaying.value) {
                            viewModel.mediaStreamPlayer.resume()
                        }

                        viewModel.mediaStreamPlayer.stop()
                        forceUpdate = false
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        if (viewModel.isStop.value) {
                            viewModel.setIsStop(false)
                            playerView.value.player = null
                            viewModel.mediaStreamPlayer.release()

                            playerView = mutableStateOf(getPlayerView(context, viewModel))

                            play(
                                viewModel,
                                qualityStartIndex,
                                viewModel.saveCurrentId.value,
                                playerView.value,
                            )
                            forceUpdate = true
                        }
                    }

                    else -> {
                        Unit
                    }
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            Log.d("MediaScreen", "compose despose ${playerView.value.player}")
            lifecycleOwner.lifecycle.removeObserver(observer)
            playerView.value.player = null
            viewModel.mediaStreamPlayer.clearVideoSurface()
            viewModel.mediaStreamPlayer.stop()
            viewModel.mediaStreamPlayer.release()
        }
    }

    LaunchedEffect(viewModel.mediaStreamPlayer.playbackState) {
        viewModel.mediaStreamPlayer.playbackState.collect { state ->
            Log.d("MediaScreen", "state : $state")
            when (state) {
                is PlaybackState.Prepared -> {}

                is PlaybackState.Playing -> {
                    viewModel.setRepeatMode(mediaOptions.value.repeatMode)
                    viewModel.setPlaying(isPlaying = true)
                    viewModel.setPlayAllEnd(false)
                    viewModel.setDuration()
                }

                is PlaybackState.MediaTransition -> {
                    if (state.reason == MediaStreamTransitionReason.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        // 인덱스 초기화,오디오 모드
                        RLog.d(
                            "MediaScreen",
                            "MediaTransition size ${playList.value.size} , currentIndex : $currentIndex",
                        )
                        viewModel.setRepeatMode(mediaOptions.value.repeatMode)
                        viewModel.setCurrentTime()
                        viewModel.audioOnly(false)
                        viewModel.setVideoQualityChanged(
                            qualityChanged = false,
                            videoQuality = playList.value[currentIndex.value].second[qualityStartIndex],
                        )
                    }
                }

                is PlaybackState.Complete -> {
                    viewModel.setPlayAllEnd(true)
                }

                is PlaybackState.Stop -> {
                    viewModel.setIsStop(true)
                }

                else -> {
                    viewModel.setPlaying(isPlaying = false)
                }
            }
        }
    }
    LaunchedEffect(viewModel.mediaStreamPlayer.playbackState) {
        viewModel.mediaStreamPlayer.playbackErrorState.collect { state ->
            Log.e("MediaScreen", "state : $state")
            when (state) {
                is PlaybackState.Error -> {
                    // 재생 url 만료
                    errorState = Pair(true, getErrorMessage(context, state.errorCode))
                }

                else -> {
                    Unit
                }
            }
        }
    }
    if (errorState.first) {
        PlayerErrorDialog(
            true,
            title =
                String.format(
                    stringResource(R.string.player_error),
                    "${errorState.second.first}",
                ),
            message = errorState.second.second ?: stringResource(R.string.player_unkwnown_error),
            onConfirm = {
                errorState = Pair(false, Pair(200, null))
                if (errorState.second.first == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS) {
                    start(
                        viewModel = viewModel,
                        qualityStartIndex = qualityStartIndex,
                        startIndex = 0,
                        connectFailError = true,
                        playList = mediaList,
                    )
                } else {
                    viewModel.setReset(reset = true)
                }
            },
            onDismiss = {
                errorState = Pair(false, Pair(200, null))
                viewModel.setReset(reset = true)
            },
        )
    }
}

private fun getErrorMessage(
    context: Context,
    errorCode: Int,
) = when (errorCode) {
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
        Pair(
            errorCode,
            context.getString(R.string.player_error_protocal),
        )
    }

    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
        Pair(
            errorCode,
            context.getString(R.string.player_error_file_not_founded),
        )
    }

    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
        Pair(
            errorCode,
            context.getString(R.string.player_error_connect_error),
        )
    }

    else -> {
        Pair(errorCode, context.getString(R.string.player_error_unknown_error))
    }
}

@OptIn(UnstableApi::class)
private fun getPlayerView(
    context: Context,
    viewModel: PlayerViewModel,
): PlayerView =
    PlayerView(context).apply {
        useController = false
        // 1. SurfaceView의 Holder를 가져와서 콜백을 달아줘
        (videoSurfaceView as? SurfaceView)?.holder?.addCallback(
            object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    RLog.d("MediaScreen", "surfaceCreated")
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int,
                ) {
                    // 화면 크기 바뀌었을 때 (회전 등)
                    RLog.d("MediaScreen", "surfaceChanged: $width x $height")
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    RLog.d("MediaScreen", "surfaceDestroyed")
                    viewModel.mediaStreamPlayer.clearVideoSurface()
                }
            },
        )

        viewModel.setSurfaceView(videoSurfaceView)
    }

private fun play(
    viewModel: PlayerViewModel,
    qualityStartIndex: Int,
    savedId: String,
    playerView: PlayerView,
) {
    val mediaList = viewModel.reorderMediaList(viewModel.mediaList.value, savedId)
    RLog.d("MediaScreen", "savedId : $savedId")
    RLog.d("MediaScreen", "playList : $mediaList")

    viewModel.setMediaList(mediaList)

    start(
        viewModel = viewModel,
        qualityStartIndex = qualityStartIndex,
        startIndex = 0,
        connectFailError = false,
        playList = mediaList,
    )
    viewModel.seekTo(0, viewModel.saveTimeMs.value)
    viewModel.setQuality(mediaList[0].second)
    viewModel.setVideoQualityChanged(
        qualityChanged = false,
        videoQuality = viewModel.videoQualityChanged.value,
    ) // 포그라운드 넘어가기전 화질

    playerView.player = viewModel.mediaStreamPlayer.getPlayer()

    viewModel.setCurrentTime()
}

private fun start(
    viewModel: PlayerViewModel,
    qualityStartIndex: Int,
    startIndex: Int,
    connectFailError: Boolean,
    playList: List<Pair<String, List<Pair<Quality, PlayMediaItem>>>>,
) {
    viewModel.mediaStreamPlayer.start(
        items =
            playList.map {
                viewModel.buildMediaItem(
                    playMediaItem = it.second[qualityStartIndex],
                    isConnectError = connectFailError,
                )
            },
        startIndex = startIndex,
    )
}

private fun buttonEnabled(
    size: Int,
    currentIndex: Int,
    viewModel: PlayerViewModel,
) {
    RLog.d("MediaScreen", "$size , $currentIndex")
    if (size == 0) return
    if (size > 1) {
        if (currentIndex == 0) {
            viewModel.beforeButtonEnabled(false)
            viewModel.nextButtonEnabled(true)
        } else if (size - 1 == currentIndex) {
            viewModel.beforeButtonEnabled(true)
            viewModel.nextButtonEnabled(false)
        } else {
            viewModel.nextButtonEnabled(true)
            viewModel.beforeButtonEnabled(true)
        }
    } else {
        viewModel.beforeButtonEnabled(false)
        viewModel.nextButtonEnabled(false)
    }
}
