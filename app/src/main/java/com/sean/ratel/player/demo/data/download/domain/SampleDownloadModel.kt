package com.sean.ratel.player.demo.data.download.domain

import androidx.annotation.Keep

@Keep
data class DownloadModel(
    val videoList: List<SampleDownloadModel>,
    val exampleTitle: String,
)

@Keep
data class SampleDownloadModel(
    var requestId: String,
    val url: String,
    var downloadState: String?,
)
