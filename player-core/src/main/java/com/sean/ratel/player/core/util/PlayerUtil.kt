package com.sean.ratel.player.core.com.sean.ratel.player.core.util

import android.content.Context
import android.media.AudioManager
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlayerError

object PlayerUtil {
    fun isSystemVolumeMute(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
    }

    fun getConvertPlaybackRateToYouTubePlaybackRate(rate: PlayerConstants.PlaybackRate): YouTubeStreamPlaybackRate =
        when (rate) {
            PlaybackRate.UNKNOWN -> YouTubeStreamPlaybackRate.UNKNOWN
            PlaybackRate.RATE_0_25 -> YouTubeStreamPlaybackRate.RATE_0_25
            PlaybackRate.RATE_0_5 -> YouTubeStreamPlaybackRate.RATE_0_5
            PlaybackRate.RATE_0_75 -> YouTubeStreamPlaybackRate.RATE_0_75
            PlaybackRate.RATE_1 -> YouTubeStreamPlaybackRate.RATE_1
            PlaybackRate.RATE_1_25 -> YouTubeStreamPlaybackRate.RATE_1_25
            PlaybackRate.RATE_1_5 -> YouTubeStreamPlaybackRate.RATE_1_5
            PlaybackRate.RATE_1_75 -> YouTubeStreamPlaybackRate.RATE_1_75
            PlaybackRate.RATE_2 -> YouTubeStreamPlaybackRate.RATE_2
        }

    fun getConvertYouTubePlaybackRatYoPlaybackRate(rate: YouTubeStreamPlaybackRate): PlaybackRate =
        when (rate) {
            YouTubeStreamPlaybackRate.UNKNOWN -> PlaybackRate.UNKNOWN
            YouTubeStreamPlaybackRate.RATE_0_25 -> PlaybackRate.RATE_0_25
            YouTubeStreamPlaybackRate.RATE_0_5 -> PlaybackRate.RATE_0_5
            YouTubeStreamPlaybackRate.RATE_0_75 -> PlaybackRate.RATE_0_75
            YouTubeStreamPlaybackRate.RATE_1 -> PlaybackRate.RATE_1
            YouTubeStreamPlaybackRate.RATE_1_25 -> PlaybackRate.RATE_1_25
            YouTubeStreamPlaybackRate.RATE_1_5 -> PlaybackRate.RATE_1_5
            YouTubeStreamPlaybackRate.RATE_1_75 -> PlaybackRate.RATE_1_75
            YouTubeStreamPlaybackRate.RATE_2 -> PlaybackRate.RATE_2
        }

    fun getConvertPlayerErrorToYouTubeStreamPlayerError(error: PlayerConstants.PlayerError): YouTubeStreamPlayerError =
        when (error) {
            PlayerConstants.PlayerError.UNKNOWN -> {
                YouTubeStreamPlayerError.UNKNOWN
            }

            PlayerConstants.PlayerError.INVALID_PARAMETER_IN_REQUEST -> {
                YouTubeStreamPlayerError.INVALID_PARAMETER_IN_REQUEST
            }

            PlayerConstants.PlayerError.HTML_5_PLAYER -> {
                YouTubeStreamPlayerError.HTML_5_PLAYER
            }

            PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> {
                YouTubeStreamPlayerError.VIDEO_NOT_FOUND
            }

            PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER -> {
                YouTubeStreamPlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER
            }
        }

}
