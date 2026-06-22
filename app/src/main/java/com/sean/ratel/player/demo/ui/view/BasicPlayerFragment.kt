package com.sean.ratel.player.demo.ui.view

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Choreographer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlayQuality
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackCaptionState
import com.sean.ratel.player.core.data.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate
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
import com.sean.ratel.player.demo.data.youtube.domain.YouTubePlayerOptions
import com.sean.ratel.player.demo.databinding.FragmentBasicPlayerBinding
import com.sean.ratel.player.demo.di.qualifier.NotControl
import com.sean.ratel.player.utils.PlayerUtils.hasPipPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
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

    @Inject
    lateinit var pipViewModel: PIPManager

    @NotControl
    @Inject
    lateinit var iFramePlayerOptions: IFramePlayerOptions

    @Inject
    lateinit var playOptions: YouTubePlayerOptions

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
        repeatOnStart {
            youTubeStreamPlayer.videoSpeedChange.collect {
                Log.d("BasicPlayerFragment", "$it")
            }
        }
        repeatOnStart {
            youTubeStreamPlayer.videoQualityChange.collect {
                Log.d("OKJSPKK", "change : ${it.displayName()}")
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
                iFramePlayerOptions = iFramePlayerOptions,
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
            Column(Modifier.wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Control(pipViewModel)
                Quality()
                Caption()
            }
        }
    }

    @Composable
    @Suppress("ktlint:standard:function-naming")
    fun Control(pipViewModel: PIPManager) {
        val context = LocalContext.current
        val isPipMode by pipViewModel.pipClick.collectAsState(initial = PIPTarget("0", false))
        var clickSpeed by remember { mutableStateOf(false) }
        var currentRate by remember { mutableStateOf(YouTubeStreamPlaybackRate.UNKNOWN) }

        if (!isPipMode.isPipClick) {
            Box(
                Modifier
                    .fillMaxWidth()
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
                    Column(Modifier.fillMaxSize()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .padding(10.dp),
                        ) {
                            Text(
                                "Play Control",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .wrapContentSize(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Box(
                                Modifier
                                    .wrapContentSize()
                                    .weight(1.0f),
                            ) {
                                IconButton(
                                    onClick = {
                                        if (!context.hasPipPermission()) {
                                            Toast
                                                .makeText(
                                                    context,
                                                    "설정 이동후 권한을 체크해 주세요",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                        } else {
                                            Log.d("hbungshin", "clickPIP")
                                            pipViewModel.setPIPClick(
                                                R.id.fragment_basic_container.toString(),
                                                !isPipMode.isPipClick,
                                            )
                                        }
                                    },
                                    Modifier
                                        .width(48.dp)
                                        .height(48.dp),
                                ) {
                                    Icon(
                                        imageVector =
                                            if (!isPipMode.isPipClick) {
                                                Icons.Default.CloseFullscreen
                                            } else {
                                                Icons.Default.OpenInFull
                                            },
                                        contentDescription = null,
                                        tint = Color.White,
                                    )
                                }
                            }
                            Box(
                                Modifier
                                    .wrapContentSize()
                                    .weight(1.0f),
                            ) {
                                IconButton(
                                    onClick = {
                                        clickSpeed = true
                                        youTubeStreamPlayer.setPlaybackRate(YouTubeStreamPlaybackRate.RATE_2)
                                    },
                                    Modifier
                                        .width(48.dp)
                                        .height(48.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = Color.White,
                                    )
                                }
                            }
                            if (clickSpeed) {
                                PlaybackRateBottomSheet(currentRate, onRateSelected = {
                                    Log.d("OKJSP", "onRateSelected : $it")
                                    youTubeStreamPlayer.setPlaybackRate(it)
                                    currentRate = it
                                }, onDismiss = {
                                    clickSpeed = false
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    @Suppress("ktlint:standard:function-naming")
    fun Quality() {
        var clickQuality by remember { mutableStateOf(false) }
        var currentQuality by remember { mutableStateOf<YouTubeStreamPlayQuality>(YouTubeStreamPlayQuality.Auto) }
        val availableQualities by youTubeStreamPlayer.videoQualityLevel.collectAsStateWithLifecycle()
        val captionAvailable by youTubeStreamPlayer.captionAvailable.collectAsStateWithLifecycle()
        val state =
            when {
                !captionAvailable -> YouTubeStreamPlaybackCaptionState.UNSUPPORTED
                captionAvailable -> YouTubeStreamPlaybackCaptionState.ENABLED
                else -> YouTubeStreamPlaybackCaptionState.DISABLED
            }
        Log.d("QualityQuality", "captionAvailable : $captionAvailable")
        Box(
            Modifier
                .fillMaxWidth()
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
                Column(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                            .padding(10.dp),
                    ) {
                        Text(
                            "Play Quality",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            Modifier
                                .size(48.dp)
                                .clickable {
                                    clickQuality = true
                                },
                            tint = Color.White,
                        )
                    }

                    if (clickQuality) {
                        QualityBottomSheet(
                            currentQuality = currentQuality,
                            availableQualities = availableQualities,
                            onQualitySelected = {
                                Log.d("OKJSP", "onRateSelected : $it")
                                youTubeStreamPlayer.setQuality(it)
                                currentQuality = it
                            },
                            onDismiss = {
                                clickQuality = false
                            },
                        )
                    }
                }
            }
        }
    }

    @Composable
    @Suppress("ktlint:standard:function-naming")
    fun Caption() {
        val captionAvailable by youTubeStreamPlayer.captionAvailable.collectAsStateWithLifecycle()
        var initCaptionState by remember { mutableStateOf(if (playOptions.ccLoadPolicy == 1) true else false) }
        val state =
            when {
                !captionAvailable -> YouTubeStreamPlaybackCaptionState.UNSUPPORTED
                captionAvailable -> YouTubeStreamPlaybackCaptionState.ENABLED
                else -> YouTubeStreamPlaybackCaptionState.UNSUPPORTED
            }
        Log.d("CAPTION_AVAILABLE", "state : $state")

        Box(
            Modifier
                .fillMaxWidth()
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
                Column(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                            .padding(10.dp),
                    ) {
                        Text(
                            "Caption",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClosedCaption,
                            contentDescription = null,
                            Modifier
                                .size(48.dp)
                                .clickable {
                                    Log.d(
                                        "CAPTION_AVAILABLE",
                                        "captionAvailable : $captionAvailable",
                                    )

                                    if (initCaptionState) {
                                        youTubeStreamPlayer.disableCaptions()

                                        initCaptionState = false
                                    } else {
//
                                        initCaptionState = true
                                        youTubeStreamPlayer.enableCaptions(Locale.getDefault().language)
                                    }
                                },
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    @Suppress("ktlint:standard:function-naming")
    fun PlaybackRateBottomSheet(
        currentRate: YouTubeStreamPlaybackRate,
        onRateSelected: (YouTubeStreamPlaybackRate) -> Unit,
        onDismiss: () -> Unit,
    ) {
        val rates = YouTubeStreamPlaybackRate.entries.filter { it != YouTubeStreamPlaybackRate.UNKNOWN }

        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = stringResource(R.string.play_speed),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
                rates.forEach { rate ->
                    val isSelected = rate == currentRate
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onRateSelected(rate)
                                    onDismiss()
                                }.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${rate.rate}x",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    @Suppress("ktlint:standard:function-naming")
    fun QualityBottomSheet(
        currentQuality: YouTubeStreamPlayQuality,
        availableQualities: List<YouTubeStreamPlayQuality>,
        onQualitySelected: (YouTubeStreamPlayQuality) -> Unit,
        onDismiss: () -> Unit,
    ) {
        var clickSpeed by remember { mutableStateOf(false) }
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = "Video Quality",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )

                availableQualities.forEach { quality ->
                    val isSelected = quality == currentQuality

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onQualitySelected(quality)
                                    onDismiss()
                                }.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = quality.displayName(),
                            style = MaterialTheme.typography.bodyLarge,
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            modifier = Modifier.weight(1f),
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun YouTubeStreamPlayQuality.displayName(): String =
        when (this) {
            YouTubeStreamPlayQuality.Auto -> "Auto"
            YouTubeStreamPlayQuality.P1080 -> "1080P"
            YouTubeStreamPlayQuality.P720 -> "720P"
        }

    fun onClickPipAction(context: Context) {
        val v = binding.root
        val rect = Rect()
        binding.root.invalidate()
        Choreographer.getInstance().postFrameCallback {
            v.getGlobalVisibleRect(rect)

            val screenWidth = v.width
            val screenHeight = v.height

            Log.d(
                "onClickPipButton",
                """
                rootHeight=$screenHeight
                rootWidth=$screenWidth
                rect=$rect
                rectHeight=${ rect.height()}
                 address=${this.hashCode()}
                """.trimIndent(),
            )

            val enterPipMode =

                pipViewModel.enterPipMode(
                    videoSize = Size(screenWidth, screenHeight),
                    rect = rect,
                    isPlaying = true,
                    isFirst = null,
                    isLast = null,
                )

            when (enterPipMode) {
                PipResult.NoSystemFeature -> {
                    Toast.makeText(requireActivity(), "PIP error", Toast.LENGTH_LONG).show()
                }

                PipResult.NoPermission -> {
                }

                else -> {
                    Unit
                }
            }
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
