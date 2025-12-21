package com.sean.ratel.player.core.data.domain.model

import android.app.PendingIntent

data class DownloadNotificationData(
    val title: String,
    val message: String,
    val progress: Int? = null,   // null → 완료/실패
    val smallIcon: Int,
    val contentIntent: PendingIntent? = null
)