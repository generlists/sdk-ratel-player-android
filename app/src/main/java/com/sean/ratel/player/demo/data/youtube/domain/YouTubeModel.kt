package com.sean.ratel.player.demo.data.youtube.domain

import androidx.annotation.Keep

@Keep
data class YouTubeModel(
    val exampleTitle:String,
    val channelModel: ChannelModel,
    val videoModel: VideoModel)

@Keep
data class YouTubeModelList(
    val exampleTitle:String,
    val videoList:List<YouTubeModel>
)