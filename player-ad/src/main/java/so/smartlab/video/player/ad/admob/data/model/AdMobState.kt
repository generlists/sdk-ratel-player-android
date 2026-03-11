package so.smartlab.video.player.ad.admob.data.model

import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.nativead.NativeAd

/**
 * 광고 상태 관린
 */
sealed interface AdMobInitState {
    object Idle : AdMobInitState
    object PrivacyOptionRequired : AdMobInitState
    object InitStart : AdMobInitState
    object InitComplete : AdMobInitState
    object InitError : AdMobInitState
}

sealed interface AdMobBannerState {
    object Idle : AdMobBannerState
    data class AdStart(val progress: Boolean) : AdMobBannerState
    data class AdLoad(val progress: Boolean, val adSize: AdSize, val adView: AdView?) :
        AdMobBannerState

    data class AdError(val progress: Boolean, val error: LoadAdError?) : AdMobBannerState
    data class AdLoadComplete(val progress: Boolean, val adSize: AdSize, val adView: AdView?) :
        AdMobBannerState

    object AdDestroy : AdMobBannerState
}

sealed interface AdMobNativeAdState {

    object Idle : AdMobNativeAdState
    data class AdInit(val progress: Boolean) : AdMobNativeAdState
    data class AdStart(val progress: Boolean, val nativeAd: NativeAd?) : AdMobNativeAdState
    data class AdError(val progress: Boolean, val error: LoadAdError?) : AdMobNativeAdState

}

sealed interface AdMobInterstitialAdState {

    object Idle : AdMobInterstitialAdState
    data class AdInit(val progress: Boolean) : AdMobInterstitialAdState
    data class AdError(val progress: Boolean, val error: LoadAdError?) : AdMobInterstitialAdState
    data class FailFullScreenContent(val error: com.google.android.gms.ads.AdError?) :
        AdMobInterstitialAdState

    object FullScreenContent : AdMobInterstitialAdState
    object FullScreenContentDismiss : AdMobInterstitialAdState
    data class AdLoadComplete(val complete: Boolean) :
        AdMobInterstitialAdState
}

//app open ad
sealed interface AdMobAppOpenAdState {

    object Idle : AdMobAppOpenAdState
    data class AdInit(val progress: Boolean) : AdMobAppOpenAdState
    data class AdLoad(val progress: Boolean, val openAd: AppOpenAd?) : AdMobAppOpenAdState
    data class AdLoadFail(val progress: Boolean, val loadError: LoadAdError) : AdMobAppOpenAdState
    data class FailFullScreenContent(val error: com.google.android.gms.ads.AdError?) :
        AdMobAppOpenAdState

    object ShowFullScreenContent : AdMobAppOpenAdState
    object DisMissFullScreenContent : AdMobAppOpenAdState
    data class AdShowComplete(val complete: Boolean) :
        AdMobAppOpenAdState
}


