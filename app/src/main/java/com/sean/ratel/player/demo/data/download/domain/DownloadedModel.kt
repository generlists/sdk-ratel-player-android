package com.sean.ratel.player.demo.data.download.domain


import kotlinx.serialization.Serializable


@Serializable
data class VideoDownloadedInfo(
    val downloadBrand:DownloadBland,
    val requestId: String,
    val downloadResponse: DownloadResponse,
    val downloadPath: String? = null,
    val realDownloadPath:String? = null
)

enum class DownloadBland{
    FACEBOOK,
    TIKTOK
}
