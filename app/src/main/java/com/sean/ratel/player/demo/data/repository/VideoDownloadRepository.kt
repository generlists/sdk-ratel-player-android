package com.sean.ratel.player.demo.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sean.ratel.player.core.data.domain.model.DownloadInfo
import com.sean.ratel.player.core.data.domain.model.HttpHeaders
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.core.data.player.download.VideoDownloadManager
import com.sean.ratel.player.demo.data.download.PixabayVideoResponse
import com.sean.ratel.player.demo.data.download.api.PixaBayApi
import com.sean.ratel.player.demo.data.download.domain.DownloadBland
import com.sean.ratel.player.demo.data.download.domain.DownloadResponseDeserializer
import com.sean.ratel.player.demo.data.download.domain.VideoDownloadedInfo
import com.sean.ratel.player.demo.di.qualifier.PixaBayApiKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class VideoDownloadRepository
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        private val pixaBayApi: PixaBayApi,
        private val videoDownloadManager: VideoDownloadManager,
        @PixaBayApiKey private val apiKey: String,
    ) {
        fun addDownload(
            id: String,
            title: String,
            url: String,
            headers: HttpHeaders? = null,
            cookies: String? = null,
            downloadedInfo: VideoDownloadedInfo? = null,
        ) {
            videoDownloadManager.startDownload(
                downloadId = id,
                downloadQuality = Quality.SD,
                downLoadUrl = url,
                brandName = DownloadBland.PIXABAY.name,
                headers = headers,
                cookies = cookies,
                convertMp4 = true,
                fileName = "DownloadBland.${DownloadBland.PIXABAY.name}.name_SD_$id.mp4",
                notificationMessage = title,
                requestExtra = downloadedInfo?.toByteArray(),
            )
        }

        fun addDownLoadEventListener(listener: (DownloadInfo) -> Unit) {
            videoDownloadManager.addDownloadEventListener(listener)
        }

        fun getDownloadedItem(): List<com.sean.ratel.player.core.data.domain.model.DownloadedInfo> =
            videoDownloadManager.getDownloadedItems()

        fun VideoDownloadedInfo.toByteArray(): ByteArray {
            val json = Gson().toJson(this)
            return json.toByteArray(Charsets.UTF_8)
        }

        fun downloadedInfoFromByteArray(
            downloadBland: DownloadBland,
            bytes: ByteArray?,
        ): VideoDownloadedInfo? {
            val json = bytes?.toString(Charsets.UTF_8)

            return try {
                // Gson 인스턴스 생성 및 Custom Deserializer 등록
                val gson =
                    GsonBuilder()
                        .registerTypeAdapter(
                            PixabayVideoResponse::class.java,
                            DownloadResponseDeserializer(downloadBland),
                        ).create()

                gson.fromJson(json, VideoDownloadedInfo::class.java)
            } catch (e: Exception) {
                null
            }
        }

        fun requestPixaBayDownload(
            query: String,
            perPage: Int = 20,
            page: Int = 1,
        ): Flow<PixabayVideoResponse?> =
            flow {
                try {
                    val response =
                        pixaBayApi.pixabaySearchVideos(
                            apiKey,
                            query,
                            perPage,
                            page,
                        )
                    emit(response)
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("TAG", "Code: ${e.code()}")
                    Log.e("TAG", "Error Body: $errorBody") // ← 실제 원인 여기 나옴
                }
            }
    }
