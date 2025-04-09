package com.sean.ratel.player.demo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.sean.ratel.player.demo.ui.theme.DemoplayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemoPlayApp(
                mainViewModel = mainViewModel,
                finish = { finish() },
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DemoplayerTheme {
    }
}

