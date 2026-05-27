package com.sean.ratel.player.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import java.util.Locale
import kotlin.math.ceil

object PlayerUtils {

    fun extractTtChainToken(cookie: String): String? {
        val regex = Regex("""tt_chain_token="?([^";]+)"?""")
        val match = regex.find(cookie)
        return match?.groups?.get(1)?.value
    }


}

fun formatTimeFromFloat(floatTime: Float): String {
    // 소숫점을 올림 처리
    val totalSeconds = ceil(floatTime).toInt()/1000
    // 분과 초로 변환
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}