package com.sean.ratel.player.demo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.sean.ratel.player.demo.ui.theme.DemoplayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    val mainViewModel by viewModels<MainViewModel>()
    val openAd = false //앱시작 광고할때 true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            mainViewModel.initAdMobSDK(this)

            DemoPlayApp(

                mainViewModel = mainViewModel,

                requestBannerAdView = {
                    mainViewModel.requestBannerAdView(this@MainActivity)
                },
                requestInLineBannerView = {
                    mainViewModel.requestInLineBannerAdView(this@MainActivity)
                },
                requestNativeAd = {
                    mainViewModel.requestNativeAd(this@MainActivity)
                },
                showAppOpenAd = {
                    if(openAd)
                        mainViewModel.showAppOpenAd(this@MainActivity)
                },

                finish = { finish() }
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
