package com.sean.ratel.player.demo.ui.screen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.ratel.player.core.data.domain.model.InfoType
import com.sean.ratel.player.core.data.domain.model.PlayMediaItem
import com.sean.ratel.player.core.data.domain.model.PreviewInfoData
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.demo.data.download.domain.VideoDownloadedInfo
import com.sean.ratel.player.ui.MediaOptions
import com.sean.ratel.player.ui.control.component.options.MediaOptionKey
import com.sean.ratel.player.ui.control.component.options.MediaOptionValue
import com.sean.ratel.player.ui.control.component.share.ImageShareManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoEndViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val imageShareManager: ImageShareManager,
    ) : ViewModel() {
        private val _mediaOptions = MutableStateFlow(MediaOptions())
        val mediaOptions: StateFlow<MediaOptions> = _mediaOptions

        private val _endTransferList =
            MutableStateFlow<List<Pair<String, List<Pair<Quality, PlayMediaItem>>>>?>(null)
        val endTransferList: StateFlow<List<Pair<String, List<Pair<Quality, PlayMediaItem>>>>?> =
            _endTransferList

        fun setUpdateMediaOption(
            key: MediaOptionKey,
            value: MediaOptionValue,
        ) {
            when {
                key == MediaOptionKey.PLAYBACK_REPEAT && value is MediaOptionValue.Repeat -> {
                    _mediaOptions.update { currentOptions ->
                        currentOptions.copy(repeatMode = value.value)
                        // preference 저장
                        // setRepeat(value.value)//
                    }
                }

                key == MediaOptionKey.SCREEN_VOLUME && value is MediaOptionValue.FloatValue -> {
                    _mediaOptions.update { currentOptions ->
                        currentOptions.copy(volume = value)
                        // preference 저장
                        // setVolume(value.value)//
                    }
                }

                key == MediaOptionKey.SCREEN_SCALE && value is MediaOptionValue.Scale -> {
                    _mediaOptions.update { currentOptions ->
                        currentOptions.copy(contentScale = value.value)
                        // preference 저장
                        // setScale(value.value)
                    }
                }

                key == MediaOptionKey.SCREEN_BRIGHT && value is MediaOptionValue.FloatValue -> {
                    _mediaOptions.update { currentOptions ->
                        currentOptions.copy(screenBrightness = value)
                        // preference 저장
                        // setScreenBrightness(value.value)
                    }
                }

                key == MediaOptionKey.PLAYBACK_SPEED && value is MediaOptionValue.Speed -> {
                    _mediaOptions.update { currentOptions ->
                        currentOptions.copy(speed = value.value)
                        // preference 저장
                        // setSpeed(value.value)
                    }
                }

                key == MediaOptionKey.SHUFFLE && value is MediaOptionValue.Toggle -> {
                    _mediaOptions.update { currentOptions ->
                        currentOptions.copy(shuffle = value)
                        // preference 저장
                        // setShuffle(value.value)
                    }
                }
            }

            Log.d("optionChanged", "after optionChanged ${_mediaOptions.value}")
        }

        fun getDummyData(): PreviewInfoData {
            val mainLocalInfoList =
                listOf<Pair<String, String>>(
                    "메인 폴더" to "카메라",
                    "폴더명" to "line",
                    "mime Type" to "video/mp4",
                    "재생 시간" to "00:11",
                    "파일 크기 " to "320MB",
                    "파일명" to "그저그런.mp4",
                    "상대경로" to "/DCIM",
                    "저장위치" to "/storage/emulator/0/DCIM/line/12356789.mp4",
                )
            val subLocalInfoList =
                listOf<Pair<String, String>>(
                    "추가된 시간" to "00:11",
                    "해상도" to "1090 * 1920(HD)",
                    "소유자" to "FaceBook",
                    "orientation" to "PORTRAIT",
                )

            return PreviewInfoData(
                infoType = InfoType.LocalVideoInfo,
                bitmap = null,
                title = "영상정보 이다 ",
                mainInfoList = mainLocalInfoList,
                subInfoList = subLocalInfoList,
                confirmText = "확인",
                cancelText = "취소",
            )
        }

        fun fetchList(
            startIndex: Int,
            itemList: List<VideoDownloadedInfo>,
        ) {
            viewModelScope.launch {
                var videoList = listOf<Pair<String, List<Pair<Quality, PlayMediaItem>>>>()

                itemList.map { item ->

                    var qualityList = listOf<Pair<Quality, PlayMediaItem>>()

                    Quality.entries.forEach { quality ->

                        when {
                            quality == Quality.SD -> {
                                val addList =
                                    listOf(
                                        Pair(
                                            Quality.SD,
                                            PlayMediaItem(
                                                item.requestId + "_" + "SD",
                                                item.requestUrl,
                                                "file://${item.realDownloadPath}",
                                            ),
                                        ),
                                    )
                                qualityList = qualityList + addList
                            }
                        }
                    }

                    videoList = videoList + listOf(Pair(item.requestId, qualityList))

                    val headVideoList =
                        videoList.subList(
                            startIndex.coerceIn(0, videoList.size),
                            videoList.size,
                        )

                    val tailVideoList = videoList.subList(0, startIndex.coerceAtMost(videoList.size))

                    _endTransferList.value = headVideoList + tailVideoList
                    _endTransferList.value?.forEach {
                        Log.d(
                            "hbungshin",
                            "리스트 크기: $it",
                        )
                    }
                }
            }
        }
    }
