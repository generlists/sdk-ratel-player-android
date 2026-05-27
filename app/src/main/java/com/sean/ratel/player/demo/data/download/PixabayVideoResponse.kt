package com.sean.ratel.player.demo.data.download

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class PixabayVideoResponse(
    val hits: List<VideoHit>,
) : Parcelable

@Parcelize
@Keep
data class VideoHit(
    var id: String,
    val tags: String,
    val name: String,
    var downloadState: String?,
    val videos: VideoItem,
) : Parcelable

@Parcelize
@Keep
data class VideoItem(
    val medium: Video,
    val large: Video,
    val small: Video,
    val tiny: Video,
) : Parcelable

@Parcelize
@Keep
data class Video(
    val url: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val thumbnail: String,
) : Parcelable
