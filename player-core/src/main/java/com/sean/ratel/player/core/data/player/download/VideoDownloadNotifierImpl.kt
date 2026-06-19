package com.sean.ratel.player.core.data.player.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import com.sean.ratel.player.core.data.domain.model.DownloadNotificationData
import com.sean.ratel.player.core.data.domain.model.VideoDownloadNotifier
import com.sean.ratel.player.core.data.player.download.VideoDownloadService.Companion.DOWNLOAD_CHANNEL_ID

@OptIn(UnstableApi::class)
class VideoDownloadNotifierImpl(
    private val context: Context,
    private val notificationHelper: DownloadNotificationHelper,
) : VideoDownloadNotifier {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun showProgress(data: DownloadNotificationData) {
    }

    override fun showCompleted(data: DownloadNotificationData) {
        val notification =
            notificationHelper.buildDownloadCompletedNotification(
                context,
                data.smallIcon,
                data.contentIntent,
                data.message,
            )
        val channel =
            NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_HIGH,
            )
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(NOTIFY_ID, notification)
    }

    override fun showFailed(data: DownloadNotificationData) {
        val notification =
            notificationHelper.buildDownloadFailedNotification(
                context,
                data.smallIcon,
                data.contentIntent,
                data.message,
            )
        val channel =
            NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_HIGH,
            )
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(NOTIFY_ID, notification)
    }

    override fun cancel(id: Int) {
        notificationManager.cancel(id)
    }

    companion object {
        const val NOTIFY_ID = 9999
    }
}
