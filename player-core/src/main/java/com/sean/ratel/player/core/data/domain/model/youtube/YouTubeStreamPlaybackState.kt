package com.sean.ratel.player.core.data.domain.model.youtube

import com.sean.ratel.player.core.data.domain.YouTubeStreamPlayer

sealed class YouTubeStreamPlaybackState {

    data object IFrameReady : YouTubeStreamPlaybackState()

    data object Ready : YouTubeStreamPlaybackState()

    data object UnKnown : YouTubeStreamPlaybackState()

    data object UnStarted : YouTubeStreamPlaybackState()

    data class Prepared(
        val youTubePlayer: YouTubeStreamPlayer,
    ) : YouTubeStreamPlaybackState()

    data object Playing : YouTubeStreamPlaybackState()

    data object Paused : YouTubeStreamPlaybackState()

    data object Buffering : YouTubeStreamPlaybackState()

    data object Ended : YouTubeStreamPlaybackState()

    data object RELEASE : YouTubeStreamPlaybackState()

    data class Error(
        val error: YouTubeStreamPlayerError,
    ) : YouTubeStreamPlaybackState()
}
