package com.sean.ratel.player.demo.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerAdapterImpl
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerImpl
import com.sean.ratel.player.core.data.player.youtube.adaptor.YouTubeStreamPlayerAdapter
import com.sean.ratel.player.core.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlaybackRate
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.domain.model.youtube.YouTubeStreamPlayerError
import com.sean.ratel.player.core.util.repeatOnStart
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.databinding.FragmentAdvancePlayerBinding
import com.sean.ratel.player.demo.di.qualifier.WithControl
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AdvancePlayerFragment : Fragment() {

    private val ARG_PARAM2 = "param2"
    private var _binding: FragmentAdvancePlayerBinding? = null
    private val binding get() = _binding!!


    lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var youTubeStreamPlayer: YouTubeStreamPlayer
    private lateinit var youtubeStreamPlayerAdapter: YouTubeStreamPlayerAdapter
    private var currentIndex = 0
    private var isFullscreen = false

    lateinit var mainViewModel: MainViewModel

    @WithControl
    @Inject
    lateinit var iFramePlayerOptions: IFramePlayerOptions

    @Inject
    lateinit var youtubeStreamPlayerTracker: YouTubePlayerTracker

    init {
        repeatOnStart {
            youTubeStreamPlayer.videoSpeedChange.collect { rate ->
                rate?.let {}
            }
        }

        repeatOnStart {
            youTubeStreamPlayer.fullScreenView.collect { fullscreenView ->
                fullscreenView?.let {
                    isFullscreen = true
                    binding.playContainer.visibility = View.GONE
                    binding.playerController.visibility = View.GONE
                    binding.fullScreenViewContainer.visibility = View.VISIBLE
                    binding.fullScreenViewContainer.addView(fullscreenView)
                }
            }
        }

        repeatOnStart {
            youTubeStreamPlayer.exitFullScreen.collect { b ->
                if (b) {

                    isFullscreen = false
                    binding.playContainer.visibility = View.VISIBLE
                    binding.playerController.visibility = View.VISIBLE
                    binding.fullScreenViewContainer.visibility = View.GONE
                    binding.fullScreenViewContainer.removeAllViews()

                }
            }
        }

        repeatOnStart {
            youTubeStreamPlayer.playbackState.collect { state ->
                val value =
                    if (state is YouTubeStreamPlaybackState.Prepared) "Prepared" else state.toString()
                mainViewModel.setPlayBackState(value)

                if (state is YouTubeStreamPlaybackState.Playing) {
                    youTubeStreamPlayer.setMute(false)
                    mainViewModel.setMutePlay(mute = false)
                }
            }
        }

        repeatOnStart {
            youTubeStreamPlayer.playbackError.collect { state ->
                if (state != YouTubeStreamPlayerError.UNKNOWN) {
                    Toast.makeText(requireActivity(), "$state", Toast.LENGTH_LONG).show()
                }
            }
        }

        repeatOnStart {

            youTubeStreamPlayer.currentTime.collect { currentTime ->
                if (currentTime > 0f) {
                    mainViewModel.setPlayCurrentTime(currentTime)

                }
            }
        }
        repeatOnStart {
            youTubeStreamPlayer.duration.collect { duration ->
                if (duration > 0f) {
                    mainViewModel.setPlayDuration(duration)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val videoIdList = arguments?.getStringArrayList(ARG_PARAM2)
        _binding = FragmentAdvancePlayerBinding.inflate(inflater, container, false)

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
        lifecycle.addObserver(youTubePlayerView)

        youtubeStreamPlayerAdapter = YouTubeStreamPlayerAdapterImpl(youTubePlayerView)

        youTubeStreamPlayer =
            YouTubeStreamPlayerImpl(
                lifecycle = lifecycle,
                autoPlay = true,
                youtubeStreamPlayerAdapter = youtubeStreamPlayerAdapter,
                iFramePlayerOptions = iFramePlayerOptions,
            )
        val videoID = videoIdList?.get(currentIndex)
        youTubeStreamPlayer.initPlayer(networkHandle = false, videoId = videoID)
        youTubeStreamPlayer.addFullscreenListener()
        youTubeStreamPlayer.setMute(mute = true) //디폴트 false

        //toggle
        _binding?.fullscreenBtn?.setOnClickListener {
            youTubeStreamPlayer.toggleFullscreen()
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val videoIdList = arguments?.getStringArrayList(ARG_PARAM2)
        val composeView =
            binding.root.findViewById<ComposeView>(R.id.player_controller)
        composeView?.setContent {
            val idList = videoIdList?.toCollection(ArrayList())
            PlayController(idList)
        }
    }

    @Composable
    fun PlayController(videoList: ArrayList<String>?) {


        Scaffold(
            topBar = {},

            ) { innerPadding ->

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {

                Column(
                    Modifier
                        .wrapContentSize()
                        .align(Alignment.TopStart)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                    DebugState()
                    PervNextPlay(videoList)
                    SpeedPlay()
                    SoundOnOff()
                }
            }
        }
    }

    @Composable
    fun DebugState() {
        val playstate = mainViewModel.playState.collectAsState()
        val currentTime = mainViewModel.currentTime.collectAsState()
        val duration = mainViewModel.duration.collectAsState()

        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.LightGray)
            ) {
                Text(
                    stringResource(R.string.play_state),
                    Modifier
                        .wrapContentSize()
                        .padding(start = 5.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    playstate.value,
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(start = 5.dp, end = 5.dp, top = 5.dp),
                    color = Color.White,

                    )
                Text(
                    String.format("currentTime :  %s", formatSecondsToTime(currentTime.value)),
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(start = 5.dp, end = 5.dp),
                    color = Color.White,

                    )
                Text(
                    String.format("duration :  %s", formatSecondsToTime(duration.value)),
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
                    color = Color.White,

                    )

            }

        }


    }

    @Composable
    fun OnOffRadioButton(
        isOn: Boolean,
        onToggle: (Boolean) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isOn) MaterialTheme.colorScheme.primary else Color(0xFFB0BEC5))
                .clickable { onToggle(!isOn) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isOn,
                onClick = { onToggle(!isOn) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isOn) "ON" else "OFF",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    fun PervNextPlay(videoList: List<String>?) {
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 5.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.LightGray)
            ) {
                Text(
                    stringResource(R.string.video_move),
                    Modifier
                        .wrapContentSize()
                        .padding(start = 5.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(60.dp), verticalAlignment = Alignment.CenterVertically

            ) {
                Button(onClick = {
                    val playIndex = currentIndex - 1
                    if (playIndex >= 0 && playIndex < (videoList?.size ?: 0)) {
                        videoList?.get(playIndex)
                            ?.let { youTubeStreamPlayer.loadOrCueVideo(it, 0f) }
                        currentIndex -= 1
                    } else {
                        Toast.makeText(context, R.string.first_video, Toast.LENGTH_SHORT).show()
                    }

                }, Modifier.padding(start = 10.dp, end = 10.dp)) {
                    Text(stringResource(R.string.prev_play))
                }
                Button(onClick = {
                    val playIndex = currentIndex + 1
                    if (playIndex < (videoList?.size ?: 0)) {
                        videoList?.get(playIndex)
                            ?.let { youTubeStreamPlayer.loadOrCueVideo(it, 0f) }
                        currentIndex += 1
                    } else {
                        Toast.makeText(context, R.string.last_video, Toast.LENGTH_SHORT).show()
                    }


                }) {
                    Text(stringResource(R.string.next_play))
                }
            }

        }

    }

    @Composable
    fun SpeedPlay() {
        var selectedItem by remember { mutableStateOf(YouTubeStreamPlaybackRate.UNKNOWN) }
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 5.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.LightGray)
            ) {
                Text(
                    stringResource(R.string.speed_play),
                    Modifier
                        .wrapContentSize()
                        .padding(start = 5.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                Modifier
                    .wrapContentWidth()
                    .height(70.dp), verticalAlignment = Alignment.CenterVertically

            ) {
                SelectBox(
                    speedList,
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it },
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp)
                )
                youTubeStreamPlayer.setPlaybackRate(selectedItem)
            }

        }
    }

    @Composable
    fun SoundOnOff() {
        var isSoundOnOff by remember { mainViewModel.mutePlay }
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 5.dp)
        ) {

            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.LightGray)
            ) {
                Text(
                    stringResource(R.string.sound_on_off),
                    Modifier
                        .wrapContentSize()
                        .padding(start = 5.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                youTubeStreamPlayer.setMute(mute = isSoundOnOff)
                OnOffRadioButton(
                    isOn = isSoundOnOff,
                    onToggle = { isSoundOnOff = it }
                )

            }

        }
    }

    @Composable
    fun SelectBox(
        items: List<YouTubeStreamPlaybackRate>,
        selectedItem: YouTubeStreamPlaybackRate,
        onItemSelected: (YouTubeStreamPlaybackRate) -> Unit,
        modifier: Modifier = Modifier
    ) {
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = modifier) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = selectedItem.name)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    fun formatSecondsToTime(seconds: Float): String {
        val totalSeconds = seconds.toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60

        return String.format(Locale.KOREA, "%02d:%02d:%02d", hours, minutes, secs)
    }

    companion object {

        @JvmStatic
        fun newInstance(videoIdList: List<String>?) =
            AdvancePlayerFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_PARAM2, videoIdList?.toCollection(ArrayList()))
                }
            }

        val speedList = listOf<YouTubeStreamPlaybackRate>(
            YouTubeStreamPlaybackRate.RATE_0_25,
            YouTubeStreamPlaybackRate.RATE_0_5,
            YouTubeStreamPlaybackRate.RATE_0_75,
            YouTubeStreamPlaybackRate.RATE_1,
            YouTubeStreamPlaybackRate.RATE_1_25,
            YouTubeStreamPlaybackRate.RATE_1_5,
            YouTubeStreamPlaybackRate.RATE_1_75,
            YouTubeStreamPlaybackRate.RATE_2
        )
    }

}