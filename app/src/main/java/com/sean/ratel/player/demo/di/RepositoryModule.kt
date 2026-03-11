package com.sean.ratel.player.demo.di

import android.content.Context
import com.sean.ratel.player.demo.BuildConfig
import com.sean.ratel.player.demo.data.repository.ContentFetchRepositoryImpl
import com.sean.ratel.player.demo.data.repository.YouTubeRepository
import com.sean.ratel.player.demo.data.youtube.api.LocalJsonService
import com.sean.ratel.player.demo.di.qualifier.ADAPTIVE_BANNER_UNIT_ID
import com.sean.ratel.player.demo.di.qualifier.ADMOB_UNIT_ID
import com.sean.ratel.player.demo.di.qualifier.AD_OPEN_UNIT_ID
import com.sean.ratel.player.demo.di.qualifier.BANNER_UNIT_ID
import com.sean.ratel.player.demo.di.qualifier.INTERSTITIAL_UNIT_ID
import com.sean.ratel.player.demo.di.qualifier.NATIVE_AD_UNIT_ID
import com.sean.ratel.player.demo.di.qualifier.TEST_DEVICE_HASHED_ID
import com.sean.ratel.player.demo.ui.AppAdsConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import so.smartlab.video.player.ad.admob.data.repository.AdsConfigProvider
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


    @Provides
    @Singleton
    fun provideADSdkConfigProvider(
        @ApplicationContext context: Context,
        @ADMOB_UNIT_ID admobId: String,
        @TEST_DEVICE_HASHED_ID testDeviceId: String,
        @BANNER_UNIT_ID banner: String,
        @ADAPTIVE_BANNER_UNIT_ID adaptiveUnitId: String,
        @NATIVE_AD_UNIT_ID nativeUnitId: String,
        @AD_OPEN_UNIT_ID openId: String,
        @INTERSTITIAL_UNIT_ID interstitialId: String
    ): AdsConfigProvider =
        AppAdsConfig(
            admobId,
            testDeviceId,
            banner,
            adaptiveUnitId,
            nativeUnitId,
            openId,
            interstitialId
        )


    @Provides
    @Singleton
    @ADMOB_UNIT_ID
    fun provideAdMobId(): String = BuildConfig.admobAppId


    @Provides
    @Singleton
    @TEST_DEVICE_HASHED_ID
    fun provideTestDeviceId(): String = BuildConfig.TEST_DEVICE_HASHED_ID


    @Provides
    @Singleton
    @BANNER_UNIT_ID
    fun provideBannerId(): String = BuildConfig.BANNER_UNIT_ID

    @Provides
    @Singleton
    @ADAPTIVE_BANNER_UNIT_ID
    fun provideAdaptiveBannerId(): String = BuildConfig.ADAPTIVE_BANNER_UNIT_ID


    @Provides
    @Singleton
    @AD_OPEN_UNIT_ID
    fun provideAdOpenUnitId(): String = BuildConfig.Ad_OPEN_UNIT_ID


    @Provides
    @Singleton
    @NATIVE_AD_UNIT_ID
    fun provideNativeBannerId(): String = BuildConfig.NATIVE_AD_UNIT_ID

    @Provides
    @Singleton
    @INTERSTITIAL_UNIT_ID
    fun provideInterstitialAdId(): String = BuildConfig.INTERSTITIALAd_UNIT_ID

}