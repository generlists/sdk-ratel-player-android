package com.sean.ratel.player.demo.ui.screen

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.view.BasicPlayerFragment

@Composable
@Suppress("ktlint:standard:function-naming")
fun BasicPlayer(
    videoId: String?,
    mainViewModel: MainViewModel,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Box(Modifier.fillMaxSize()) {
            FragmentHost(videoId)

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            }
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun FragmentHost(videoId: String?) {
    AndroidView(factory = { context ->
        FragmentContainerView(context).apply {
            id = R.id.fragment_basic_container // 또는 고정된 ID 사용 가능
            layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
        }
    }) { fragmentContainerView ->
        val activity = fragmentContainerView.context as FragmentActivity
        val fragment = BasicPlayerFragment.newInstance(videoId ?: "")
        activity.supportFragmentManager
            .beginTransaction()
            .replace(fragmentContainerView.id, fragment)
            .commitNow()
    }
}
