package com.sean.ratel.player.demo.data.download

import android.os.Parcelable
import androidx.annotation.Keep
import com.sean.ratel.player.core.data.domain.model.HttpHeaders
import com.sean.ratel.player.demo.data.download.domain.DownloadResponse
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class TikTokVideoResponse(
    var success: String,
    var code: Int,
    var info: TikTokDownloadInfo,
) : Parcelable

@Parcelize
@Keep
data class TikTokDownloadInfo(
    override var id: String,
    override val title: String,
    override val duration: Double,
    override val description: String,
    override val thumbnail: String,
    override val originalUrl: String,
    override val uploader: String,
    override val uploaderId: String,
    override val uploadDate: String,
    override val viewCount: Long,
    val likeCount: Long?,
    val commentCount: Long?,
    val repostCount: Long?,
    val video: VideoOptions,
) : DownloadResponse,
    Parcelable

@Parcelize
@Keep
data class VideoOptions(
    val hd: VideoFormatS?,
    val sd: VideoFormatS?,
    val watermarked: VideoFormat?,
) : Parcelable

@Parcelize
@Keep
data class VideoFormatS(
    val url: String?,
    val httpHeaders: HttpHeaders?,
    val cookies: String?,
) : Parcelable
