package com.sean.ratel.player.core.data.player.download
import android.app.Notification
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

//Foreground Service는 반드시 Notification을 표시해야 한다.
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class VideoDownloadService() : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_CHANNEL_ID,
    R.string.download_channel_name,
    R.string.download_channel_desc
) {
    @Inject
    lateinit var downloadTracker: DownloadTracker


    @Inject
    lateinit var videoDownloadManager: DownloadManager
    @Inject
    lateinit var notificationHelper: DownloadNotificationHelper

    override fun getDownloadManager(): DownloadManager {
        return videoDownloadManager
    }

    override fun getScheduler(): Scheduler? {
        return PlatformScheduler(this, JOB_ID)
    }

    override fun getForegroundNotification(
        downloads: List<Download>,
        notMetRequirements: Int
    ): Notification  {
    return notificationHelper.buildProgressNotification(
        applicationContext,
        R.drawable.ic_download,
        null,
        null,
        downloads,
        notMetRequirements
    )
    }


    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1001
        private const val JOB_ID = 123456
        const val DOWNLOAD_CHANNEL_ID ="download_channel"
    }
}