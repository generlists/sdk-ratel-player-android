package so.smartlab.video.player.ad.admob.ui.kind

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.sean.ratel.player.utils.log.RLog
import so.smartlab.video.player.ad.admob.data.AppOpenAdManager.Companion.AD_LOG_TAG
import so.smartlab.video.player.ad.admob.data.model.AdMobBannerState

@Suppress("ktlint:standard:function-naming")
@Composable
fun AdaptiveInLineBannerView(bannerBackGroundColor: Color,
                             bannerTextColor:Color,
                             adMobBannerState: AdMobBannerState,) {

    val lifecycleOwner = LocalLifecycleOwner.current
    var adView by remember { mutableStateOf<AdView?>(null) }
    var progress by remember { mutableStateOf(true) }
    var adSize by remember { mutableStateOf<AdSize?>(AdSize(0,0)) }
    var adError by remember { mutableStateOf<LoadAdError?>(null) }

    val bannerState = adMobBannerState

    when (bannerState) {

        is AdMobBannerState.AdStart ->{
            progress = bannerState.progress
        }

        is AdMobBannerState.AdLoad -> {
            adView = bannerState.adView
            adSize =
                if (bannerState.adSize.height == 0) (bannerState.adView)?.adSize else bannerState.adSize
            progress = bannerState.progress
        }
        is AdMobBannerState.AdLoadComplete -> {
            adView = bannerState.adView
            adSize = if(bannerState.adSize .height == 0)  (bannerState.adView)?.adSize else bannerState.adSize
            progress = bannerState.progress

        }

        is AdMobBannerState.AdError -> {
            adError = bannerState.error
            progress = bannerState.progress
        }

        else -> null
    }

    RLog.d(AD_LOG_TAG,"adSize : ${adSize} , bannerState : $bannerState  ")



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
                    .wrapContentHeight()

                    .background(bannerBackGroundColor),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                Modifier
                    .size(18.dp)
                    .padding(1.dp),
                // 원의 두께 조정
                strokeWidth = 3.dp,
                color = bannerTextColor,
            )
        }
    }else{
        InLineAdaptiveBannerUI(
            adView = adView,
            adError = adError
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun InLineAdaptiveBannerUI(
    adView: AdView?,
    adError: LoadAdError?
) {

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()

        ) {
        if(adError  == null){

            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
            {

                adView?.let { safeAdView ->
                    AndroidView(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        factory = {
                            (safeAdView.parent as? ViewGroup)?.removeView(safeAdView)
                            safeAdView.apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            }
                        },
                        update = { view ->
                            // 광고 로드 완료 후 뷰가 멍청하게 가만히 있으면 강제로 깨우기
                            view.post {
                                view.requestLayout()
                                view.invalidate()
                            }
                        }
                    )
                }
            }
        }
    }
}