package so.smartlab.video.player.ad.admob.ui.utils

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.android.gms.ads.AdSize

object UiUtils {

    fun adSize(context: Context): AdSize {
        val displayMetrics = context.resources.displayMetrics
        val adWidthPixels =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics =
                    context.getSystemService(WindowManager::class.java)?.currentWindowMetrics
                windowMetrics?.bounds?.width() ?: 0
            } else {
                displayMetrics.widthPixels
            }
        val density = displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    private fun adWidth(context:Context): Int{

            val displayMetrics = context.resources.displayMetrics
            val adWidthPixels =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowMetrics =
                        context.getSystemService(WindowManager::class.java)?.currentWindowMetrics
                    windowMetrics?.bounds?.width() ?: 0
                } else {
                    displayMetrics.widthPixels
                }
            val density = displayMetrics.density
            return (adWidthPixels / density).toInt()
        }
    fun adInLineAdaptiveBannerSize(
        context: Context
    ): AdSize {
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth(context))
    }

    fun pixelToDp(
        context: Context,
        px: Float,
    ): Float =
        px / (
                context.resources.displayMetrics.densityDpi
                    .toFloat() / DisplayMetrics.DENSITY_DEFAULT
                )
}
