package com.sean.ratel.player.core.data.player.download

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import com.sean.ratel.player.core.data.domain.model.DownloadInfo
import com.sean.ratel.player.core.data.domain.model.toInfo
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileOutputStream

/**
 * 다운로드 상태관리
 */
@OptIn(UnstableApi::class)
class DownloadTracker(
    private val context: Context,
    private val downloadManager: DownloadManager,
    private val simpleCache: Cache // Cache 인터페이스로 주입받습니다
) : DownloadManager.Listener {

    private val listeners = mutableListOf<(DownloadInfo) -> Unit>()
    private val _isConverting = MutableStateFlow<Map<String, Boolean>>(mapOf())

    fun addListener(listener: (DownloadInfo) -> Unit) {
        listeners.add(listener)
    }

    override fun onDownloadChanged(
        downloadManager: DownloadManager,
        requestDownload: Download,
        finalException: Exception?
    ) {
        super.onDownloadChanged(downloadManager, requestDownload, finalException)

        listeners.forEach { downloadInfo ->

            val info = requestDownload.toInfo()


            downloadInfo(info)
            //Mp4 파일로 저장

            val isConvertFile = _isConverting.value.get(requestDownload.request.id)

            if (isConvertFile == true) {
                when (requestDownload.state) {
                    Download.STATE_COMPLETED -> {
                        Log.d(
                            "hbungshin",
                            "isConvertFile : $isConvertFile , request.id : ${requestDownload.request.id}"
                        )
                        convertCachedToMp4(
                            requestDownload,
                            simpleCache,
                            createMp4File(context, requestDownload.request.id)
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    // 예시 (실제 구현에는 예외 처리 및 스레딩 필요)
    private fun convertCachedToMp4(download: Download, cache: Cache, outputFile: File) {
        val upstreamFactory = DefaultHttpDataSource.Factory()
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null) // 쓰기 캐시 비활성화

        val dataSpec = DataSpec(download.request.uri)

        // 실제 데이터 소스 생성
        val dataSource = cacheDataSourceFactory.createDataSource()

        try {
            dataSource.open(dataSpec)
            val outputStream = FileOutputStream(outputFile)
            val buffer = ByteArray(1024 * 4)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (totalBytesRead < download.contentLength) {
                bytesRead = dataSource.read(buffer, 0, buffer.size)
                if (bytesRead == C.RESULT_END_OF_INPUT) break

                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
            }
            outputStream.close()
            dataSource.close()

            // 성공적으로 변환되면 캐시를 삭제
            // DownloadService.removeDownload() 등을 사용
        } catch (e: Exception) {
            // 오류 처리
            e.printStackTrace()
        }
    }

    fun isDownloaded(id: String): Boolean {
        return downloadManager.downloadIndex.getDownload(id)?.state == Download.STATE_COMPLETED
    }

    fun isConvertMp4(isConvert: Map<String, Boolean>) {
        _isConverting.value += isConvert

    }


    fun createMp4File(context: Context, downloadId: String): File {
        val outputDir = context.getExternalFilesDir("media_output")

        if (outputDir == null || !outputDir.exists()) {
            outputDir?.mkdirs()
        }

        val safeFileName = downloadId.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        val mp4FileName = "${safeFileName}.mp4"

        // 3. 최종 File 객체 반환
        return File(outputDir, mp4FileName)
    }
}