package com.sean.ratel.player.core.data.player.download

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultAssetLoaderFactory
import androidx.media3.transformer.DefaultDecoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.sean.ratel.player.core.data.domain.model.DownloadAppParam
import com.sean.ratel.player.core.data.domain.model.DownloadInfo
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.core.data.domain.model.toInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import so.smartlab.common.utils.log.RLog
import java.io.File
import java.io.FileOutputStream

/**
 * 다운로드 상태관리
 */
@OptIn(UnstableApi::class)
class DownloadTracker(
    private val context: Context,
    private val downloadManager: DownloadManager,
    private val simpleCache: Cache,
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
        finalException: Exception?,
    ) {
        super.onDownloadChanged(downloadManager, requestDownload, finalException)

        listeners.forEach { downloadInfo ->

            // Mp4 파일로 저장
            val downloadOptions = _downloadOptions.value[requestDownload.request.id]

            val info =
                requestDownload.toInfo(
                    downloadOptions?.downloadUrl ?: "",
                    downloadOptions?.brandName ?: "FACEBOOK",
                    downloadOptions?.quality ?: Quality.SD,
                    downloadOptions?.notificationMessage ?: "다운로드",
                )
            downloadInfo(info)

            if (downloadOptions?.isConvertMp4 == true) {
                when (requestDownload.state) {
                    Download.STATE_COMPLETED -> {
                        RLog.d(
                            "hbungshin",
                            "isConvertFile : $downloadOptions , request.id : ${requestDownload.request.id}",
                        )

                        convertCachedToMp4(
                            context,
                            requestDownload,
                            simpleCache,
                            downloadOptions.downloadUrl,
                            downloadOptions.fileName ?: "UNKNOWN_${requestDownload.request.id}.mp4",
                        )
                    }

                    else -> {
                        Unit
                    }
                }
            }
        }
    }

    // 예시 (실제 구현에는 예외 처리 및 스레딩 필요)
    private fun convertCachedToMp4(
        context: Context,
        download: Download,
        cache: Cache,
        mediaUrl: String,
        saveFileName: String,
    ) {
        val ext = getExtensionOrMp4(saveFileName)
        RLog.d("TRANSFORM", "ext : $ext , id : ${download.request.id} , saveFileName : $saveFileName")

        when (ext) {
            "m3u8" -> {
                val dir =
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "scrap_pro",
                    )

                if (!dir.exists()) dir.mkdirs()

                val upstreamFactory = DefaultHttpDataSource.Factory()

                val cacheDataSourceFactory =
                    CacheDataSource
                        .Factory()
                        .setCache(cache)
                        .setUpstreamDataSourceFactory(upstreamFactory)
                        .setCacheWriteDataSinkFactory(null)

                val hlsMediaSourceFactory = HlsMediaSource.Factory(cacheDataSourceFactory)

                val assetLoaderFactory =
                    DefaultAssetLoaderFactory(
                        context,
                        DefaultDecoderFactory.Builder(context).build(),
                        Clock.DEFAULT,
                        hlsMediaSourceFactory,
                        DataSourceBitmapLoader(context),
                    )

                val transformer =
                    Transformer
                        .Builder(context)
                        .setAudioMimeType(MimeTypes.AUDIO_AAC)
                        .setAssetLoaderFactory(assetLoaderFactory)
                        .addListener(
                            object : Transformer.Listener {
                                override fun onCompleted(
                                    composition: Composition,
                                    exportResult: ExportResult,
                                ) {
                                    RLog.d("TRANSFORM", "completed")
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val file = File(dir, "${saveFileName.substringBefore(".")}.mp4")
                                        val stableFile = waitForStableFile(file.path)
                                        RLog.d(
                                            "TRANSFORM",
                                            "${file.path} , " +
                                                "exists=${stableFile?.exists()}, " +
                                                "length=${stableFile?.length()}, " +
                                                "path=${stableFile?.path}",
                                        )
                                    }
                                }

                                override fun onError(
                                    composition: Composition,
                                    exportResult: ExportResult,
                                    exportException: ExportException,
                                ) {
                                    RLog.e(
                                        "TRANSFORM",
                                        "error=${exportException.errorCode} ${exportException.message}",
                                        exportException,
                                    )
                                }
                            },
                        ).build()

                val mediaItem =
                    MediaItem
                        .Builder()
                        .setUri(mediaUrl.toUri())
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .build()

                val editedMediaItem =
                    EditedMediaItem
                        .Builder(mediaItem)
                        .setRemoveVideo(true)
                        .build()

                val outFile = File(dir, "${saveFileName.substringBefore(".")}.mp4")
                transformer.start(editedMediaItem, outFile.path)

                val file = File(dir, saveFileName)

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf(MimeTypes.APPLICATION_MP4),
                    null,
                )
            }

            "mp4" -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val upstreamFactory = DefaultHttpDataSource.Factory()
                    val cacheDataSourceFactory =
                        CacheDataSource
                            .Factory()
                            .setCache(cache)
                            .setUpstreamDataSourceFactory(upstreamFactory)
                            .setCacheWriteDataSinkFactory(null)

                    val dataSource = cacheDataSourceFactory.createDataSource()
                    val dataSpec =
                        DataSpec
                            .Builder()
                            .setUri(download.request.uri)
                            .build()

                    val resolver = context.contentResolver
                    val uri =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val values =
                                ContentValues().apply {
                                    put(MediaStore.Downloads.DISPLAY_NAME, saveFileName)
                                    put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
                                    put(MediaStore.Downloads.IS_PENDING, 1)
                                }

                            values.put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_DOWNLOADS + "/scrap_pro",
                            )
                            values.put(MediaStore.MediaColumns.IS_PENDING, 1)

                            resolver.insert(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                values,
                            )
                        } else {
                            // Android 9 이하 (legacy)
                            val dir =
                                File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                    "scrap_pro",
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
                                null,
                            )

                            return@launch
                        }

                    try {
                        if (uri == null) return@launch

                        dataSource.open(dataSpec)

                        resolver.openOutputStream(uri)?.use { output ->
                            val buffer = ByteArray(8 * 1024)
                            while (true) {
                                val read = dataSource.read(buffer, 0, buffer.size)
                                if (read == C.RESULT_END_OF_INPUT) break
                                output.write(buffer, 0, read)
                            }
                        }

                        val updateValues =
                            ContentValues().apply {
                                put(MediaStore.Video.Media.IS_PENDING, 0)
                            }
                        resolver.update(uri, updateValues, null, null)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        dataSource.close()
                    }

                    val dir =
                        File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "scrap_pro",
                        )
                    val file = File(dir, saveFileName)
                    RLog.d("hbungshin", "isFile : ${waitForStableFile(file.path)?.exists()}")
                }
            }

            else -> {}
        }
    }

    suspend fun waitForStableFile(path: String?): File? {
        if (path.isNullOrEmpty()) return null

        val file = File(path)
        var lastLength = -1L
        var stableCount = 0

        repeat(50) {
            if (file.exists()) {
                val currentLength = file.length()

                // 1. 파일이 있고, 크기가 0보다 크며, 이전 체크 때랑 크기가 같다면?
                if (currentLength > 0 && currentLength == lastLength) {
                    stableCount++
                    if (stableCount >= 2) return file
                } else {
                    stableCount = 0 // 크기가 변하고 있으면 카운트 초기화
                }

                lastLength = currentLength
            }
            delay(100)
        }

        // 끝까지 안 나오면 시발 포기!
        return if (file.exists() && file.length() > 0) file else null
    }

    fun isDownloaded(id: String) = downloadManager.downloadIndex.getDownload(id)?.state == Download.STATE_COMPLETED

    fun fileTransfer(convertMap: Map<String, DownloadAppParam>) {
        _downloadOptions.value += convertMap
    }

    fun extractAudioToFile(
        context: Context,
        cache: Cache,
        uri: Uri,
        outputFilePath: String,
    ) {
        val (dataSource, _) = getDataSpec(cache = cache, uri = uri)

        try {
            FileOutputStream(outputFilePath).use { output ->
                val buffer = ByteArray(8 * 1024)
                while (true) {
                    val read = dataSource.read(buffer, 0, buffer.size)
                    if (read == C.RESULT_END_OF_INPUT) break
                    output.write(buffer, 0, read)
                }
            }
            dataSource.close()
        } catch (e: Exception) {
        } finally {
            dataSource.close()
        }

        MediaScannerConnection.scanFile(
            context,
            arrayOf(outputFilePath),
            arrayOf(MimeTypes.APPLICATION_M3U8),
            null,
        )
    }

    private fun getDataSpec(
        cache: Cache,
        uri: Uri,
    ): Pair<CacheDataSource, DataSpec> {
        val upstreamFactory = DefaultHttpDataSource.Factory()
        val cacheDataSourceFactory =
            CacheDataSource
                .Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setCacheWriteDataSinkFactory(null)

        val dataSource = cacheDataSourceFactory.createDataSource()
        val dataSpec =
            DataSpec
                .Builder()
                .setUri(uri)
                .build()
        return Pair(dataSource, dataSpec)
    }

    fun getExtensionOrMp4(value: String): String {
        val target =
            if (value.startsWith("http://") || value.startsWith("https://")) {
                Uri.parse(value).lastPathSegment.orEmpty()
            } else {
                value.substringBefore("?")
            }

        val ext = target.substringAfterLast('.', "").lowercase()
        return if (ext.isBlank()) "mp4" else ext
    }
}
