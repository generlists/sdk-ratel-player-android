package com.sean.ratel.player.core.data.player.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import com.sean.ratel.player.core.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Foreground Service는 반드시 Notification을 표시해야 한다.
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class VideoDownloadService :
    DownloadService(
        FOREGROUND_NOTIFICATION_ID,
        DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
        DOWNLOAD_CHANNEL_ID,
        R.string.download_channel_name,
        R.string.download_channel_desc,
    ) {
    @Inject
    lateinit var downloadTracker: DownloadTracker

    @Inject
    lateinit var videoDownloadManager: DownloadManager

    @Inject
    lateinit var notificationHelper: DownloadNotificationHelper

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun getDownloadManager(): DownloadManager = videoDownloadManager

    override fun getScheduler(): Scheduler = PlatformScheduler(this, JOB_ID)

    override fun getForegroundNotification(
        downloads: List<Download>,
        notMetRequirements: Int,
    ): Notification {
        val currentDownload =
            downloads.firstOrNull {
                it.state == Download.STATE_DOWNLOADING
            }

        val id =
            currentDownload
                ?.request
                ?.id

        val downloadOptions = downloadTracker.downloadOptions.value[id]

        return notificationHelper.buildProgressNotification(
            applicationContext,
            scrapBrandIcon(downloadOptions?.brandName ?: "FACEBOOK"),
            null,
            downloadOptions?.notificationMessage,
            downloads,
            notMetRequirements,
        )
    }

    private fun scrapBrandIcon(scrapBrand: String) =
        when (scrapBrand) {
            getString(R.string.face_book) -> R.drawable.ic_facebook_logo
            getString(R.string.insta_gram) -> R.drawable.ic_instagram_logo
            else -> R.drawable.ic_facebook_logo
        }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1001
        private const val JOB_ID = 123456
        const val DOWNLOAD_CHANNEL_ID = "download_channel"
    }

    private fun createNotificationChannels() {
        val channel =
            NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                getString(R.string.download_channel_desc),
                NotificationManager.IMPORTANCE_LOW,
            )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}
