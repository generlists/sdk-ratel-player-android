package com.sean.ratel.player.demo.di

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.sean.ratel.player.demo.data.youtube.domain.YouTubePlayerOptions
import com.sean.ratel.player.demo.di.qualifier.AutoPlay
import com.sean.ratel.player.demo.di.qualifier.CCLangPref
import com.sean.ratel.player.demo.di.qualifier.CCLoadPolicy
import com.sean.ratel.player.demo.di.qualifier.Control
import com.sean.ratel.player.demo.di.qualifier.EnableJsApi
import com.sean.ratel.player.demo.di.qualifier.FS
import com.sean.ratel.player.demo.di.qualifier.IVLoadPolicy
import com.sean.ratel.player.demo.di.qualifier.Mute
import com.sean.ratel.player.demo.di.qualifier.NotControl
import com.sean.ratel.player.demo.di.qualifier.Rel
import com.sean.ratel.player.demo.di.qualifier.WithControl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import java.util.Locale

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {
    @NotControl
    @Provides
    @FragmentScoped
    fun provideNotIFramePlayerOption(playerOptions: YouTubePlayerOptions): IFramePlayerOptions =
        IFramePlayerOptions
            .Builder()
            .controls(playerOptions.control)
            .ccLoadPolicy(0)
            .langPref(playerOptions.ccLangPref)
            .build()

    @WithControl
    @Provides
    @FragmentScoped
    fun provideWidthIFramePlayerOption(playerOptions: YouTubePlayerOptions): IFramePlayerOptions =
        IFramePlayerOptions
            .Builder()
            .controls(playerOptions.control)
            .fullscreen(playerOptions.fs) // enable full screen button
            .langPref(playerOptions.ccLangPref)
            .build()

    @Provides
    @FragmentScoped
    fun provideYouTubePlayerTracker(): YouTubePlayerTracker = YouTubePlayerTracker()

    @Provides
    @FragmentScoped
    @AutoPlay
    fun provideAutoPlay(): Int = 0

    @Provides
    @Mute
    @FragmentScoped
    fun provideMute(): Int = 0

    @Provides
    @Control
    @FragmentScoped
    fun provideControl(): Int = 0

    @Provides
    @EnableJsApi
    @FragmentScoped
    fun provideEnableJsApi(): Int = 0

    @Provides
    @FS
    @FragmentScoped
    fun provideFS(): Int = 0

    @Provides
    @Rel
    @FragmentScoped
    fun provideRel(): Int = 0

    @Provides
    @IVLoadPolicy
    @FragmentScoped
    fun provideIVLoadPolicy(): Int = 0

    @Provides
    @CCLoadPolicy
    @FragmentScoped
    fun provideCCLoadPolicy(): Int = 3

    @Provides
    @CCLangPref
    @FragmentScoped
    fun provideCCLoadCCLangPref(): String = Locale.getDefault().language
}
