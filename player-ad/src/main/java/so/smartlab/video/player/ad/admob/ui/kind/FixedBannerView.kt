package so.smartlab.video.player.ad.admob.ui.kind


import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import so.smartlab.video.player.ad.C.PROGRESS_LOADING_HEIGHT
import so.smartlab.video.player.ad.admob.data.AppOpenAdManager.Companion.AD_LOG_TAG
import so.smartlab.video.player.ad.admob.data.model.AdMobBannerState


@Composable
fun FixedBannerView(
    bannerBackGroundColor: Color,
    loadingBackGroundColor: Color,
    loadingProgressColor: Color,
    adMobBannerState: AdMobBannerState
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    var progress by remember { mutableStateOf(true) }
    var adView by remember { mutableStateOf<AdView?>(null) }
    var adError by remember { mutableStateOf<LoadAdError?>(null) }
    var adSize by remember { mutableStateOf<AdSize>(AdSize(0, 0)) }


    when (adMobBannerState) {

        is AdMobBannerState.AdStart -> {
            progress = adMobBannerState.progress
        }

        is AdMobBannerState.AdLoad -> {
            adView = adMobBannerState.adView
            adSize = adMobBannerState.adSize
            progress = adMobBannerState.progress
        }

        is AdMobBannerState.AdLoadComplete -> {
            adView = adMobBannerState.adView
            adSize = adMobBannerState.adSize
            progress = adMobBannerState.progress

        }

        is AdMobBannerState.AdError -> {
            adError = adMobBannerState.error
            progress = adMobBannerState.progress
        }

        else -> Unit
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> adView?.resume()
                    Lifecycle.Event.ON_PAUSE -> adView?.pause()
                    Lifecycle.Event.ON_DESTROY -> adView?.destroy()
                    else -> {}
                }
            }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    if (progress) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxWidth()
                    .height(PROGRESS_LOADING_HEIGHT.dp)
                    .background(loadingBackGroundColor),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                Modifier
                    .size(18.dp)
                    .padding(1.dp),
                // 원의 두께 조정
                strokeWidth = 3.dp,
                color = loadingProgressColor,
            )
        }
    } else {
        FixedBannerViewUI(
            adView = adView,
            adSize = adSize,
            backGroundColor = bannerBackGroundColor,
            progress = progress,
            adError = adError
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun FixedBannerViewUI(
    adView: AdView?,
    adSize: AdSize,
    backGroundColor: Color,
    progress: Boolean,
    adError: LoadAdError?
) {

    RLog.d(AD_LOG_TAG, "adView : $adView")
    RLog.d(AD_LOG_TAG, "adSize : $adSize")
    RLog.d(AD_LOG_TAG, "progress : $progress")
    RLog.d(AD_LOG_TAG, "adError : $adError")

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(adSize.height.dp),

        ) {
        if (adError == null) {

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(adSize.height.dp)
                    .background(backGroundColor)
            )
            {

                adView?.let {
                    AndroidView(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(backGroundColor)
                                .height(adSize.height.dp),
                        // adView 생성
                        factory = {
                            adView.apply {
                                (parent as? ViewGroup)?.removeView(this)
                            }
                        },
                        update = { view ->
                            // 뷰 레이아웃을 강제로 다시 요청
                            view.requestLayout()
                        },
                    )
                }
            }
        }
    }
}