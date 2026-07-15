package com.sean.ratel.player.core.data.player.youtube.adaptor

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayerBridge
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener

interface YouTubeCastPlayerAdapter {
    fun getChromeCastContext(): ChromecastYouTubePlayerContext

    fun initialize(
        youTubePlayerListener: YouTubePlayerListener,
        bridgeCallback: YouTubePlayerBridge.YouTubePlayerBridgeCallbacks,
    )

    fun release()

    fun endCurrentSession()

    fun onChromecastConnecting()

    fun onChromecastConnected(
        castDevice: String?,
        chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext,
    )

    fun addChromecastConnectionListener(chromecastConnectionListener: ChromecastConnectionListener)

    fun removeChromecastConnectionListener(chromecastConnectionListener: ChromecastConnectionListener)
}
