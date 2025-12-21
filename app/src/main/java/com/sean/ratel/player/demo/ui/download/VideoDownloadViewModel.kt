package com.sean.ratel.player.demo.ui.download

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.ratel.player.core.data.domain.model.DownloadInfo
import com.sean.ratel.player.core.data.domain.model.DownloadState
import com.sean.ratel.player.core.data.domain.model.HttpHeaders
import com.sean.ratel.player.core.data.domain.model.VideoDownloadNotifier
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.data.download.domain.DownloadBland
import com.sean.ratel.player.demo.data.download.domain.SampleDownloadModel
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
class VideoDownloadViewModel @Inject constructor(
    @ApplicationContext val context:Context,
    val navigator: Navigator,
    private val repository: VideoDownloadRepository,
    var notificationHelper: VideoDownloadNotifier

) : ViewModel() {

    private val _downloads = MutableStateFlow<Map<String, DownloadInfo>>(emptyMap())
    val downloads = _downloads.asStateFlow()

    private val _downloadFaceBookList = MutableStateFlow<List<SampleDownloadModel>>(emptyList())
    val downloadFaceBookList = _downloadFaceBookList.asStateFlow()

    private val _downloadTiktokList = MutableStateFlow<List<SampleDownloadModel>>(emptyList())
    val downloadTiktokList = _downloadTiktokList.asStateFlow()

    private val _downloadedList = MutableStateFlow<List<VideoDownloadedInfo>>(emptyList())
    val  downloadedList = _downloadedList.asStateFlow()


    private  fun addListener(){
        repository.addDownLoadEventListener{ downloadInfo->
            _downloads.update { it + (downloadInfo.id to downloadInfo) }
        }

    }

   fun loadSampleData() {
       viewModelScope.launch {
           //facebook
           val model = repository.fetchSampleUrl(R.raw.facebook_download_sample_list)

           model.collect {
               if (it.videoList.isNotEmpty()) {

                   it.videoList.forEachIndexed { index, item ->
                       item.requestId = item.url.toUri().lastPathSegment.toString()
                   }
                   _downloadFaceBookList.value = it.videoList
               }

           }
           //TikTok
           repository.fetchSampleUrl(R.raw.tiktok_download_sample_list).collect {

               if (it.videoList.isNotEmpty()) {

                   it.videoList.forEachIndexed { index, item ->
                       item.requestId = item.url.toUri().lastPathSegment.toString()
                   }
                   _downloadTiktokList.value = it.videoList
               }

           }
       }
    }


    fun download(
        id: String,
        url: String,
        headers: HttpHeaders? = null,
        cookies: String? = null,
        downloadedInfo: VideoDownloadedInfo? = null
    ) = repository.addDownload(id, url, headers, cookies, downloadedInfo)


    fun requestFacebookDownloadUrl(requestId:String, url:String, headers: HttpHeaders?=null, cookies:String?= null){

        viewModelScope.launch {
            repository.requestFaceBookReelsDownload(url).collect { response ->

                response?.let {
                    Log.d("hbungshin","response : ${response}")
                    _downloadedList.value += listOf<VideoDownloadedInfo>(
                        VideoDownloadedInfo(
                            DownloadBland.FACEBOOK,
                            requestId,
                            response.info
                        )
                    )
                    addListener()
                    download(
                        requestId,
                        it.info.videoSD?.url ?: "",
                        headers,
                        cookies,
                        VideoDownloadedInfo(DownloadBland.FACEBOOK, requestId, response.info)
                    )
                }
            }
        }
    }

    fun requestTikTokDownloadUrl(requestId: String, url: String) {

        Log.d("hbungshin","requestId : $requestId , url : $url")
        viewModelScope.launch {

            repository.requestTikTokVideoDownload(url).collect { response ->


                response?.let {
                    _downloadedList.value += listOf<VideoDownloadedInfo>(
                        VideoDownloadedInfo(
                            DownloadBland.TIKTOK,
                            requestId,
                            response.info
                        )
                    )

                    val url = it.info.video.sd?.url
                    val headers = it.info.video.sd?.httpHeaders
                    val cookies = it.info.video.sd?.cookies

                    addListener()
                    url?.let { downloadUrl ->
                        download(
                            requestId,
                            downloadUrl,
                            headers,
                            cookies,
                            VideoDownloadedInfo(DownloadBland.TIKTOK, requestId, response.info)
                        )
                    }
                }

            }
        }
    }

    fun requestInstagramDownloadUrl(requestId:String,url:String,headers: HttpHeaders?=null,cookies:String?= null){

        viewModelScope.launch {

            repository.requestInstgramReelsDownloadUrl(url).collect { response->

                response?.video?.let{
                    val url =  it.sd?.url?:""
                   addListener()
                    download(requestId,url,headers,cookies)

                }
            }
        }
    }


    fun completeDownload(requestId:String,state: String) {

        _downloadedList.update { list ->
            list.map { item ->
                Log.d("hbungshin","item requestId : ${item.requestId} , requestId : $requestId")
                if (item.requestId == requestId && state == DownloadState.COMPLETED.name) {
                    item.copy(
                        downloadPath =requestId,
                        realDownloadPath = getRealFilePath(requestId)
                    )
                } else {
                    item
                }
            }
        }
    }

    //UI 상태 업데이트
    fun updateDownloadState(requestId: String, state: String) {

        _downloadFaceBookList.update { list ->
            list.map { item ->
                if (item.requestId == requestId) {
                    item.copy(
                        downloadState = state
                    )
                } else {
                    item
                }
            }
        }

        _downloadTiktokList.update { list ->
            list.map { item ->
                if (item.requestId == requestId && state == DownloadState.COMPLETED.name) {
                    item.copy(
                        downloadState = state
                    )
                } else {
                    item
                }
            }
        }
    }

    fun getRealFilePath(requestId:String):String?{
        val outputDir = context.getExternalFilesDir("media_output")
        if (outputDir == null || !outputDir.exists()) {
            return null
        }

        val fileName = "${requestId}.mp4"

        return File(outputDir, fileName).path

    }

    fun localDownloadList(downloadBland: DownloadBland) {

        val downloadItem = repository.getDownloadedItem().filter {
            repository.downloadedInfoFromByteArray(
                downloadBland,
                it.customData
            )?.downloadBrand == downloadBland
        }

        var list = listOf<VideoDownloadedInfo>()
        _downloadedList.value = listOf<VideoDownloadedInfo>()
        downloadItem.forEach { info ->
            val downloadInfo =
                repository.downloadedInfoFromByteArray(downloadBland, info.customData)
            if (downloadInfo != null)
                list += downloadInfo

            _downloadFaceBookList.value =
                _downloadFaceBookList.value.map { item ->
                    if (item.requestId == info.id) {
                        item.copy(
                            downloadState = DownloadState.from(info.state).toString()
                        )
                    } else {
                        item
                    }
                }
            _downloadTiktokList.value =
                _downloadTiktokList.value.map { item ->
                    if (item.requestId == info.id) {
                        item.copy(
                            downloadState = DownloadState.from(info.state).toString()
                        )
                    } else {
                        item
                    }
                }
        }

        _downloadedList.value += list

    }
}
