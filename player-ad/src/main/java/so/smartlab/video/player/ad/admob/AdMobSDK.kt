package so.smartlab.video.player.ad.admob

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import so.smartlab.video.player.ad.admob.data.model.AdMobAppOpenAdState
import so.smartlab.video.player.ad.admob.data.model.AdMobBannerState
import so.smartlab.video.player.ad.admob.data.model.AdMobInitState
import so.smartlab.video.player.ad.admob.data.model.AdMobInterstitialAdState
import so.smartlab.video.player.ad.admob.data.model.AdMobNativeAdState
import so.smartlab.video.player.ad.admob.data.repository.AdMobRepository
import so.smartlab.video.player.ad.admob.data.repository.AdsConfigProvider
import so.smartlab.video.player.ad.admob.ui.utils.UiUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsSdk @Inject internal constructor(
    val context: Context,
    private val adMobRepository: AdMobRepository,
    val config: AdsConfigProvider
) {

    //init
    val adInitState: StateFlow<AdMobInitState> =
        adMobRepository.adInitState

    val adFixedBannerState: StateFlow<AdMobBannerState> =
        adMobRepository.adFixedBannerState

    val adAdaptiveBannerState: StateFlow<AdMobBannerState> =
        adMobRepository.adAdaptiveBannerState

    val adNativeState: StateFlow<AdMobNativeAdState> =
        adMobRepository.adNativeState

    //상태 공유
    val interstitialState: SharedFlow<AdMobInterstitialAdState> =
        adMobRepository.adInterstitialAdEvent


    val appOpenState: StateFlow<AdMobAppOpenAdState> =
        adMobRepository.adApOpenAdState

    //Admob 초기화
    fun initAdMob(activity: Activity) {
        adMobRepository.setActivityContext(activity)
        adMobRepository.initAdMob()
    }

    suspend fun requestBannerAdView(
        activity: Activity,
        unitId: String
    ) {
        adMobRepository.setActivityContext(activity)

        adMobRepository.requestAdBanner(
            unitId = unitId,
            adSize = UiUtils.adSize(activity)
        )
    }

    suspend fun requestInLineBannerAdView(
        activity: Activity,
        unitId: String
    ) {
        adMobRepository.setActivityContext(activity)
        adMobRepository.requestAdaptiveInlineBanner(
            unitId = unitId,
            adSize = UiUtils.adInLineAdaptiveBannerSize(activity)
        )
    }

    suspend fun requestNativeAdView(
        activity: Activity,
        unitId: String
    ) {
        adMobRepository.setActivityContext(activity)

        adMobRepository.requestNativeAd(
            unitId = unitId
        )
    }

    suspend fun initInterstitialAd(
        unitId: String
    ) {
        adMobRepository.initInterstitialAd(unitId = unitId)
    }

    suspend fun showInterstitialAds() {
        adMobRepository.showInterstitialAds()
    }

    fun appOpenLoad() {
        adMobRepository.appOpenLoad()
    }

    fun showAppOpenAd(activity: Activity, initAdMob: Boolean, goPage: () -> Unit) {

        adMobRepository.setActivityContext(activity)
        adMobRepository.showAppOpenAd(initAdMob, goPage)
    }

    fun showPrivacyOptionMenu() {
        adMobRepository.showPrivacyOptionMenu()
    }

}