package com.sean.ratel.player.demo

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions =
        CastOptions
            .Builder()
            .setReceiverApplicationId("07130C4B")
            .build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null
}
