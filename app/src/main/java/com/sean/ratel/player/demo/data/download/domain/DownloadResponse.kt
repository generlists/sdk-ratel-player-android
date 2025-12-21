package com.sean.ratel.player.demo.data.download.domain

interface   DownloadResponse{
    var id: String
    val title: String
    val duration: Double?
    val description: String
    val thumbnail: String
    val originalUrl: String
    val uploader: String
    val uploaderId: String
    val uploadDate: String
    val viewCount: Long
}
