package com.sean.ratel.player.demo.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object Destination {

    data object Home : Screen("home")
    data object BasicPlayer : DynamicScreen("basic_player", "contentId")
    data object AdvancePlayer : DynamicScreen("advance_player", "contentId")

    abstract class Screen(
        baseRoute: String,
    ) {
        companion object {
            const val BASE_DEEPLINK_URL = "app://splay"
        }

        open val route = baseRoute
        open val deeplink = "${BASE_DEEPLINK_URL}/$baseRoute"
    }

    abstract class DynamicScreen(
        private val baseRoute: String,
        val routeArgName: String,
    ) : Screen(baseRoute) {
        val navArguments = listOf(navArgument(routeArgName) { type = NavType.StringType })

        override val route = "$baseRoute/{$routeArgName}"
        override val deeplink = "${BASE_DEEPLINK_URL}/$baseRoute/{$routeArgName}"

        fun dynamicRoute(param: String) = "$baseRoute/$param"

        fun dynamicDeeplink(param: String) = "$BASE_DEEPLINK_URL/$baseRoute/$param"
    }
}
