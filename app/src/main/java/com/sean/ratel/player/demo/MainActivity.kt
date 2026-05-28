package com.sean.ratel.player.demo

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.sean.ratel.player.core.data.player.pip.PIPViewModel
import com.sean.ratel.player.core.data.player.pip.PipAction
import com.sean.ratel.player.core.util.launch
import com.sean.ratel.player.demo.ui.theme.DemoplayerTheme
import com.sean.ratel.player.demo.ui.view.BasicPlayerFragment
import com.sean.ratel.player.utils.PlayerUtils.hasPipPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    val mainViewModel by viewModels<MainViewModel>()
    val pipViewModel: PIPViewModel by viewModels()
    private val pipButtonState = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    private var pipBroadcastReceiver: BroadcastReceiver? = null
    private var currentFragment: androidx.fragment.app.Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoPlayApp(
                mainViewModel = mainViewModel,
                finish = { finish() },
            )
        }

        launch {
            pipButtonState.collect {
                val fragment = currentFragment as BasicPlayerFragment
                when (it) {
                    PipAction.PAUSE.intentExtraValue -> {
                        fragment.pause()
                        fragment.pipButtonState()
                        pipButtonState.tryEmit(0)
                    }

                    PipAction.PLAY.intentExtraValue -> {
                        fragment.play()
                        fragment.pipButtonState()
                        pipButtonState.tryEmit(0)
                    }

                    PipAction.SKIP_PREVIOUS.intentExtraValue -> {
                        Toast
                            .makeText(
                                baseContext,
                                "첫번째 비디오입니다.",
                                Toast.LENGTH_LONG,
                            ).show()
                    }

                    PipAction.SKIP_NEXT.intentExtraValue -> {
                        Toast
                            .makeText(
                                baseContext,
                                "마지막 비디오입니다.",
                                Toast.LENGTH_LONG,
                            ).show()
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        Log.d("MainActivity", "isInPictureInPictureMode : $isInPictureInPictureMode")
        if (isInPictureInPictureMode) {
            val pipBroadcastReceiver = pipBroadcastReceiver ?: PipBroadcastReceiver()
            registerReceiver(
                pipBroadcastReceiver,
                PipAction.getIntentFilter(),
                RECEIVER_NOT_EXPORTED,
            )
            this@MainActivity.pipBroadcastReceiver = pipBroadcastReceiver
        } else {
            pipBroadcastReceiver?.let {
                unregisterReceiver(it)
            }
        }
        launch {
            val pageId = R.id.fragment_basic_container
            pipViewModel.setPIPClick(pageId.toString(), isInPictureInPictureMode)
        }
    }

    @Override
    override fun onResume() {
        super.onResume()
        pipClickProcess(this)
    }

    override fun onStop() {
        super.onStop()

        if (isInPictureInPictureMode) {
            Log.d("TAG", "PIP mode stopped, possibly due to Close button")
            finish()
        }
    }

    private fun pipClickProcess(context: Context) {
        launch {
            pipViewModel.pipClick.collect { target ->
                Log.d("hbungshin", "target : $target")
                val currentIndex = target.pageId
                val isPipClick = target.isPipClick
                val fragmentManager = (this@MainActivity as FragmentActivity).supportFragmentManager
                currentFragment = fragmentManager.findFragmentById(R.id.fragment_basic_container)
                if (isPipClick &&
                    currentFragment != null && currentFragment is BasicPlayerFragment &&
                    hasPipPermission()
                ) {
                    (currentFragment as BasicPlayerFragment).onClickPipAction(context)
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (isInPictureInPictureMode) {
            Log.d("TAG", "Close button clicked in PIP mode")
            finish()
        }
    }

    inner class PipBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?,
        ) {
            intent ?: return
            Log.d(
                "MainActivity",
                "onReceive() , intent=$intent, action=${intent.action} extra : ${
                    intent.getIntExtra(
                        "splay.intent.extra.pip",
                        0,
                    )
                }",
            )

            when {
                PipAction.isPauseAction(intent) -> pipButtonState.tryEmit(PipAction.PAUSE.intentExtraValue)
                PipAction.isPlayAction(intent) -> pipButtonState.tryEmit(PipAction.PLAY.intentExtraValue)
                PipAction.isPreviousAction(intent) -> pipButtonState.tryEmit(PipAction.SKIP_PREVIOUS.intentExtraValue)
                PipAction.isNextAction(intent) -> pipButtonState.tryEmit(PipAction.SKIP_NEXT.intentExtraValue)
            }
        }
    }
}

@Preview(showBackground = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun GreetingPreview() {
    DemoplayerTheme {
    }
}
