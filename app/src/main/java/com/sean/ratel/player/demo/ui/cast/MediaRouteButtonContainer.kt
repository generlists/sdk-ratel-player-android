package com.sean.ratel.player.demo.ui.cast

import androidx.mediarouter.app.MediaRouteButton

interface MediaRouteButtonContainer {
    fun addMediaRouteButton(mediaRouteButton: MediaRouteButton)

    fun removeMediaRouteButton(mediaRouteButton: MediaRouteButton)
}
