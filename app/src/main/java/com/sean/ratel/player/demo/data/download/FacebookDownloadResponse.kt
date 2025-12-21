package com.sean.ratel.player.demo.data.download

import android.os.Parcelable
import androidx.annotation.Keep
import com.sean.ratel.player.demo.data.download.domain.DownloadResponse
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class FacebookDownloadResponse(
    var success: String,
    var code: Int,
    var info : FacebookDownloadInfo):Parcelable

@Parcelize
@Keep
data class FacebookDownloadInfo(
    override var id: String,
    override val title: String,
    override val duration: Double?,
    override val description: String,
    override val thumbnail: String,
    override val originalUrl: String,
    override val uploader: String,
    override val uploaderId: String,
    override val uploadDate: String,
    override val viewCount: Long,

    val hasDrm: String?,
    val videoSD: VideoFormat?,
    val videoHD: VideoFormat?,
    val audio: AudioFormat?

): DownloadResponse,Parcelable


@Parcelize
@Keep
data class VideoFormat(
    val url: String,
    val ext: String?
): Parcelable
@Parcelize
@Keep
data class AudioFormat(
    val url: String,
    val ext: String?
): Parcelable