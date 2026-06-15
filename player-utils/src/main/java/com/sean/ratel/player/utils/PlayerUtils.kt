package com.sean.ratel.player.utils

import android.app.Activity
import android.app.AppOpsManager
import android.app.Instrumentation
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.ceil

object PlayerUtils {
    fun extractTtChainToken(cookie: String): String? {
        val regex = Regex("""tt_chain_token="?([^";]+)"?""")
        val match = regex.find(cookie)
        return match?.groups?.get(1)?.value
    }

    @JvmStatic
    fun Context.hasPipPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                android.os.Process.myUid(),
                packageName,
            ) == AppOpsManager.MODE_ALLOWED
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                android.os.Process.myUid(),
                packageName,
            ) == AppOpsManager.MODE_ALLOWED
        }
    }

    fun runPIPSetting(
        context: Context,
        pipSettingsLauncher: ActivityResultLauncher<Intent>,
    ) {
        val intent =
            Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS").apply {
                data = "package:${context.packageName}".toUri()
            }
        pipSettingsLauncher.launch(intent)
    }

    fun formatTimeFromFloat(floatTime: Float): String {
        // 소숫점을 올림 처리
        val totalSeconds = ceil(floatTime).toInt() / 1000
        // 분과 초로 변환
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
