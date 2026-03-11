package com.sean.ratel.player.core.data.player.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.sean.ratel.player.core.data.domain.model.DownloadAppParam
import com.sean.ratel.player.core.data.domain.model.DownloadInfo
import com.sean.ratel.player.core.data.domain.model.DownloadedInfo
import com.sean.ratel.player.core.data.domain.model.HttpHeaders
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.utils.log.RLog
import com.sean.ratel.player.utils.log.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(UnstableApi::class)
class VideoDownloadManager @Inject constructor(
    @ApplicationContext val context: Context,
    private val downloadManager: DownloadManager,
    private val downloadTracker: DownloadTracker,
    private val headerStore: HeaderStore
) {

    fun startDownload(
        downloadId: String,
        downloadQuality: Quality,
        downLoadUrl: String,
        brandName:String ="FACEBOOK",
        headers: HttpHeaders? = null,
        cookies: String? = null,
        convertMp4: Boolean = true,
        fileName:String? = null,
        notificationMessage:String? = null,
        requestExtra: ByteArray? = null
    ) {

        val request = DownloadRequest.Builder(downloadId, downLoadUrl.toUri())
            .setMimeType("video/mp4")
            .setCustomCacheKey(downloadId + "_" + downloadQuality.name)
            .setData(requestExtra)
            .build()
        setMetaData(downloadId, headers, cookies)

        addDownload(request)

        DownloadService.sendAddDownload(
            context,
            VideoDownloadService::class.java,
            request,
            true
        )

        RLog.d("Downloader", "custom key : ${downloadId}")

        downloadTracker.mp4ConvertMp4(
            mapOf(
                downloadId to (DownloadAppParam(
                    brandName = brandName,
                    quality = downloadQuality,
                    isConvertMp4 = convertMp4,
                    fileName = fileName,
                    notificationMessage = notificationMessage ?: "Download"
                ))
            )
        )
    }

    fun addDownloadEventListener(listener: (DownloadInfo) -> Unit) {
        downloadTracker.addListener(listener)
    }

    /** 다운로드 추가 */
    fun addDownload(request: DownloadRequest) {
        downloadManager.addDownload(request)
    }

    /** 다운로드 제거 */
    fun removeDownload(downloadId: String) {
        downloadManager.removeDownload(downloadId)
    }

    /** 모든 다운로드 일시정지 */
    fun pauseAll() {
        downloadManager.pauseDownloads()
    }

    /** 모든 다운로드 재개 */
    fun resumeAll() {
        downloadManager.resumeDownloads()
    }

    /** 특정 다운로드 중지 */
    fun stopDownload(downloadId: String, stopReason: Int) {
        downloadManager.setStopReason(downloadId, stopReason)
    }

    /** 특정 다운로드 재개 */
    fun restartDownload(downloadId: String) {
        downloadManager.setStopReason(downloadId, Download.STOP_REASON_NONE)
        downloadManager.resumeDownloads()
    }

    /** 다운로드 요청 변경 (품질 변경 등) */
    fun updateDownloadRequest(downloadId: String, newRequest: DownloadRequest) {
        downloadManager.addDownload(newRequest)   // 기존과 같은 downloadId 이면 update 효과
    }

    /** 현재 다운로드 목록 */
    fun getCurrentDownloads(): List<Download> =
        downloadManager.currentDownloads

    /** 특정 다운로드 상태 조회 */
    fun find(downloadId: String): Download? =
        downloadManager.currentDownloads.firstOrNull { it.request.id == downloadId }

    /** 다운로드 리스너 등록 */
    fun addListener(listener: DownloadManager.Listener) {
        downloadManager.addListener(listener)
    }

    // -------------------------------------------------------------------------
    // DOWNLOAD SERVICE 제어 기능 (startForeground, sendXXX)
    // -------------------------------------------------------------------------

    /** 서비스 실행 */
    fun startService() {
        DownloadService.startForeground(context, VideoDownloadService::class.java)
    }

    /** 서비스에서 다운로드 실행 (Service + AddDownload 한번에) */
    fun enqueueWithService(request: DownloadRequest, foreground: Boolean = true) {
        DownloadService.sendAddDownload(
            context,
            VideoDownloadService::class.java,
            request,
            foreground
        )
    }

    /** 서비스 기반 제거 */
    fun removeWithService(downloadId: String) {
        DownloadService.sendRemoveDownload(
            context,
            VideoDownloadService::class.java,
            downloadId,
            true
        )
    }

    /** 서비스 기반 일시정지 */
    fun pauseWithService() {
        DownloadService.sendPauseDownloads(context, VideoDownloadService::class.java, true)
    }

    /** 서비스 기반 재개 */
    fun resumeWithService() {
        DownloadService.sendResumeDownloads(context, VideoDownloadService::class.java, true)
    }


    private fun setMetaData(url: String, headers: HttpHeaders? = null, cookies: String? = null) {
        if (cookies != null) {
            val chainToken = Utils.extractTtChainToken(cookies)

            headers?.apply {
                val header = mapOf<String, String?>(
                    "User-Agent" to `User-Agent`,
                    "Accept" to Accept,
                    "Accept-Language" to `Accept-Language`,
                    "Sec-Fetch-Mode" to `Sec-Fetch-Mode`,
                    "Referer" to Referer,
                    "Cookie" to "tt_chain_token=$chainToken"

                )
                headerStore.saveHeaders(url, header)
            }
        }
        RLog.d("hbungshin", "cookies : $cookies")
    }


    fun getDownloadedItems(originRequestId: String): DownloadedInfo? {
        val index = downloadManager.downloadIndex
        val cursor = index.getDownloads(Download.STATE_COMPLETED)

        cursor.use {

            while (it.moveToNext()) {
                val download = it.download
                val requestId = it.download.request.id

                if (requestId == originRequestId) {
                    // 원본(다운로드 요청에 넣었던) URI
                    val originalUri = download.request.uri

                    // DownloadRequest.data 에 네가 넣어둔 메타 (videoId 등)도 꺼낼 수 있음
                    val customData = download.request.data // ByteArray?

                    return DownloadedInfo(
                        id = download.request.id,
                        originalUri = originalUri.toString(),
                        state = download.state,
                        percent = download.percentDownloaded,
                        customData = customData
                    )
                }
            }
        }
        return null
    }


    fun getDownloadedItems(): List<DownloadedInfo> {
        val index = downloadManager.downloadIndex
        val cursor = index.getDownloads(Download.STATE_COMPLETED)

        val result = mutableListOf<DownloadedInfo>()
        cursor.use {
            while (it.moveToNext()) {
                val download = it.download

                // 원본(다운로드 요청에 넣었던) URI
                val originalUri = download.request.uri

                val customData = download.request.data // ByteArray?

                result += DownloadedInfo(
                    id = download.request.id,
                    originalUri = originalUri.toString(),
                    state = download.state,
                    percent = download.percentDownloaded,
                    customData = customData
                )
            }
        }
        return result
    }


}