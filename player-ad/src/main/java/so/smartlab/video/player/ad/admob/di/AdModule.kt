package so.smartlab.video.player.ad.admob.di

import android.content.Context
import androidx.annotation.OptIn
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.UnstableApi
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import so.smartlab.video.player.ad.admob.AdsSdk
import so.smartlab.video.player.ad.admob.data.AppOpenAdManager
import so.smartlab.video.player.ad.admob.data.GoogleMobileAdsConsentManager
import so.smartlab.video.player.ad.admob.data.repository.AdMobRepository
import so.smartlab.video.player.ad.admob.data.repository.AdsConfigProvider

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdModule {

    @Provides
    @Singleton
    fun provideGoogleMobileAdsContentManager(
        @ApplicationContext context: Context,
        adsConfigProvider: AdsConfigProvider
    ): GoogleMobileAdsConsentManager {

        return GoogleMobileAdsConsentManager(context,adsConfigProvider.testDeviceHashedId)
    }

    @Provides
    @OptIn(UnstableApi::class)
    fun providerAppOpenAdManager(
        config: AdsConfigProvider,
        googleMobileAdsConsentManager: GoogleMobileAdsConsentManager): AppOpenAdManager =
        AppOpenAdManager(config,googleMobileAdsConsentManager)

    @Module
    @InstallIn(SingletonComponent::class)
    internal object AdsInternalModule {

        @Provides
        @Singleton
        fun provideAdsSdk(
            @ApplicationContext context: Context,
            config: AdsConfigProvider,
            repository: AdMobRepository
        ): AdsSdk {
            return AdsSdk(context,repository,config)
        }
    }

}