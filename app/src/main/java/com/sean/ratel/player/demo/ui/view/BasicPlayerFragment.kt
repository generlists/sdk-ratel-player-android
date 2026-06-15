package com.sean.ratel.player.demo.ui.view

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.sean.ratel.player.core.data.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.data.player.pip.PIPManager
import com.sean.ratel.player.core.data.player.pip.PIPTarget
import com.sean.ratel.player.core.data.player.pip.PipResult
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerAdapterImpl
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerImpl
import com.sean.ratel.player.core.data.player.youtube.adaptor.YouTubeStreamPlayerAdapter
import com.sean.ratel.player.core.util.launch
import com.sean.ratel.player.core.util.repeatOnStart
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.databinding.FragmentBasicPlayerBinding
import com.sean.ratel.player.demo.di.qualifier.NotControl
import com.sean.ratel.player.utils.PlayerUtils.hasPipPermission
import com.sean.ratel.player.utils.PlayerUtils.runPIPSetting
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BasicPlayerFragment : Fragment() {
    private val argsParm1 = "param1"
    private var param1: String? = null
    private var _binding: FragmentBasicPlayerBinding? = null
    private val binding get() = _binding!!

    lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var youTubeStreamPlayer: YouTubeStreamPlayer
    private lateinit var youtubeStreamPlayerAdapter: YouTubeStreamPlayerAdapter
    val pipSettingsLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { _ ->
        }

    @Inject
    lateinit var pipViewModel: PIPManager

    @NotControl
    @Inject
    lateinit var iFramePlayerOptions: IFramePlayerOptions

    @Inject
    lateinit var youtubeStreamPlayerTracker: YouTubePlayerTracker

    init {
        repeatOnStart {
            youTubeStreamPlayer.playbackState.collect { state ->
                when (state) {
                    is YouTubeStreamPlaybackState.Prepared -> {

                        param1?.let {
                            youTubeStreamPlayer.loadVideo(it, 0f)
                        }
                    }

                    YouTubeStreamPlaybackState.UnStarted -> {
                        // delay 로 시작 시간 확보
                        launch {
                            delay(500)
                            youTubeStreamPlayer.start()
                        }
                    }

                    YouTubeStreamPlaybackState.Buffering -> {
                    }

                    YouTubeStreamPlaybackState.Paused -> {
                    }

                    YouTubeStreamPlaybackState.Playing -> {
                    }

                    YouTubeStreamPlaybackState.Ended -> {
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        youTubeStreamPlayer.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(argsParm1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val videoId = arguments?.getString(argsParm1) ?: ""
        _binding = FragmentBasicPlayerBinding.inflate(inflater, container, false)

        youTubePlayerView =
            YouTubePlayerView(requireActivity()).apply {
                layoutParams =
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    )
            }

        _binding?.playContainer?.removeAllViews()
        _binding?.playContainer?.addView(youTubePlayerView)
        youtubeStreamPlayerAdapter = YouTubeStreamPlayerAdapterImpl(youTubePlayerView)

        youTubeStreamPlayer =
            YouTubeStreamPlayerImpl(
                lifecycle = lifecycle,
                autoPlay = true,
                youtubeStreamPlayerAdapter = youtubeStreamPlayerAdapter,
                iFramePlayerOptions = IFramePlayerOptions.default,
            )

        lifecycle.addObserver(youTubePlayerView)

        youTubeStreamPlayer.initPlayer(networkHandle = false, videoId = videoId)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val composeView =
            youTubePlayerView.rootView?.findViewById<ComposeView>(com.sean.ratel.player.demo.R.id.player_controller)
        composeView?.setContent {
            PIPButton(pipViewModel)
        }
    }

    @Composable
    @Suppress("ktlint:standard:function-naming")
    fun PIPButton(pipViewModel: PIPManager) {
        val context = LocalContext.current
        val isPipMode by pipViewModel.pipClick.collectAsState(initial = PIPTarget("0", false))

        if (!isPipMode.isPipClick) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    border =
                        BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(Modifier.wrapContentSize()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .padding(10.dp),
                        ) {
                            Text(
                                "PIP",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Box(Modifier.fillMaxSize()) {
                            IconButton(
                                onClick = {
                                    if (!context.hasPipPermission()) {
                                        Toast
                                            .makeText(
                                                context,
                                                "설정 이동후 권한을 체크해 주세요",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        //  mainViewModel?.goSettingView()
                                    } else {
                                        Log.d("hbungshin", "clickPIP")
                                        pipViewModel.setPIPClick(
                                            R.id.fragment_basic_container.toString(),
                                            !isPipMode.isPipClick,
                                        )
                                    }
                                },
                                Modifier
                                    .width(84.dp)
                                    .height(84.dp),
                            ) {
                                Icon(
                                    imageVector = if (!isPipMode.isPipClick) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                                    contentDescription = null,
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onClickPipAction(context: Context) {
        val videoSize = youTubeStreamPlayer.getVideoSize()
        val isPlaying = youTubeStreamPlayer.isPlaying()

        val visibleRect = Rect()
        binding.root.getGlobalVisibleRect(visibleRect)

        val enterPipMode =
            pipViewModel.enterPipMode(
                videoSize,
                visibleRect,
                isPlaying = isPlaying,
                isFirst = true,
                isLast = true,
            )

        when (enterPipMode) {
            PipResult.NoSystemFeature -> {
                Toast
                    .makeText(
                        requireActivity(),
                        "메모리 부족 오류",
                        Toast.LENGTH_LONG,
                    ).show()
            }

            PipResult.NoPermission -> {
                runPIPSetting(context, pipSettingsLauncher)
            }

            else -> {}
        }
    }

    fun pipButtonState() {
        launch {
            pipViewModel.pipClick.collect {
                val visibleRect = Rect()
                binding.root.getGlobalVisibleRect(visibleRect)

                pipViewModel.updatePipParams(
                    youTubeStreamPlayer.isPlaying(),
                    youTubeStreamPlayer.getVideoSize(),
                    rect = visibleRect,
                )
            }
        }
    }

    fun pause() {
        youTubeStreamPlayer.pause()
    }

    fun play() {
        youTubeStreamPlayer.start()
    }

    companion object {
        @JvmStatic
        fun newInstance(videoId: String) =
            BasicPlayerFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(argsParm1, videoId)
                    }
            }
    }
}
