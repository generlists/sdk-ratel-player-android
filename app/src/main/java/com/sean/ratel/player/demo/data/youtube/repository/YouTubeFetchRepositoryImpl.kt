package com.sean.ratel.player.demo.data.youtube.repository

import com.sean.ratel.player.demo.data.youtube.api.LocalJsonService
import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModel
import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModelList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentFetchRepositoryImpl
    @Inject
    constructor(private val localJsonService: LocalJsonService) : YouTubeRepository {

    override suspend fun getLocalContent(rawId: Int): Flow<YouTubeModel> = localJsonService.fetchContentFromJson(rawId)

    override suspend fun getLocalContentList(rawId: Int): Flow<YouTubeModelList> = localJsonService.fetchContentListFromJson(rawId)

}