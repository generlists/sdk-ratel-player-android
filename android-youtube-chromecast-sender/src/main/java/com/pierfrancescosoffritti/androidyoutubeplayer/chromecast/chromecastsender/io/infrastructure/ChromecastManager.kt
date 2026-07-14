package com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure

import android.util.Log
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext.Companion.TAG
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.youtube.ChromecastCommunicationConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.youtube.ChromecastYouTubeIOChannel
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.utils.JSONUtils
import so.smartlab.common.utils.log.RLog
import java.util.Locale

/**
 * Class responsible for chromecast sessions.
 */
internal class ChromecastManager(
  private val chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext,
  private val sessionManager: SessionManager,
  private val chromecastConnectionListeners: Set<ChromecastConnectionListener>
) : CastSessionListener {

  val chromecastCommunicationChannel = ChromecastYouTubeIOChannel(sessionManager)
  private val castSessionManagerListener = CastSessionManagerListener(this)

  override fun onCastSessionConnecting() {
    chromecastConnectionListeners.forEach { it.onChromecastConnecting() }
  }

  override fun onCastSessionConnected(castSession: CastSession) {
    castSession.removeMessageReceivedCallbacks(chromecastCommunicationChannel.namespace)
    castSession.setMessageReceivedCallbacks(
      chromecastCommunicationChannel.namespace,
      chromecastCommunicationChannel
    )

    sendCommunicationConstants(chromecastCommunicationChannel)

    chromecastYouTubePlayerContext.onChromecastConnected(castSession.castDevice?.friendlyName,chromecastYouTubePlayerContext)
    chromecastConnectionListeners.forEach { it.onChromecastConnected(castSession.castDevice?.friendlyName,chromecastYouTubePlayerContext) }
  }

  override fun onCastSessionDisconnected(castSession: CastSession) {
    castSession.removeMessageReceivedCallbacks(chromecastCommunicationChannel.namespace)

    chromecastYouTubePlayerContext.onChromecastDisconnected()
    chromecastConnectionListeners.forEach { it.onChromecastDisconnected() }
  }

  fun restoreSession() {
    RLog.d(TAG,"LIB [restoreSession] restoreSession")
    val currentCastSessions = sessionManager.currentCastSession
//    Log.d("CHROME", "restoreSession session=$currentCastSessions")
//    Log.d("CHROME", "isConnected=${currentCastSessions?.isConnected}")
//    Log.d("CHROME", "castDevice=${currentCastSessions?.castDevice?.friendlyName}")
    if (currentCastSessions != null)
      onCastSessionConnected(currentCastSessions)
  }

  fun endCurrentSession() {
    RLog.d(TAG,"LIB [endCurrentSession]endCurrentSession")
    sessionManager.endCurrentSession(true)
  }

  fun addSessionManagerListener() =
    sessionManager.addSessionManagerListener(castSessionManagerListener, CastSession::class.java)

  fun removeSessionManagerListener() =
    sessionManager.removeSessionManagerListener(castSessionManagerListener, CastSession::class.java)

  fun release() {
    RLog.d(TAG,"LIB [release]")
    removeSessionManagerListener()
  }

  private fun sendCommunicationConstants(chromecastCommunicationChannel: ChromecastCommunicationChannel) {
    val communicationConstants = ChromecastCommunicationConstants.asJson()
    RLog.d(TAG,"LIB [sendCommunicationConstants] ${Locale.getDefault().toLanguageTag()}")
    val message = JSONUtils.buildCommunicationConstantsJson(
      "command" to ChromecastCommunicationConstants.INIT_COMMUNICATION_CONSTANTS,
      "communicationConstants" to communicationConstants,
      "locale" to Locale.getDefault().toLanguageTag()
    )

    chromecastCommunicationChannel.sendMessage(message)
  }
}