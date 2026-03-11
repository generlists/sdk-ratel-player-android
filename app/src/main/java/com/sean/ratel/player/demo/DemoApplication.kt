package com.sean.ratel.player.demo

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.sean.ratel.player.utils.log.RLog
import dagger.hilt.android.HiltAndroidApp
import so.smartlab.video.player.ad.admob.data.AppOpenAdManager
import so.smartlab.video.player.ad.admob.ui.utils.ActivityEvent
import so.smartlab.video.player.ad.admob.ui.utils.AppLifecycleBus
import javax.inject.Inject

@HiltAndroidApp
class DemoApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {

    @Inject
    lateinit var appOpenAdManager: AppOpenAdManager
    @Inject
    lateinit var appLifecycleBus: AppLifecycleBus
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        registerActivityLifecycleCallbacks(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        RLog.init(
            this,
            enableAllLogger =if (BuildConfig.DEBUG) true else false,
            enableShowLogWithLinkToSource = false,
            enableUdpLogger = false,
        )

    }

    /**
     * DefaultLifecycleObserver method that shows the app open ad when the app moves to foreground.
     */
    // [START lifecycle_observer_events]
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let {
            appLifecycleBus.emit(ActivityEvent.Started(it))
        }
    }


    /** ActivityLifecycleCallback methods. */
    // [START activity_lifecycle_callbacks]
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    // [END activity_lifecycle_callbacks]


}