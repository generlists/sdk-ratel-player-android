package com.sean.ratel.player.demo.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object Destination {
    data object Splash : Screen("splash")

    data object Home : Screen("home")

    data object Download : Screen("download")

    data object Browser : Screen("browser")

    data object FaceBook : Screen("facebook")

    data object TikTok : Screen("tiktok")

    data object EndPlayer : DynamicScreen("endPlayer", "contentId", "startIndex")

    data object BasicPlayer : DynamicScreen("basic_player", "contentId", "startIndex")

    data object AdvancePlayer : DynamicScreen("advance_player", "contentId", "startIndex")

    abstract class Screen(
        baseRoute: String,
    ) {
        open val route = baseRoute
    }

    abstract class DynamicScreen(
        private val baseRoute: String,
        val routeArgName: String,
        val startIndexArgName: String,
    ) : Screen(baseRoute) {
        val navArguments =
            listOf(
                navArgument(routeArgName) { type = NavType.StringType },
                navArgument(startIndexArgName) {
                    type = NavType.IntType
                    defaultValue = 0
                },
            )

        override val route = "$baseRoute/{$routeArgName}/{$startIndexArgName}"

        fun dynamicRoute(
            contentId: String,
            startIndex: Int = 0,
        ): String = "$baseRoute/$contentId/$startIndex"
    }
}
