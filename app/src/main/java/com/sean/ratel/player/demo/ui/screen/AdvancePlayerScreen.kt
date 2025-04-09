package com.sean.ratel.player.demo.ui.screen

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.navigation.Destination
import com.sean.ratel.player.demo.ui.view.AdvancePlayerFragment


@Composable
fun AdvancePlayer(mainViewModel: MainViewModel, videoIdList: List<String>?) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
    {
        AdvancePlayerHost(mainViewModel, videoIdList)
    }
}

@Composable
fun AdvancePlayerHost(mainViewModel: MainViewModel, videoIdList: List<String>?) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val fragmentManager = activity.supportFragmentManager
    val tag = "advance_player_fragment"

    val containerView = remember {
        FragmentContainerView(context).apply {
            id = R.id.fragmen_advance_container
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    AndroidView(factory = { containerView })

    LaunchedEffect(Unit) {
        val existingFragment = fragmentManager.findFragmentByTag(tag)
        if (existingFragment == null) {
            val fragment = AdvancePlayerFragment.newInstance(videoIdList)
            fragmentManager.beginTransaction()
                .add(containerView.id, fragment, tag)
                .commitNowAllowingStateLoss()
        }
    }

    BackHandler(enabled = true) {
        val fm = activity.supportFragmentManager
        val fragment = fm.findFragmentByTag("advance_player_fragment")
        if (fragment != null) {
            fm.beginTransaction()
                .remove(fragment)
                .commitNowAllowingStateLoss()
        }

        mainViewModel.runNavigationBack(Destination.Home.route)
    }
}
