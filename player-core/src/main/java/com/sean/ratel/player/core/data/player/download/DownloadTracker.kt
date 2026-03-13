package com.sean.ratel.player.core.data.player.download

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import com.sean.ratel.player.core.data.domain.model.DownloadAppParam
import com.sean.ratel.player.core.data.domain.model.DownloadInfo
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.core.data.domain.model.toInfo
import com.sean.ratel.player.utils.log.RLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * 다운로드 상태관리
 */
@OptIn(UnstableApi::class)
class DownloadTracker(
    private val context: Context,
    private val downloadManager: DownloadManager,
    private val simpleCache: Cache
) : DownloadManager.Listener {

    private val listeners = mutableListOf<(DownloadInfo) -> Unit>()
    private val _downloadOptions =
        MutableStateFlow<Map<String, DownloadAppParam>>(mapOf())
    val downloadOptions = _downloadOptions.asStateFlow()

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

            //Mp4 파일로 저장
            val downloadOptions = _downloadOptions.value[requestDownload.request.id]

            val info = requestDownload.toInfo(downloadOptions?.brandName?:"FACEBOOK", downloadOptions?.quality?: Quality.SD, downloadOptions?.notificationMessage?:"다운로드")
            downloadInfo(info)

            if (downloadOptions?.isConvertMp4 == true) {
                when (requestDownload.state) {
                    Download.STATE_COMPLETED -> {
                        RLog.d(
                            "hbungshin",
                            "isConvertFile : $downloadOptions , request.id : ${requestDownload.request.id}"
                        )
                        convertCachedToMp4(
                            context,
                            requestDownload,
                            simpleCache,
                            downloadOptions.fileName ?: "UNKNOWN_${requestDownload.request.id}.mp4"
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    // 예시 (실제 구현에는 예외 처리 및 스레딩 필요)
    private fun convertCachedToMp4(
        context: Context,
        download: Download,
        cache: Cache,
        saveFileName: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {

            val upstreamFactory = DefaultHttpDataSource.Factory()
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setCacheWriteDataSinkFactory(null)


            val dataSource = cacheDataSourceFactory.createDataSource()
            val dataSpec = DataSpec.Builder()
                .setUri(download.request.uri)
                .build()


            val resolver = context.contentResolver
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, saveFileName)
                    put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/scrap_pro"
                )
                values.put(MediaStore.MediaColumns.IS_PENDING, 1)

                resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )
            } else {
                // Android 9 이하 (legacy)
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "scrap_pro"
                )

                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, saveFileName)

                dataSource.open(dataSpec)

                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    while (true) {
                        val read = dataSource.read(buffer, 0, buffer.size)
                        if (read == C.RESULT_END_OF_INPUT) break
                        output.write(buffer, 0, read)
                    }
                }

                dataSource.close()

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )

                return@launch
            }

            try {

                if(uri == null ) return@launch

                dataSource.open(dataSpec)

                resolver.openOutputStream(uri)?.use { output ->
                    val buffer = ByteArray(8 * 1024)
                    while (true) {
                        val read = dataSource.read(buffer, 0, buffer.size)
                        if (read == C.RESULT_END_OF_INPUT) break
                        output.write(buffer, 0, read)
                    }
                }

                val updateValues = ContentValues().apply {
                    put(MediaStore.Video.Media.IS_PENDING, 0)
                }
                resolver.update(uri, updateValues, null, null)


            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                dataSource.close()
            }
        }
    }

    fun isDownloaded(id: String): Boolean {
        return downloadManager.downloadIndex.getDownload(id)?.state == Download.STATE_COMPLETED
    }

    fun mp4ConvertMp4(convertMap: Map<String, DownloadAppParam>) {
        _downloadOptions.value += convertMap

    }

}

