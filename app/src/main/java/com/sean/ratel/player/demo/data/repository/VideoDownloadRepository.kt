package com.sean.ratel.player.demo.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.sean.ratel.player.core.data.domain.model.DownloadInfo
import com.sean.ratel.player.core.data.domain.model.HttpHeaders
import com.sean.ratel.player.core.data.player.download.VideoDownloadManager
import com.sean.ratel.player.demo.data.download.FacebookDownloadResponse
import com.sean.ratel.player.demo.data.download.InstagramVideoResponse
import com.sean.ratel.player.demo.data.download.TikTokVideoResponse
import com.sean.ratel.player.demo.data.download.api.ShortFormDownloadApi
import com.sean.ratel.player.demo.data.download.domain.DownloadBland
import com.sean.ratel.player.demo.data.download.domain.DownloadModel
import com.sean.ratel.player.demo.data.download.domain.DownloadResponse
import com.sean.ratel.player.demo.data.download.domain.DownloadResponseDeserializer
import com.sean.ratel.player.demo.data.download.domain.VideoDownloadedInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class VideoDownloadRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val videoDownloadApi: ShortFormDownloadApi,
    private val videoDownloadManager: VideoDownloadManager
) {


    suspend fun fetchSampleUrl(rawId: Int): Flow<DownloadModel> =
        flow {
            val jsonString = context.resources.openRawResource(rawId)
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<DownloadModel>() {}.type

            val response: DownloadModel = Gson().fromJson(jsonString, type)
            emit(response)
        }


    fun addDownload(
        id: String,
        url: String,
        headers: HttpHeaders? = null,
        cookies: String? = null,
        downloadedInfo: VideoDownloadedInfo? = null
    ) {

        videoDownloadManager.startDownload(
            downloadId = id,
            downLoadUrl = url,
            headers = headers,
            cookies = cookies,
            requestExtra = downloadedInfo?.toByteArray()
        )
    }

    fun addDownLoadEventListener(listener: (DownloadInfo) -> Unit) {
        videoDownloadManager.addDownloadEventListener(listener)
    }

    fun requestFaceBookReelsDownload(url: String): Flow<FacebookDownloadResponse?> =
        flow {
            val response = videoDownloadApi.requestFaceBookReelsDownloadUrl(url)
            emit(response)
        }

    fun requestTikTokVideoDownload(url: String): Flow<TikTokVideoResponse?> =

        flow {
            val response = videoDownloadApi.requestTikTokVideoDownloadUrl(url)
            emit(response)
        }

    fun requestInstgramReelsDownloadUrl(url: String): Flow<InstagramVideoResponse?> =

        flow {
            val response = videoDownloadApi.requestInstagramReelsDownloadUrl(url)
            emit(response)

        }

    fun getDownloadedItem(): List<com.sean.ratel.player.core.data.domain.model.DownloadedInfo> =
        videoDownloadManager.getDownloadedItems()


    fun VideoDownloadedInfo.toByteArray(): ByteArray {
        val json = Gson().toJson(this)
        return json.toByteArray(Charsets.UTF_8)
    }

    fun downloadedInfoFromByteArray(
        downloadBland: DownloadBland,
        bytes: ByteArray?
    ): VideoDownloadedInfo? {
        val json = bytes?.toString(Charsets.UTF_8)

        return try {
            // Gson 인스턴스 생성 및 Custom Deserializer 등록
            val gson = GsonBuilder()
                .registerTypeAdapter(
                    DownloadResponse::class.java,
                    DownloadResponseDeserializer(downloadBland)
                )
                .create()

            gson.fromJson(json, VideoDownloadedInfo::class.java)
        } catch (e: Exception) {
            Log.e("VideoDownloadRepository", "$e")
            null
        }
    }


}