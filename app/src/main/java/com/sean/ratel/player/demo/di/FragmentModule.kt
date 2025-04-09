package com.sean.ratel.player.demo.di

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.sean.ratel.player.demo.di.qualifier.NotControl
import com.sean.ratel.player.demo.di.qualifier.WithControl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @NotControl
    @Provides
    @FragmentScoped
    fun provideNotIFramePlayerOption(
    ): IFramePlayerOptions {
        return IFramePlayerOptions.Builder().controls(0).build()
    }
    @WithControl
    @Provides
    @FragmentScoped
    fun provideWidthIFramePlayerOption(
    ): IFramePlayerOptions {
        return IFramePlayerOptions.Builder().controls(1)
        .fullscreen(1) // enable full screen button
        .build()
    }

    @Provides
    @FragmentScoped
    fun provideYouTubePlayerTracker(
    ): YouTubePlayerTracker {
        return YouTubePlayerTracker()
    }
}