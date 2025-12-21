package com.sean.ratel.player.core.di.module

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider

import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DefaultDownloadIndex
import androidx.media3.exoplayer.offline.DownloadIndex
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import com.sean.ratel.player.core.data.domain.MediaStreamPlayer
import com.sean.ratel.player.core.data.domain.model.VideoDownloadNotifier
import com.sean.ratel.player.core.data.player.download.DownloadTracker
import com.sean.ratel.player.core.data.player.download.DynamicHeaderDataSourceFactory
import com.sean.ratel.player.core.data.player.download.HeaderStore
import com.sean.ratel.player.core.data.player.download.VideoDownloadManager
import com.sean.ratel.player.core.data.player.download.VideoDownloadNotifierImpl
import com.sean.ratel.player.core.data.player.download.VideoDownloadService.Companion.DOWNLOAD_CHANNEL_ID
import com.sean.ratel.player.core.data.player.media.MediaExoStreamPlayer


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors

import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ExoDownloadModule {


    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideDatabaseProvider(
        @ApplicationContext context: Context
    ): DatabaseProvider = StandaloneDatabaseProvider(context)


    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideCache(
        @ApplicationContext context: Context,
    ): Cache {
        val databaseProvider = StandaloneDatabaseProvider(context)
        val cacheDir = File(context.filesDir, "media_cache")
        return SimpleCache(cacheDir, NoOpCacheEvictor(), databaseProvider)
    }

    @Provides
    @Singleton
    fun provideDownloadExecutor(): Executor = Executors.newFixedThreadPool(4)

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideCustomDataSourceFactory(
        cache: Cache,
        defaultHttpDataSourceFactory: DefaultHttpDataSource.Factory,
        headerStore: HeaderStore
    ): DataSource.Factory {

        val dynamicHeaderFactory =
            DynamicHeaderDataSourceFactory(defaultHttpDataSourceFactory, headerStore)

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(dynamicHeaderFactory)
            .setCacheWriteDataSinkFactory(null)

    }
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideDefaultHttpDataSourceFactory(): DefaultHttpDataSource.Factory =
        DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideDownloadIndex(databaseProvider: DatabaseProvider): DownloadIndex =
        DefaultDownloadIndex(databaseProvider)


    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideDownloadManager(
        @ApplicationContext context: Context,
        databaseProvider: DatabaseProvider,
        cache: Cache,
        executor: Executor,
        dataSourceFactory: DataSource.Factory
    ): DownloadManager {

        return DownloadManager(
            context,
            databaseProvider,
            cache,
            dataSourceFactory,
            executor
        ).apply {
            maxParallelDownloads = 3   // 병렬 다운로드 개수
        }
    }
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideNotificationHelper( @ApplicationContext context: Context):DownloadNotificationHelper{

        return DownloadNotificationHelper(context,DOWNLOAD_CHANNEL_ID )

    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideMyDownloadNotifier(
        @ApplicationContext context: Context,
        helper: DownloadNotificationHelper
    ): VideoDownloadNotifier {
        return VideoDownloadNotifierImpl(context, helper)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideDownloadTracker(
        @ApplicationContext context: Context,
        downloadManager: DownloadManager,
        cache: Cache,
    ): DownloadTracker {
        val tracker = DownloadTracker(context, downloadManager,cache)
        downloadManager.addListener(tracker)
        return tracker
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideVideoDownloadManager(
        @ApplicationContext context: Context,
        downloadManager: DownloadManager,
        downloadTracker: DownloadTracker,
        headerStore: HeaderStore
    ): VideoDownloadManager =
        VideoDownloadManager(context, downloadManager, downloadTracker, headerStore)

    @OptIn(UnstableApi::class)
    @Provides
    fun provideTExoPlayer(
        @ApplicationContext context: Context,
        //userAgentProvider: UserAgentProvider,
        datasourceFactory:DataSource.Factory
    ): MediaStreamPlayer {
        return MediaExoStreamPlayer(context,datasourceFactory)
    }

}
