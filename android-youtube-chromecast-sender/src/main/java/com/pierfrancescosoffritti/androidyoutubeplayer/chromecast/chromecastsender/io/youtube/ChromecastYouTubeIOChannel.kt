package com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.youtube

import android.util.Log
import com.google.android.gms.cast.CastDevice
import com.google.android.gms.cast.framework.SessionManager
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext.Companion.TAG
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastCommunicationChannel
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.utils.JSONUtils
import so.smartlab.common.utils.log.RLog

/**
 * Communication channel used to exchange messages with the YouTube Chromecast receiver.
 */
internal class ChromecastYouTubeIOChannel(
  private val sessionManager: SessionManager
) : ChromecastCommunicationChannel {
  override val namespace get() = "urn:x-cast:com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.communication"

  override val observers = HashSet<ChromecastCommunicationChannel.ChromecastChannelObserver>()

  override fun sendMessage(message: String) {
    try {
      sessionManager.currentCastSession?.sendMessage(namespace, message)
//      val session = sessionManager.currentCastSession
//      Log.d("CHROME", "sendMessage session=$session")
//      Log.d("CHROME", "sendMessage namespace=$namespace")
//      Log.d("CHROME", "sendMessage message=$message")

      sessionManager.currentCastSession

        ?.sendMessage(namespace, message)

        ?.setResultCallback { status ->

          RLog.d(TAG, "[sendMessage] message = ${message}")

        }

    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }

  override fun onMessageReceived(castDevice: CastDevice, namespace: String, message: String) {
    val parsedMessage = JSONUtils.parseMessageFromReceiverJson(message)
    observers.forEach { it.onMessageReceived(parsedMessage) }
  }
}