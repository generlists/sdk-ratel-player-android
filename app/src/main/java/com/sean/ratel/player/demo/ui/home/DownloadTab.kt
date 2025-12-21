package com.sean.ratel.player.demo.ui.home

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.navigation.Destination

@Keep
enum class DownloadTab(
    @StringRes val title: Int,
    val route: String,
    val destRoute: String,
) {
    FACEBOOK(
        R.string.download_tab_facebook,
        Destination.Home.route,
        Destination.Home.route,
    ),
    TIKTOK(
        R.string.download_tab_tiktok,
        Destination.Download.route,
        Destination.Download.route,
    ),
}