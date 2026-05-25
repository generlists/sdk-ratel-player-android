package com.sean.ratel.player.demo


import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DemoApplication :
    MultiDexApplication(){

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
    }
}
