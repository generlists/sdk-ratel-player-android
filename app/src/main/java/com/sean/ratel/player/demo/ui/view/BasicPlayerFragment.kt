package com.sean.ratel.player.demo.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.sean.ratel.player.core.data.domain.YouTubeStreamPlayer
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerAdapterImpl
import com.sean.ratel.player.core.data.player.youtube.YouTubeStreamPlayerImpl
import com.sean.ratel.player.core.data.player.youtube.adaptor.YouTubeStreamPlayerAdapter
import com.sean.ratel.player.core.util.launch
import com.sean.ratel.player.core.util.repeatOnStart
import com.sean.ratel.player.demo.databinding.FragmentBasicPlayerBinding
import com.sean.ratel.player.demo.di.qualifier.NotControl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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
