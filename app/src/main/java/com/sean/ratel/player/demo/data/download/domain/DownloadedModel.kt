package com.sean.ratel.player.demo.data.download.domain


import com.sean.ratel.player.core.data.domain.model.Quality
import kotlinx.serialization.Serializable


@Serializable
data class VideoDownloadedInfo(
    val downloadBrand:DownloadBland,
    val requestId: String,
    val downloadResponse: DownloadResponse,
    var screen: List<Quality> = listOf(Quality.SD,Quality.HD,Quality.AUDIO),
    val downloadPath: String? = null,
    val realDownloadPath:String? = null
)

enum class DownloadBland{
    FACEBOOK,
    TIKTOK
}
