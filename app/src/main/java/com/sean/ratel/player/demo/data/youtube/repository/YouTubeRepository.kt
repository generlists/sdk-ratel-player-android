package com.sean.ratel.player.demo.data.youtube.repository

import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModel
import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModelList
import kotlinx.coroutines.flow.Flow

interface YouTubeRepository {
    suspend fun getLocalContent(rawId: Int): Flow<YouTubeModel>
    suspend fun getLocalContentList(rawId: Int): Flow<YouTubeModelList>
}