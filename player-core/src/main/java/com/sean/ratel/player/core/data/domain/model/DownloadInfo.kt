package com.sean.ratel.player.core.data.domain.model

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download


data class DownloadInfo(
    val id: String,
    val mimeType: String?,
    val state: DownloadState,
    val progress: Int,
    val bytesDownloaded: Long,
    val contentLength: Long,
)

data class DownloadedInfo(
    val id: String,
    val originalUri: String,
    val state: Int,
    val percent: Float,
    val  customData:ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DownloadedInfo

        if (percent != other.percent) return false
        if (id != other.id) return false
        if (originalUri != other.originalUri) return false
        if (state != other.state) return false
        if (!customData.contentEquals(other.customData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = percent.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + originalUri.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + customData.contentHashCode()
        return result
    }

}

enum class DownloadState(val state: Int) {
    IDLE(-1),
    QUEUED(0),
    STOPPED(1),
    DOWNLOADING(2),
    COMPLETED(3),
    FAILED(4),
    REMOVING(5),
    RESTARTED(6),
    PAUSED(7); // 커스텀 상태 (앱 전용)
    companion object {
        fun from(state: Int?): DownloadState =
            values().firstOrNull { it.state == state } ?: IDLE
    }
}

@OptIn(UnstableApi::class)
fun Download.toInfo(): DownloadInfo {
    val progress =
        if (contentLength > 0) ((bytesDownloaded * 100) / contentLength).toInt()
        else 0

    return DownloadInfo(
        id = request.id,
        mimeType = request.mimeType,
        state = when (state) {
            Download.STATE_QUEUED -> DownloadState.QUEUED
            Download.STATE_DOWNLOADING -> DownloadState.DOWNLOADING
            Download.STATE_COMPLETED -> DownloadState.COMPLETED
            Download.STATE_FAILED -> DownloadState.FAILED
            Download.STATE_REMOVING -> DownloadState.REMOVING
            Download.STATE_STOPPED -> DownloadState.STOPPED
            Download.STATE_RESTARTING -> DownloadState.RESTARTED
            else -> DownloadState.PAUSED
        },
        progress = progress,
        bytesDownloaded = bytesDownloaded,
        contentLength = contentLength
    )
}