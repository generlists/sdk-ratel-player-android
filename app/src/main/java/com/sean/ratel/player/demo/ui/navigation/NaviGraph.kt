package com.sean.ratel.player.demo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sean.ratel.player.demo.MainActivity
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.ui.screen.AdvancePlayer
import com.sean.ratel.player.demo.ui.screen.BasicPlayer
import com.sean.ratel.player.demo.ui.screen.YouTubeScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = Destination.Home.route,
    navigator: Navigator,
    finish: () -> Unit = {},
) {

    val activity = LocalContext.current as MainActivity
    val mainViewModel: MainViewModel = ViewModelProvider(activity)[MainViewModel::class.java]

    NavHandler(
        navController = navController,
        navigator = navigator,
        finish = finish)
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Destination.Home.route) {
            YouTubeScreen(mainViewModel)
        }

        composable(
            route = Destination.BasicPlayer.route,
            arguments = Destination.BasicPlayer.navArguments,
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("contentId")
            BasicPlayer(videoId)
        }

        composable(
            route = Destination.AdvancePlayer.route,
            arguments = Destination.BasicPlayer.navArguments,
        ) { backStackEntry ->
            val videoIdList = backStackEntry.arguments?.getString("contentId")
            val videoList  = videoIdList?.split(",")
            AdvancePlayer(mainViewModel,videoList)
        }
    }
}
