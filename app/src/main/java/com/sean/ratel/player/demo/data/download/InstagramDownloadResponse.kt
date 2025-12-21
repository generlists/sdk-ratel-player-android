package com.sean.ratel.player.demo.data.download


data class InstagramVideoResponse(
    val id: String,
    val title: String,
    val description:String,
    val uploader: String?,
    val uploaderId: String?,
    val duration: Int,
    val likeCount:String??,
    val thumbnails:List<ThumbNail>,
    val timestamp:Long,

    val commentCount:String?,
    val channel:String?,


    val httpHeader:HttpHeaderes,
    val video: VideoOptiones,
    val originUrl:String,
    val hasDrm:String,
)

data class ThumbNail(
    val url:String,
    val width:Int,
    val height:Int,
    val id:String,
    val resolution:String

)

data class VideoOptiones(
    val hd: VideoFormates?,
    val sd: VideoFormates?,
)

data class VideoFormates(
    val url: String?,
    val ext:String?

)

data class HttpHeaderes(
    val `User-Agent`: String?,
    val Accept: String?,
    val `Accept-Language`: String?,
    val `Sec-Fetch-Mode`: String?,
    val Referer: String?
)