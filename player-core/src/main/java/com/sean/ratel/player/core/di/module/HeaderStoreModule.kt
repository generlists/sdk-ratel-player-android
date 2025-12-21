package com.sean.ratel.player.core.di.module

import com.sean.ratel.player.core.data.player.download.HeaderStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HeaderStoreModule {

    @Provides
    @Singleton
    fun provideHeaderStore(): HeaderStore = HeaderStore()
}