package com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.youtube

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayerBridge
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.youtube.adaptor.YouTubeCastPlayerAdapter

class YouTubeCastPlayerAdapterImpl(
    private val chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext,
) : YouTubeCastPlayerAdapter {
    override fun getChromeCastContext(): ChromecastYouTubePlayerContext = chromecastYouTubePlayerContext

    override fun initialize(youTubePlayerListener: YouTubePlayerListener,bridgeCallback: YouTubePlayerBridge.YouTubePlayerBridgeCallbacks) {
        chromecastYouTubePlayerContext.initialize(youTubePlayerListener,bridgeCallback)
    }

    override fun release() {
        chromecastYouTubePlayerContext.release()
    }

    override fun endCurrentSession() {
        chromecastYouTubePlayerContext.endCurrentSession()
    }

    override fun onChromecastConnecting() {
        chromecastYouTubePlayerContext.onChromecastConnecting()
    }

    override fun onChromecastConnected(castDevice: String?,chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        chromecastYouTubePlayerContext.onChromecastConnected(castDevice,chromecastYouTubePlayerContext)
    }

    override fun addChromecastConnectionListener(chromecastConnectionListener: ChromecastConnectionListener) {
        chromecastYouTubePlayerContext.addChromecastConnectionListener(chromecastConnectionListener)
    }

    override fun removeChromecastConnectionListener(chromecastConnectionListener: ChromecastConnectionListener) {
        chromecastYouTubePlayerContext.removeChromecastConnectionListener(chromecastConnectionListener)
    }
}
