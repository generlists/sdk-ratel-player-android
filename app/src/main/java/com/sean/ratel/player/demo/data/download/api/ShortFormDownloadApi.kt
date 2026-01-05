package com.sean.ratel.player.demo.data.download.api

import com.sean.ratel.player.demo.data.download.FacebookDownloadResponse
import com.sean.ratel.player.demo.data.download.InstagramVideoResponse
import com.sean.ratel.player.demo.data.download.TikTokVideoResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ShortFormDownloadApi {

    @POST("/facebook_reels_download")
    suspend fun requestFaceBookReelsDownloadUrl(
        @Query("url") url: String,
    ): FacebookDownloadResponse?


    @GET("/instagram_reels_download")
    suspend fun requestInstagramReelsDownloadUrl(
        @Query("url") url: String,
    ): InstagramVideoResponse?


    @GET("/tiktok_video_download")
    suspend fun requestTikTokVideoDownloadUrl(
        @Query("url") url: String,
    ): TikTokVideoResponse?


}