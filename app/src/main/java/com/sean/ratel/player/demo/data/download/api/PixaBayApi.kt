package com.sean.ratel.player.demo.data.download.api

import com.sean.ratel.player.demo.data.download.PixabayVideoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PixaBayApi {
    @GET("videos/")
    suspend fun pixabaySearchVideos(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1,
    ): PixabayVideoResponse
}
