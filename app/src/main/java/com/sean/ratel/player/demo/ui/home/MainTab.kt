package com.sean.ratel.player.demo.ui.home

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.navigation.Destination

@Keep
enum class MainTab(
    @StringRes val title: Int,
    val route: String,
    val destRoute: String,
) {
    YOUTUBE(
        R.string.youtube_tab,
        Destination.Home.route,
        Destination.Home.route,
    ),
}
