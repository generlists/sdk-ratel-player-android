package so.smartlab.video.player.ad.admob.ui.kind

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.NativeAd
import so.smartlab.video.player.ad.R
import so.smartlab.video.player.ad.admob.data.AppOpenAdManager.Companion.AD_LOG_TAG
import so.smartlab.video.player.ad.admob.data.model.AdMobNativeAdState
import so.smartlab.video.player.ad.databinding.NativeAdBinding

@Suppress("ktlint:standard:function-naming")
@Composable
fun NativeAdView( backGroundColor: Color,
                  backGroundTextColor: Color,nativeAdState: AdMobNativeAdState) {

    val lifecycleOwner = LocalLifecycleOwner.current

    RLog.d(AD_LOG_TAG,"nativeAdState : $nativeAdState")
    var progress by remember { mutableStateOf(true) }
    var adNative by remember { mutableStateOf<NativeAd?>(null) }
    var adError by remember { mutableStateOf<LoadAdError?>(null) }

    val bannerState = nativeAdState

    when (bannerState) {

        is AdMobNativeAdState.AdInit ->{
            progress = bannerState.progress
        }

        is AdMobNativeAdState.AdStart ->{
            progress = bannerState.progress
            adNative = bannerState.nativeAd

        }

        is AdMobNativeAdState.AdError -> {
            adError = bannerState.error
            progress = bannerState.progress
        }

        else -> null
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_DESTROY -> {
                        adNative?.destroy()
                    }

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
                    .wrapContentHeight().background(backGroundColor)
                        ,
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                Modifier
                    .size(18.dp)
                    .padding(1.dp),
                // 원의 두께 조정
                strokeWidth = 3.dp,
                color = backGroundTextColor,
            )
        }
    }else{
        BindNativeView(
            ad = adNative,
           adError = adError
        )
    }

}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun BindNativeView(ad: NativeAd?,adError: AdError?) {
    val context = LocalContext.current
    if(ad == null || adError != null) return

    val adView =
        remember {
            val binding = NativeAdBinding.inflate(LayoutInflater.from(context), null, false)
            binding.adView.mediaView = binding.adMedia

            // 광고 구성 요소들을 바인딩

            binding.adView.headlineView = binding.adHeadline
            binding.adView.bodyView = binding.adBody
            binding.adView.callToActionView = binding.adCallToAction
            binding.adView.iconView = binding.adAppIcon
            binding.adView.priceView = binding.adPrice
            binding.adView.starRatingView = binding.adStars
            binding.adView.storeView = binding.adStore
            binding.adView.advertiserView = binding.adAdvertiser

            // 광고 데이터 연결
            (binding.adView.headlineView as TextView).text = ad?.headline

            ad.mediaContent?.let { binding.adMedia.mediaContent = it }

            ad.body?.let { binding.adBody.text = it }
                ?: run { (binding.adBody as? TextView)?.visibility = View.INVISIBLE }

            ad.callToAction?.let { binding.adCallToAction.text = it }
                ?: run { (binding.adCallToAction as? TextView)?.visibility = View.INVISIBLE }

            // 광고 아이콘 설정
            ad.icon?.let {
                (binding.adAppIcon).load(it.uri) {
                    transformations(CircleCropTransformation()) // 원형 이미지 변환 적용
                        .placeholder(R.drawable.ad_circle_shape)
                }
            } ?: run { (binding.adAppIcon as? ImageView)?.visibility = View.INVISIBLE }

            // 기타 선택적 요소 설정
            ad.price?.let { (binding.adPrice as? TextView)?.text = it }
                ?: run { (binding.adPrice as? TextView)?.visibility = View.INVISIBLE }
            ad.store?.let { (binding.adStore as? TextView)?.text = it }
                ?: run { (binding.adStore as? TextView)?.visibility = View.INVISIBLE }
            ad.starRating?.let { (binding.adStars as? RatingBar)?.rating = it.toFloat() }
                ?: run { (binding.adStars as? RatingBar)?.visibility = View.INVISIBLE }
            ad.advertiser?.let { (binding.adAdvertiser as? TextView)?.text = it }
                ?: run { (binding.adAdvertiser as? TextView)?.visibility = View.INVISIBLE }

            // NativeAd 객체 연결
            binding.adView.setNativeAd(ad)

            val mediaContent = ad.mediaContent
            val vc = mediaContent?.videoController

            if (vc != null && mediaContent.hasVideoContent()) {
                vc.videoLifecycleCallbacks =
                    object : VideoController.VideoLifecycleCallbacks() {
                        override fun onVideoEnd() {
                            super.onVideoEnd()
                        }
                    }
            }
            binding.adView
        }
    AndroidView(
        factory = { _ ->
            adView
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .wrapContentHeight(),
    )
}