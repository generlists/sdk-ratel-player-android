package com.sean.ratel.player.demo.di

import android.content.Context
import com.sean.ratel.player.demo.data.youtube.api.LocalJsonService
import com.sean.ratel.player.demo.data.youtube.repository.ContentFetchRepositoryImpl
import com.sean.ratel.player.demo.data.youtube.repository.YouTubeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object  RepositoryModule {

    @Provides
    @Singleton
    fun provideLocalJsonService(@ApplicationContext context: Context): LocalJsonService =
        LocalJsonService(context)

    @Provides
    @Singleton
    fun provideContentRepository(
        localJsonService: LocalJsonService
    ): YouTubeRepository = ContentFetchRepositoryImpl(localJsonService)
}