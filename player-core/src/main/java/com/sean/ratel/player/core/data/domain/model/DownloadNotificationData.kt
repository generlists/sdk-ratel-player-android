package com.sean.ratel.player.core.data.domain.model

import android.app.PendingIntent

data class DownloadNotificationData(
    val title: String,
    val message: String,
    val progress: Int? = null,
    val smallIcon: Int,
    val contentIntent: PendingIntent? = null
)