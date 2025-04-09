package com.sean.ratel.player.demo.data.youtube.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModel
import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModelList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalJsonService(private val context: Context) {

    suspend fun fetchContentFromJson(rawId: Int): Flow<YouTubeModel> =
        flow {
            val jsonString = context.resources.openRawResource(rawId)
                .bufferedReader()
                .use { it.readText() }


            val type = object : TypeToken<YouTubeModel>() {}.type

            val response: YouTubeModel = Gson().fromJson(jsonString, type)
            emit(response)
        }

    suspend fun fetchContentListFromJson(rawId: Int): Flow<YouTubeModelList> =
        flow {
            val jsonString = context.resources.openRawResource(rawId)
                .bufferedReader()
                .use { it.readText() }


            val type = object : TypeToken<YouTubeModelList>() {}.type

            val response: YouTubeModelList = Gson().fromJson(jsonString, type)
            emit(response)
        }
}