package com.sean.ratel.player.core.data.domain

import android.net.Uri
import android.view.Surface
import android.view.SurfaceView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.sean.ratel.player.core.Configurations
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.PlaySpeed
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.RepeatMode
import com.sean.ratel.player.core.data.domain.model.PlayMuteInfo
import com.sean.ratel.player.core.data.domain.model.PlaybackState
import com.sean.ratel.player.core.data.domain.model.Resolution
import com.sean.ratel.player.core.data.domain.model.SampleBandWidth
import com.sean.ratel.player.core.data.domain.model.track.AudioTrack
import com.sean.ratel.player.core.data.domain.model.track.VideoTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MediaStreamPlayer {

    val playbackState: Flow<PlaybackState>

    val resolution: Flow<Resolution>

    val sampleBandWidth: Flow<SampleBandWidth>

    val isMute: StateFlow<PlayMuteInfo>

    val maximumVideoQuality: StateFlow<Int>

    val videoTracks: StateFlow<List<VideoTrack>>

    val audioTracks: StateFlow<List<AudioTrack>>

    val selectedVideoTrack: StateFlow<VideoTrack?>

    val selectedAudioTrack: StateFlow<AudioTrack?>

    val isShuffleOn: StateFlow<Boolean>


    val duration: StateFlow<Long?>

    val currentPosition:StateFlow<Long?>

    val repeatMode:StateFlow<RepeatMode>

    val seekBackIncrement: StateFlow<Long>

    val seekForwardIncrement: StateFlow<Long>

    val playSpeed:StateFlow<PlaySpeed>


    fun getPlayer():ExoPlayer?


    fun setPlayerConfig(configurations: Configurations)


    fun start(uri: Uri)

    fun start(
        uri: Uri,
        cacheKey: String? = null)

    fun start(
        items: List<MediaItem>,
        startIndex: Int = 0,
        cacheKey: String? = null
    )

    fun isPrevItem():Boolean

    fun pervPlay()

    fun isNextItem():Boolean

    fun nextPlay()

    fun seekForward()

    fun seekBack()


    fun resume()

    fun pause()

    fun stop()

    fun seekTo(msec: Long)

    fun isPlaying(): Boolean

    fun isPlayComplete(): Boolean


    fun getBufferedPosition(): Long

    fun release()

    fun setVideoSurface(surface: Surface?)

    fun setVideoSurfaceView(surface: SurfaceView?)

    fun clearVideoSurface()

    fun setMute(mute: Boolean)

    fun setMute(playIndex: Int, mute: Boolean)

    fun setMaximumVideoQuality(quality: Int, isUserSelect: Boolean = true)

    fun setShuffleOn(isShuffle:Boolean)

    fun setRepeat(repeatMode: RepeatMode)

    fun setPlaySpeed(playSpeed: PlaySpeed)
}