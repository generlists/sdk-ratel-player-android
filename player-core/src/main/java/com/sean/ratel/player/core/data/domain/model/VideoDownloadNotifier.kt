package com.sean.ratel.player.core.data.domain.model

interface VideoDownloadNotifier {

    fun showProgress(data: DownloadNotificationData)
    fun showCompleted(data: DownloadNotificationData)
    fun showFailed(data: DownloadNotificationData)
    fun cancel(id: Int)
}