package com.sean.ratel.player.core.com.sean.ratel.player.core.util

import android.content.Context
import android.media.AudioManager

object PlayerUtil {


    fun isSystemVolumeMute(context: Context): Boolean {

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
    }
}