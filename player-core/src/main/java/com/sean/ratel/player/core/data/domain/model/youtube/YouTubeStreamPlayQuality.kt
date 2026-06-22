package com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.youtube

sealed class YouTubeStreamPlayQuality {
    object Auto : YouTubeStreamPlayQuality()

    object P720 : YouTubeStreamPlayQuality()

    object P1080 : YouTubeStreamPlayQuality()
}
