package com.sean.ratel.player.demo.ui.navigation // package com.sean.ratel.player.demo.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sean.ratel.player.demo.MainActivity
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.ui.download.AccompanistBrowserScreen
import com.sean.ratel.player.demo.ui.download.VideoDownloadViewModel
import com.sean.ratel.player.demo.ui.screen.AdvancePlayer
import com.sean.ratel.player.demo.ui.screen.BasicPlayer
import com.sean.ratel.player.demo.ui.screen.DownLoadSample
import com.sean.ratel.player.demo.ui.screen.EndPlayerScreen
import com.sean.ratel.player.demo.ui.screen.SplashScreen
import com.sean.ratel.player.demo.ui.screen.YouTubeScreen
import com.sean.ratel.player.ui.ThemeMode

@Composable
@Suppress("ktlint:standard:function-naming")
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    themeMode: ThemeMode,
    startDestination: String = Destination.Home.route,
    navigator: Navigator,
    finish: () -> Unit = {},
) {
    val activity = LocalContext.current as MainActivity
    val mainViewModel: MainViewModel = ViewModelProvider(activity)[MainViewModel::class.java]
    val videoDownloadViewModel: VideoDownloadViewModel = ViewModelProvider(activity)[VideoDownloadViewModel::class.java]

    NavHandler(
        navController = navController,
        navigator = navigator,
        finish = finish,
    )
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Destination.Splash.route) {
            SplashScreen(activity, mainViewModel)
        }
        composable(Destination.Home.route) {
            YouTubeScreen(mainViewModel)
        }
        composable(Destination.Download.route) {
            DownLoadSample(mainViewModel, videoDownloadViewModel)
        }
        composable(Destination.Browser.route) {
            AccompanistBrowserScreen("https://m.facebook.com")
        }

        composable(
            route = Destination.BasicPlayer.route,
            arguments = Destination.BasicPlayer.navArguments,
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("contentId")
            BasicPlayer(videoId, mainViewModel)
        }

        composable(
            route = Destination.AdvancePlayer.route,
            arguments = Destination.BasicPlayer.navArguments,
        ) { backStackEntry ->
            val videoIdList = backStackEntry.arguments?.getString("contentId")
            val videoList = videoIdList?.split(",")
            AdvancePlayer(mainViewModel, videoList)
        }

        composable(
            route = Destination.EndPlayer.route,
            arguments = Destination.BasicPlayer.navArguments,
        ) { backStackEntry ->
            val content = backStackEntry.arguments?.getString("contentId")
            val startIndex = backStackEntry.arguments?.getInt("startIndex") ?: -1

            Log.d("hbungshin", "content : $content")
            Log.d("hbungshin", "startIndex : $startIndex")

            content?.let {
                EndPlayerScreen(
                    modifier = modifier,
                    urls = it,
                    startIndex = startIndex,
                    themeMode = themeMode,
                    downloadViewModel = videoDownloadViewModel,
                )
            }
        }
    }
}
