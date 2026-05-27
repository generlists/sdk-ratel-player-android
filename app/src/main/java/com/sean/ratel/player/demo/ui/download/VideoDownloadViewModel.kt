package com.sean.ratel.player.demo.ui.download

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.ratel.player.core.data.domain.model.DownloadInfo
import com.sean.ratel.player.core.data.domain.model.DownloadState
import com.sean.ratel.player.core.data.domain.model.HttpHeaders
import com.sean.ratel.player.core.data.domain.model.VideoDownloadNotifier
import com.sean.ratel.player.demo.data.download.VideoHit
import com.sean.ratel.player.demo.data.download.domain.DownloadBland
import com.sean.ratel.player.demo.data.download.domain.VideoDownloadedInfo
import com.sean.ratel.player.demo.data.repository.VideoDownloadRepository
import com.sean.ratel.player.demo.ui.navigation.Navigator
import dagger.hilt.android.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class VideoDownloadViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val navigator: Navigator,
        private val repository: VideoDownloadRepository,
        var notificationHelper: VideoDownloadNotifier,
    ) : ViewModel() {
        private val _downloads = MutableStateFlow<Map<String, DownloadInfo>>(emptyMap())
        val downloads = _downloads.asStateFlow()

        private val _downloadedList = MutableStateFlow<List<VideoDownloadedInfo>>(emptyList())
        val downloadedList = _downloadedList.asStateFlow()

        private val _pixcabayVideoList = MutableStateFlow<List<VideoHit>>(emptyList())
        val pixcabayVideoList = _pixcabayVideoList.asStateFlow()

        private fun addListener() {
            repository.addDownLoadEventListener { downloadInfo ->
                _downloads.update { it + (downloadInfo.id to downloadInfo) }
            }
        }

        fun loadSampleData() {
            viewModelScope.launch {
                requestPixaBayApi("flower")
            }
        }

        fun requestFreeDownload(
            requestId: String,
            name: String,
            url: String,
            videoHit: VideoHit,
            headers: HttpHeaders? = null,
            cookies: String? = null,
        ) {
            viewModelScope.launch {
                addListener()
                download(
                    requestId,
                    name,
                    url ?: "",
                    headers,
                    cookies,
                    VideoDownloadedInfo(DownloadBland.PIXABAY, requestId, url, videoHit),
                )
            }
        }

        fun download(
            id: String,
            title: String,
            url: String,
            headers: HttpHeaders? = null,
            cookies: String? = null,
            downloadedInfo: VideoDownloadedInfo? = null,
        ) = repository.addDownload(id, title, url, headers, cookies, downloadedInfo)

        fun completeDownload(
            requestId: String,
            state: String,
        ) {
            _downloadedList.update { list ->
                list.map { item ->
                    Log.d(
                        "LOG_TAG",
                        "item requestId : ${item.requestId},$ , requestId : $requestId requestUrl : ${item.requestUrl}",
                    )
                    if (item.requestId == requestId && state == DownloadState.COMPLETED.name) {
                        item.copy(
                            downloadPath = requestId,
                            requestUrl = item.requestUrl,
                            realDownloadPath = getRealFilePath(requestId),
                        )
                    } else {
                        item
                    }
                }
            }
            loadDownloadList(DownloadBland.PIXABAY)
        }

        // UI 상태 업데이트
        fun updateDownloadState(
            requestId: String,
            state: String,
        ) {
            _pixcabayVideoList.update { list ->
                list.map { item ->
                    if (item.id == requestId) {
                        item.copy(
                            downloadState = state,
                        )
                    } else {
                        item
                    }
                }
            }
        }

        fun getRealFilePath(requestId: String): String? {
            val outputDir = context.getExternalFilesDir("media_output")
            if (outputDir == null || !outputDir.exists()) {
                return null
            }

            val fileName = "$requestId.mp4"

            return File(outputDir, fileName).path
        }

        fun loadDownloadList(downloadBland: DownloadBland) {
            val downloadItem =
                repository.getDownloadedItem().filter {
                    repository
                        .downloadedInfoFromByteArray(
                            downloadBland,
                            it.customData,
                        )?.downloadBrand == downloadBland
                }

            var list = listOf<VideoDownloadedInfo>()
            _downloadedList.value = listOf<VideoDownloadedInfo>()

            downloadItem.forEach { info ->
                val downloadInfo =
                    repository.downloadedInfoFromByteArray(downloadBland, info.customData)

                if (downloadInfo != null) {
                    list += downloadInfo
                }

                _pixcabayVideoList.value =
                    _pixcabayVideoList.value.map { item ->
                        if (item.id == info.id) {
                            item.copy(
                                downloadState = DownloadState.from(info.state).toString(),
                            )
                        } else {
                            item
                        }
                    }
            }

            _downloadedList.value += list
        }

        fun requestPixaBayApi(
            query: String,
            perPage: Int = 20,
            page: Int = 1,
        ) {
            viewModelScope.launch {
                repository
                    .requestPixaBayDownload(
                        query,
                        perPage,
                        page,
                    ).collect { response ->
                        Log.d("TAG", "response : $response")
                        response?.let {
                            _pixcabayVideoList.value = response.hits
                            loadDownloadList(DownloadBland.PIXABAY)
                        }
                    }
            }
        }
    }
