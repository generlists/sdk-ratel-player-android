package so.smartlab.video.player.ad.admob.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import so.smartlab.video.player.ad.BuildConfig
import so.smartlab.video.player.ad.admob.data.AppOpenAdManager
import so.smartlab.video.player.ad.admob.data.AppOpenAdManager.Companion.AD_LOG_TAG
import so.smartlab.video.player.ad.admob.data.GoogleMobileAdsConsentManager
import so.smartlab.video.player.ad.admob.data.OnShowAdCompleteListener
import so.smartlab.video.player.ad.admob.data.model.AdMobAppOpenAdState
import so.smartlab.video.player.ad.admob.data.model.AdMobBannerState
import so.smartlab.video.player.ad.admob.data.model.AdMobInitState
import so.smartlab.video.player.ad.admob.data.model.AdMobInterstitialAdState
import so.smartlab.video.player.ad.admob.data.model.AdMobNativeAdState
import so.smartlab.video.player.ad.admob.ui.utils.UiUtils
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AdMobRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val adsConfigProvider: AdsConfigProvider,
    private val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager,
    private val appOpenAdManager: AppOpenAdManager,
) {

    //내부 변수
    private val _gatherConsentFinished = AtomicBoolean(false)
    private var _isPrivacyOptionsRequired = false

    private val _isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var _adMobInitialComplete = false

    private var interstitialAd: InterstitialAd? = null
    private var adIsLoading = false
    private var viewAd: Boolean = false
    private lateinit var currentActivity: Activity

    //상태 전달
    private val _adInitState = MutableStateFlow<AdMobInitState>(AdMobInitState.Idle)
    val adInitState: StateFlow<AdMobInitState> = _adInitState.asStateFlow()


    private val _adInterstitialAdEvent =
        MutableSharedFlow<AdMobInterstitialAdState>(
            replay = 0,
            extraBufferCapacity = 1
        )

    val adInterstitialAdEvent =
        _adInterstitialAdEvent.asSharedFlow()


    private val _adAppOpenAdState = MutableStateFlow<AdMobAppOpenAdState>(AdMobAppOpenAdState.Idle)
    val adApOpenAdState: StateFlow<AdMobAppOpenAdState> = _adAppOpenAdState.asStateFlow()

    private val _adFixedBannerState = MutableStateFlow<AdMobBannerState>(AdMobBannerState.Idle)
    val adFixedBannerState: StateFlow<AdMobBannerState> = _adFixedBannerState.asStateFlow()

    private val _adAdaptiveBannerState = MutableStateFlow<AdMobBannerState>(AdMobBannerState.Idle)
    val adAdaptiveBannerState: StateFlow<AdMobBannerState> = _adAdaptiveBannerState.asStateFlow()

    private val _adNativeState = MutableStateFlow<AdMobNativeAdState>(AdMobNativeAdState.Idle)
    val adNativeState: StateFlow<AdMobNativeAdState> = _adNativeState.asStateFlow()


    internal fun initAdMob() {

        googleMobileAdsConsentManager.gatherConsent(currentActivity) { consentError ->
            if (consentError != null) {
                // Consent not obtained in current session.
                Log.w(
                    "SSSSSS",
                    String.format("%s: %s", consentError.errorCode, consentError.message)
                )
            }
            _gatherConsentFinished.set(true)

            if (googleMobileAdsConsentManager.canRequestAds) {
                RLog.d("AdView","InitStart")
                _adInitState.value = AdMobInitState.InitStart

                initializeMobileAdsSdk()

            }

            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                _isPrivacyOptionsRequired = true
                _adInitState.value = AdMobInitState.PrivacyOptionRequired
            }
        }

    }

    internal fun setActivityContext(activity: Activity) {
        this.currentActivity = activity
    }

    private fun initializeMobileAdsSdk() {
        //Log.d("SSSSSS","getAndSet : ${_isMobileAdsInitializeCalled.getAndSet(true)}")
        if (_isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Set your test devices.
        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration
                    .Builder()
                    .setTestDeviceIds(listOf(adsConfigProvider.testDeviceHashedId))
                    .build(),
            )
        }

        // Initialize the Google Mobile Ads SDK on a background thread.
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(
                currentActivity,
            ) { p0 ->
                for ((adapterClassName, adapterStatus) in p0.adapterStatusMap) {
                    when (adapterStatus.initializationState) {
                        AdapterStatus.State.READY -> {
                            // 초기화 성공

                            currentActivity.runOnUiThread {
                                if (adapterClassName == "com.google.android.gms.ads.MobileAds") {
                                    _adMobInitialComplete = true
                                    _adInitState.value = AdMobInitState.InitComplete
                                    clearInitAdState()
                                }
                                RLog.d(
                                    "AdView",
                                    "initAdMob init complete ${_adMobInitialComplete}",
                                )
                            }
                        }

                        AdapterStatus.State.NOT_READY -> {
                            // 초기화 실패 또는 준비되지 않음
                            currentActivity.runOnUiThread {
                                if (adapterClassName == "com.google.android.gms.ads.MobileAds") {
                                    _adMobInitialComplete = false
                                    _adInitState.value = AdMobInitState.InitError
                                    clearInitAdState()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    internal suspend fun requestAdBanner(
        unitId: String,
        adSize: AdSize
    ): AdView =
        suspendCancellableCoroutine { cont ->

            _adFixedBannerState.value = AdMobBannerState.AdStart(progress = true)

            val adView =
                AdView(currentActivity).apply {

                    adUnitId = unitId
                    setAdSize(adSize)

                    adListener =
                        object : AdListener() {
                            override fun onAdLoaded() {
                                // 광고 로드 완료 시, 코루틴을 resume
                                RLog.d(AD_LOG_TAG, "onAdLoaded isActive : ${cont.isActive}")

                                if (cont.isActive) {
                                    cont.resume(this@apply)

                                    _adFixedBannerState.value = AdMobBannerState.AdLoad(
                                        progress = false,
                                        adSize,
                                        this@apply
                                    )

                                }
                            }

                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                // 광고 로드 실패 시, 코루틴을 예외와 함께 resume
                                if (cont.isActive) {
                                    RLog.e(AD_LOG_TAG, adError.message)
                                }

                                _adFixedBannerState.value =
                                    AdMobBannerState.AdError(progress = false, error = adError)

                                when (adError.code) {
                                    AdRequest.ERROR_CODE_INTERNAL_ERROR -> RLog.e(
                                        AD_LOG_TAG,
                                        "Internal error"
                                    )

                                    AdRequest.ERROR_CODE_INVALID_REQUEST -> RLog.e(
                                        AD_LOG_TAG,
                                        "Invalid request"
                                    )

                                    AdRequest.ERROR_CODE_NETWORK_ERROR -> RLog.e(
                                        AD_LOG_TAG,
                                        "Network error"
                                    )

                                    AdRequest.ERROR_CODE_NO_FILL -> RLog.e(
                                        AD_LOG_TAG,
                                        "No fill (No ads available)"
                                    )
                                }

                            }

                            override fun onAdClicked() {
                                // Code to be executed when the user clicks on an ad.
                                RLog.d(AD_LOG_TAG, "onAdClicked")
                            }

                            override fun onAdClosed() {
                                // Code to be executed when the user is about to return
                                // to the app after tapping on an ad.
                                RLog.d(AD_LOG_TAG, "onAdClosed")
                            }

                            override fun onAdImpression() {
                                // Code to be executed when an impression is recorded
                                // for an ad.
                                RLog.d(AD_LOG_TAG, "onAdImpression")
                                _adFixedBannerState.value = AdMobBannerState.AdLoadComplete(
                                    progress = false,
                                    UiUtils.adSize(currentActivity),
                                    this@apply
                                )

                            }

                            override fun onAdOpened() {
                                // Code to be executed when an ad opens an overlay that
                                // covers the screen.
                                RLog.d(AD_LOG_TAG, "onAdOpened")
                            }
                        }

                    loadAd(AdRequest.Builder().build())
                }

            // 코루틴이 취소될 경우 AdView 리소스 정리
            cont.invokeOnCancellation {
                adView.destroy()
                _adFixedBannerState.value = AdMobBannerState.AdDestroy
            }
        }

    internal suspend fun requestAdaptiveInlineBanner(
        unitId: String,
        adSize: AdSize
    ): AdView =
        suspendCancellableCoroutine { cont ->

            _adAdaptiveBannerState.value = AdMobBannerState.AdStart(progress = true)

            val adView =
                AdView(currentActivity).apply {

                    adUnitId = unitId
                    setAdSize(adSize)

                    adListener =
                        object : AdListener() {
                            override fun onAdLoaded() {
                                // 광고 로드 완료 시, 코루틴을 resume
                                RLog.d(AD_LOG_TAG, "onAdLoaded isActive : ${adSize}")

                                if (cont.isActive) {
                                    cont.resume(this@apply)
                                    _adAdaptiveBannerState.value = AdMobBannerState.AdLoad(
                                        progress = false,
                                        adSize,
                                        this@apply
                                    )
                                }
                            }

                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                // 광고 로드 실패 시, 코루틴을 예외와 함께 resume
                                if (cont.isActive) {
                                    RLog.e(AD_LOG_TAG, adError.message)
                                }
                                _adAdaptiveBannerState.value =
                                    AdMobBannerState.AdError(progress = false, error = adError)


                                when (adError.code) {
                                    AdRequest.ERROR_CODE_INTERNAL_ERROR -> RLog.e(
                                        AD_LOG_TAG,
                                        "Internal error"
                                    )

                                    AdRequest.ERROR_CODE_INVALID_REQUEST -> RLog.e(
                                        AD_LOG_TAG,
                                        "Invalid request"
                                    )

                                    AdRequest.ERROR_CODE_NETWORK_ERROR -> RLog.e(
                                        AD_LOG_TAG,
                                        "Network error"
                                    )

                                    AdRequest.ERROR_CODE_NO_FILL -> RLog.e(
                                        AD_LOG_TAG,
                                        "No fill (No ads available)"
                                    )
                                }

                            }

                            override fun onAdClicked() {
                                // Code to be executed when the user clicks on an ad.
                                RLog.d(AD_LOG_TAG, "onAdClicked")
                            }

                            override fun onAdClosed() {
                                // Code to be executed when the user is about to return
                                // to the app after tapping on an ad.
                                RLog.d(AD_LOG_TAG, "onAdClosed")
                            }

                            override fun onAdImpression() {
                                // Code to be executed when an impression is recorded
                                // for an ad.
                                RLog.d(AD_LOG_TAG, "onAdImpression isActive : ${adSize}")
                                RLog.d(AD_LOG_TAG, "onAdImpression")
                                _adAdaptiveBannerState.value = AdMobBannerState.AdLoadComplete(
                                    progress = false,
                                    UiUtils.adInLineAdaptiveBannerSize(currentActivity),
                                    this@apply
                                )
                            }

                            override fun onAdOpened() {
                                // Code to be executed when an ad opens an overlay that
                                // covers the screen.
                                RLog.d(AD_LOG_TAG, "onAdOpened")
                            }
                        }

                    loadAd(AdRequest.Builder().build())
                }

            // 코루틴이 취소될 경우 AdView 리소스 정리
            cont.invokeOnCancellation {
                adView.destroy()
                _adAdaptiveBannerState.value = AdMobBannerState.AdDestroy
            }
        }


    internal suspend fun requestNativeAd(unitId: String) {

        withContext(Dispatchers.IO) {
            _adNativeState.value = AdMobNativeAdState.AdInit(progress = true)
            val builder = AdLoader.Builder(currentActivity, unitId)
            builder.forNativeAd { nativeAd ->

                RLog.d(AD_LOG_TAG, "headline : ${nativeAd.headline}")
                // nativeAd.mediaContent?.let { unifiedAdBinding.adMedia.setMediaContent(it) }
                RLog.d(AD_LOG_TAG, "body : ${nativeAd.body}")
                RLog.d(AD_LOG_TAG, "callToAction : ${nativeAd.callToAction}")
                RLog.d(AD_LOG_TAG, "icon : ${nativeAd.icon}")
                RLog.d(AD_LOG_TAG, "price : ${nativeAd.price}")
                RLog.d(AD_LOG_TAG, "price : ${nativeAd.store}")
                RLog.d(AD_LOG_TAG, "price : ${nativeAd.starRating}")
                RLog.d(AD_LOG_TAG, "advertiser : ${nativeAd.advertiser}")

                _adNativeState.value =
                    AdMobNativeAdState.AdStart(progress = false, nativeAd = nativeAd)
            }

            val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
            val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
            builder.withNativeAdOptions(adOptions)

            val adLoader =
                builder
                    .withAdListener(
                        object : AdListener() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                RLog.d(AD_LOG_TAG, "Ad failed to load: ${adError.message}")
                                _adNativeState.value =
                                    AdMobNativeAdState.AdError(progress = false, error = adError)
                            }

                            override fun onAdClicked() {
                                RLog.d(AD_LOG_TAG, "onAdClicked")
                            }

                            override fun onAdClosed() {
                                RLog.d(AD_LOG_TAG, "onAdClosed")
                            }

                            override fun onAdImpression() {
                                RLog.d(AD_LOG_TAG, "onAdImpression")
                            }

                            override fun onAdLoaded() {
                                RLog.d(AD_LOG_TAG, "onAdLoaded")
                                //nativeAdState(AdMobNativeAdState.AdLoad(progress = false))
                            }

                            override fun onAdOpened() {
                                RLog.d(AD_LOG_TAG, "onAdOpened")
                            }

                            override fun onAdSwipeGestureClicked() {
                                RLog.d(AD_LOG_TAG, "onAdSwipeGestureClicked")
                            }
                        },
                    ).build()

            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    //1
    internal fun initInterstitialAd(unitId: String) {

        _adInterstitialAdEvent.tryEmit(AdMobInterstitialAdState.AdInit(progress = true))
        // 광고가 로드 중이거나 이미 로드된 경우 광고 요청을 하지 않음
        if (adIsLoading || interstitialAd != null || viewAd) {
            RLog.d(AD_LOG_TAG, "Ad is loading or already loaded, skipping request.")
          //  _adInterstitialAdState.value = AdMobInterstitialAdState.AdLoadComplete(complete = true)

            _adInterstitialAdEvent.tryEmit(AdMobInterstitialAdState.AdLoadComplete(complete = true))
            return
        }

        adIsLoading = true
        val adRequest = AdRequest.Builder().build()

        // 광고 로드 시작
        InterstitialAd.load(
            currentActivity,
            // 테스트 광고 ID: "ca-app-pub-3940256099942544/1033173712"
            unitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    RLog.d(AD_LOG_TAG, "Failed to load ad: ${adError.message}")
                    interstitialAd = null
                    //_adInterstitialAdState.value = AdMobInterstitialAdState.AdError(true, adError)
                    _adInterstitialAdEvent.tryEmit(AdMobInterstitialAdState.AdError(true, adError))
                    adIsLoading = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    RLog.d(AD_LOG_TAG, "Ad was loaded successfully. $ad")
                    interstitialAd = ad
//                    _adInterstitialAdState.value =
//                        AdMobInterstitialAdState.AdLoadComplete(complete = true)
                    _adInterstitialAdEvent.tryEmit(AdMobInterstitialAdState.AdLoadComplete(complete = true))
                    adIsLoading = false
                }
            },
        )
    }

    //2
    internal fun showInterstitialAds() {
        interstitialAd?.let { ad ->
            showInterstitialAd(context = currentActivity, ad)
        }
    }

    private fun showInterstitialAd(
        context: Activity?,
        ad: InterstitialAd?
    ) {

        if (context != null) {
            interstitialAd = null
            ad?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        RLog.d(AD_LOG_TAG, "Ad was dismissed.")
                        interstitialAd = null
                        //_adInterstitialAdState.value =
                            AdMobInterstitialAdState.FullScreenContentDismiss
                        _adInterstitialAdEvent.tryEmit( AdMobInterstitialAdState.FullScreenContentDismiss)
                        viewAd = true
                        clearInterstitialAdState()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        RLog.d(AD_LOG_TAG, "Ad failed to show: ${adError.message}")
                        interstitialAd = null
//                        _adInterstitialAdState.value =
//                            AdMobInterstitialAdState.FailFullScreenContent(adError)
                        _adInterstitialAdEvent.tryEmit(  AdMobInterstitialAdState.FailFullScreenContent(adError))
                        viewAd = true
                        clearInterstitialAdState()
                    }

                    override fun onAdShowedFullScreenContent() {
                        RLog.d(AD_LOG_TAG, "Ad showed fullscreen content.")
                        //_adInterstitialAdState.value = AdMobInterstitialAdState.FullScreenContent
                        _adInterstitialAdEvent.tryEmit(AdMobInterstitialAdState.FullScreenContent)
                    }
                }
            ad?.show(context)
        } else {
            RLog.d(AD_LOG_TAG, "Context is not an Activity, cannot show ad.")
        }
    }

    internal fun clearInterstitialAdState() {
        //_adInterstitialAdState.value = AdMobInterstitialAdState.Idle
        _adInterstitialAdEvent.tryEmit( AdMobInterstitialAdState.Idle)
        interstitialAd = null
        adIsLoading = false
        viewAd = false
    }

    internal  fun clearInitAdState(){
        _isMobileAdsInitializeCalled.set(false)
        _gatherConsentFinished.set(false)
    }

    internal fun clearAppOpenAdState() {
        _adAppOpenAdState.value = AdMobAppOpenAdState.Idle
        appOpenAdManager.clearAppOpen()

    }

    internal fun appOpenLoad() {
        appOpenAdManager.loadAd(context, _adAppOpenAdState)
    }

    internal fun showAppOpenAd(initAdmob: Boolean, goPage: () -> Unit) {

        appOpenAdManager.showAdIfAvailable(
            currentActivity,
            _adAppOpenAdState,
            object : OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    RLog.d(
                        AD_LOG_TAG,
                        "onShowAdComplete gatherConsentFinished",
                    )
                    // Check if the consent form is currently on screen before moving to the main
                    // activity.
                    _adAppOpenAdState.value = AdMobAppOpenAdState.AdShowComplete(complete = true)

                    if (initAdmob || !appOpenAdManager.isAdAvailable()) {
                        goPage()
                    }
                    clearAppOpenAdState()
                }
            },
        )
    }

    internal fun showPrivacyOptionMenu() {

        googleMobileAdsConsentManager.showPrivacyOptionsForm(currentActivity) { formError ->
            if (formError != null) {
                Toast.makeText(currentActivity, formError.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

}