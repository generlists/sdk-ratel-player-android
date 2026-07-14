package com.sean.ratel.player.demo.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.mediarouter.app.MediaRouteButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.sean.ratel.player.core.data.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerAdapterImpl
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerImpl
import com.sean.ratel.player.core.util.launch
import com.sean.ratel.player.core.util.repeatOnStart
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.databinding.FragmentCastPlayerBinding
import com.sean.ratel.player.demo.di.qualifier.WithControl
import com.sean.ratel.player.demo.ui.cast.MediaRouteButtonContainer
import com.sean.ratel.player.demo.ui.cast.MediaRouteButtonUtils
import com.sean.ratel.player.demo.ui.cast.PlayerState
import com.sean.ratel.player.demo.ui.cast.YouTubePlayersManager
import com.sean.ratel.player.demo.ui.screen.CastViewModel
import com.sean.ratel.player.demo.ui.view.component.PlayController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CastPlayerFragment : Fragment() {
    private val argParam2 = "param2"
    private val argParam3 = "param3"
    private var _binding: FragmentCastPlayerBinding? = null
    private val binding get() = _binding!!

    lateinit var youTubePlayerView: YouTubePlayerView
    private var playContainerView: FrameLayout? = null

    private var currentIndex = 0
    private var videoId: String? = null

    lateinit var mainViewModel: MainViewModel
    lateinit var castViewModel: CastViewModel

    private val googlePlayServicesAvailabilityRequestCode = 1

    @Inject
    lateinit var youTubePlayersManager: YouTubePlayersManager

    private lateinit var mediaRouteButton: MediaRouteButton

    private lateinit var mediaRouteButtonRoot: ViewGroup

    @WithControl
    @Inject
    lateinit var iFramePlayerOptions: IFramePlayerOptions

    @Inject
    lateinit var youtubeStreamPlayerTracker: YouTubePlayerTracker

    init {
        repeatOnStart {
            youTubePlayersManager.onLocalPlaybackState(this)
        }
        repeatOnStart {
            youTubePlayersManager.onCastPlaybackState(this)
        }

        repeatOnStart {
            youTubePlayersManager.onPlaySpeedRate(this)
        }

        repeatOnStart {
            youTubePlayersManager.debugString.collect { str ->
                mainViewModel.setPlayBackState(str)
            }
        }
        repeatOnStart {
            youTubePlayersManager.mute.collect { b ->
                mainViewModel.setMutePlay(mute = b)
            }
        }

        repeatOnStart {
            youTubePlayersManager.onPlayError(requireActivity(), this)
        }
        repeatOnStart {
            youTubePlayersManager.captionAvailable(this)
        }

        repeatOnStart {

            youTubePlayersManager.currentTime.collect { currentTime ->
                currentTime?.let {
                    if (currentTime > 0f) {
                        mainViewModel.setPlayCurrentTime(currentTime)
                    }
                }
            }
        }
        repeatOnStart {
            youTubePlayersManager.duration.collect { duration ->
                duration?.let {
                    if (duration > 0f) {
                        mainViewModel.setPlayDuration(duration)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        castViewModel = ViewModelProvider(requireActivity())[CastViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val videoIdList = arguments?.getStringArrayList(argParam2)

        _binding = FragmentCastPlayerBinding.inflate(inflater, container, false)
        playContainerView = _binding?.playContainer

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
        videoId = videoIdList?.get(currentIndex)

        mediaRouteButtonRoot = _binding?.mediaRouteButtonRoot as LinearLayout

        youTubePlayersManager.setVideoList(mainViewModel.youtubeGoogleCastModelList.value)

        youTubePlayersManager.setLocalPlayer(currentIndex, getPlayer(videoId))

        mediaRouteButton = MediaRouteButtonUtils.initMediaRouteButton(requireActivity())

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val videoIdList = arguments?.getStringArrayList(argParam2)
        val startIndex = arguments?.getInt(argParam3)

        val composeView =
            binding.root.findViewById<ComposeView>(R.id.player_controller)
        composeView?.setContent {
            val idList = videoIdList?.toCollection(ArrayList())

            val playerInit by youTubePlayersManager.localPlayerInit.collectAsStateWithLifecycle(
                initialValue = false,
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Red),
            ) {
                PlayController(
                    mainViewModel,
                    youTubePlayersManager,
                    currentIndex,
                    idList,
                    playIndex = {
                        currentIndex = it
                    },
                )
            }

            if (playerInit) {
                MediaRouteButtonUtils.addMediaRouteButtonToPlayerUi(
                    mediaRouteButton,
                    android.R.color.black,
                    null,
                    localPlayerUiMediaRouteButtonContainer,
                )
            }
            Log.d(
                "CHROME",
                "castValue : ${youTubePlayersManager.castEventManager.playerState.value}",
            )

            launch {
                youTubePlayersManager.castEventManager.playerState.collect { playerState ->
                    if (playerState == PlayerState.CAST) {
                        _binding?.playCastLayer?.visibility = View.VISIBLE
                    } else {
                        _binding?.playCastLayer?.visibility = View.GONE
                    }
                }
            }
        }
        launch {
            youTubePlayersManager.castEventManager.castSession.collect {
//                Log.d("CHROME","상태 : $it")
//                if(it is CastSessionState.SessionStart){
//                    videoId?.let{
//                        youTubePlayersManager.loadCastVideo(it,0f)
//                    }
//                }
            }
        }
    }

    private fun getPlayer(videoId: String?): YouTubeStreamPlayer {
        Log.d("getPlayer", "$iFramePlayerOptions")

        val youTubeStreamPlayer =
            YouTubeStreamPlayerImpl(
                lifecycle = requireActivity().lifecycle,
                autoPlay = true,
                youtubeStreamPlayerAdapter = YouTubeStreamPlayerAdapterImpl(youTubePlayerView),
                iFramePlayerOptions = iFramePlayerOptions,
            )

        youTubeStreamPlayer.initPlayer(networkHandle = false, videoId = videoId)
        youTubeStreamPlayer.setMute(mute = true)
        return youTubeStreamPlayer
    }

    override fun onResume() {
        super.onResume()
        Log.d(
            "YouTubeCastPlayerImpl",
            "onResume() videoId :  ${youTubePlayersManager.videoId.value}",
        )
        if (youTubePlayersManager.videoId.value.isNotEmpty()) {
            youTubePlayersManager.loadCastVideo(youTubePlayersManager.videoId.value, 0f)
        }
        // youTubePlayersManager.playPauseStart()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private val localPlayerUiMediaRouteButtonContainer =
        object : MediaRouteButtonContainer {
            override fun addMediaRouteButton(mediaRouteButton: MediaRouteButton) {
                Log.d("KKKKK", "addMediaRouteButton $mediaRouteButton")
                mediaRouteButtonRoot.addView(mediaRouteButton)
            }

            override fun removeMediaRouteButton(mediaRouteButton: MediaRouteButton) {
                Log.d("KKKKK", "removeMediaRouteButton $mediaRouteButton")
                mediaRouteButtonRoot.removeView(mediaRouteButton)
            }
        }

    companion object {
        @JvmStatic
        fun newInstance(
            videoIdList: List<String>?,
            startIndex: Int,
        ) = CastPlayerFragment().apply {
            arguments =
                Bundle().apply {
                    putStringArrayList(argParam2, videoIdList?.toCollection(ArrayList()))
                    putInt(argParam3, startIndex)
                }
        }

        val speedList =
            listOf<YouTubeStreamPlaybackRate>(
                YouTubeStreamPlaybackRate.RATE_0_25,
                YouTubeStreamPlaybackRate.RATE_0_5,
                YouTubeStreamPlaybackRate.RATE_0_75,
                YouTubeStreamPlaybackRate.RATE_1,
                YouTubeStreamPlaybackRate.RATE_1_25,
                YouTubeStreamPlaybackRate.RATE_1_5,
                YouTubeStreamPlaybackRate.RATE_1_75,
                YouTubeStreamPlaybackRate.RATE_2,
            )
    }
}
