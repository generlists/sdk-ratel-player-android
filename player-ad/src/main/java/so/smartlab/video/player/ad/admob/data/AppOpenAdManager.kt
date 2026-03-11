package so.smartlab.video.player.ad.admob.data

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.sean.ratel.player.utils.log.RLog
import kotlinx.coroutines.flow.MutableStateFlow
import so.smartlab.video.player.ad.BuildConfig
import so.smartlab.video.player.ad.admob.data.model.AdMobAppOpenAdState
import so.smartlab.video.player.ad.admob.data.repository.AdsConfigProvider
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenAdManager @Inject
constructor(
    val adConfig: AdsConfigProvider,
    val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager){

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
    private var loadTime: Long = 0

    // [END manager_class]

    /**
     * Load an ad.
     *
     * @param context the context of the activity that loads the ad
     */
    fun loadAd(context: Context,
               adApOpenAdState: MutableStateFlow<AdMobAppOpenAdState>) {
        // Do not load ad if there is an unused ad or one is already loading.
        if (isLoadingAd || isAdAvailable()) {
            return
        }
        isLoadingAd = true
        // [START load_ad]
        adApOpenAdState.value = AdMobAppOpenAdState.AdInit(progress = true)

        AppOpenAd.load(
            context,
            adConfig.openUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    // Called when an app open ad has loaded.
                    if(BuildConfig.DEBUG) RLog.d(AD_LOG_TAG, "App open ad loaded.")
                    if(BuildConfig.DEBUG) Toast.makeText(context, "Ad was loaded.", Toast.LENGTH_SHORT).show()

                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    adApOpenAdState.value = AdMobAppOpenAdState.AdLoad(progress = false,appOpenAd)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Called when an app open ad has failed to load.
                    if(BuildConfig.DEBUG) RLog.d(AD_LOG_TAG, "App open ad failed to load with error: " + loadAdError.message)
                    if(BuildConfig.DEBUG) Toast.makeText(context, "Ad failed to load.", Toast.LENGTH_SHORT).show()
                    adApOpenAdState.value = AdMobAppOpenAdState.AdLoadFail(progress = false,loadAdError)
                    isLoadingAd = false
                }
            },
        )
        // [END load_ad]
    }

    // [START ad_expiration]
    /** Check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown. */
    fun isAdAvailable(): Boolean {
        // For time interval details, see: https://support.google.com/admob/answer/9341964
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    // [END ad_expiration]

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     */
    fun showAdIfAvailable(
        activity: Activity,
        appOpenAdState: MutableStateFlow<AdMobAppOpenAdState>
    ) {
        showAdIfAvailable(
            activity,
            appOpenAdState,
            object : OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    // Empty because the user will go back to the activity that shows the ad.
                }
            },
        )
    }

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(
        activity: Activity,
        adApOpenAdState: MutableStateFlow<AdMobAppOpenAdState>,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            if(BuildConfig.DEBUG) RLog.d(AD_LOG_TAG, "The app open ad is already showing.")
            return
        }

        // If the app open ad is not available yet, invoke the callback.
        if (!isAdAvailable()) {
            if(BuildConfig.DEBUG) RLog.d(AD_LOG_TAG, "The app open ad is not ready yet.")
            onShowAdCompleteListener.onShowAdComplete()
            if (googleMobileAdsConsentManager.canRequestAds) {
                loadAd(activity,adApOpenAdState)
            }
            return
        }

        if(BuildConfig.DEBUG) RLog.d(AD_LOG_TAG, "Will show ad.")

        appOpenAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                /** Called when full screen content is dismissed. */
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                     RLog.d(AD_LOG_TAG, "onAdDismissedFullScreenContent.")
                    if(BuildConfig.DEBUG) Toast.makeText(activity, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT).show()

                    adApOpenAdState.value = AdMobAppOpenAdState.DisMissFullScreenContent
                    onShowAdCompleteListener.onShowAdComplete()

                    if (googleMobileAdsConsentManager.canRequestAds) {
                        loadAd(activity,adApOpenAdState)
                    }
                }

                /** Called when fullscreen content failed to show. */
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    if(BuildConfig.DEBUG) RLog.d(AD_LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.message)
                    Toast.makeText(activity, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT).show()
                    adApOpenAdState.value = AdMobAppOpenAdState.FailFullScreenContent(adError)
                    onShowAdCompleteListener.onShowAdComplete()
                    if (googleMobileAdsConsentManager.canRequestAds) {
                        loadAd(activity,adApOpenAdState)
                    }
                }

                /** Called when fullscreen content is shown. */
                override fun onAdShowedFullScreenContent() {
                    RLog.d(AD_LOG_TAG, "onAdShowedFullScreenContent.")
                    adApOpenAdState.value = AdMobAppOpenAdState.ShowFullScreenContent
                    if(BuildConfig.DEBUG)Toast.makeText(activity, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT).show()
                }
            }
        isShowingAd = true
        appOpenAd?.show(activity)
    }
    companion object{
       val AD_LOG_TAG ="LOG_TAG"
    }

    fun clearAppOpen() {
        appOpenAd = null
        isLoadingAd = false
        isShowingAd = false
    }
}
/**
 * Interface definition for a callback to be invoked when an app open ad is complete (i.e.
 * dismissed or fails to show).
 */
interface OnShowAdCompleteListener {
    fun onShowAdComplete()
}