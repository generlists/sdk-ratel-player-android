package com.sean.ratel.player.core.data.domain

import android.util.Size
import android.view.View
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlayQuality
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlayerError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface YouTubeStreamPlayer {
    val playbackState: Flow<YouTubeStreamPlaybackState>

    val playbackError: Flow<YouTubeStreamPlayerError>

    val duration: StateFlow<Float>

    val currentTime: StateFlow<Float>

    val videoSpeedChange: StateFlow<YouTubeStreamPlaybackRate?>

    val videoQualityLevel: StateFlow<List<YouTubeStreamPlayQuality>>

    val videoQualityChange: StateFlow<YouTubeStreamPlayQuality>

    val captionAvailable: StateFlow<Boolean>

    val fullScreenView: StateFlow<View?>
    val exitFullScreen: StateFlow<Boolean>

    fun getYouTubePlayerView(): View

    fun initPlayer(
        networkHandle: Boolean? = false,
        videoId: String? = null,
    )

    fun loadOrCueVideo(
        videoId: String,
        startTime: Float? = 0f,
    )

    fun loadVideo(
        videoId: String,
        startTime: Float? = 0f,
    )

    fun cueVideo(
        videoId: String,
        startTime: Float? = 0f,
    )

    fun start()

    fun seekTo(msec: Float)

    fun resume()

    fun pause()

    fun stop()

    fun isPlaying(): Boolean

    fun release()

    fun setMute(mute: Boolean)

    fun setPlaybackRate(playbackRate: YouTubeStreamPlaybackRate)

    fun getVideoSize(): Size?

    fun toggleFullscreen()

    fun setLoop(loop: Boolean)

    fun setShuffle(shuffle: Boolean)

    fun addFullscreenListener(): Boolean

    fun removeFullscreenListener(): Boolean

    fun setQuality(quality: YouTubeStreamPlayQuality)

    fun enableCaptions(languageCode: String)

    fun disableCaptions()
}
