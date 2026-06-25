package com.sean.ratel.player.demo.data.youtube.domain

import com.sean.ratel.player.demo.di.qualifier.AutoPlay
import com.sean.ratel.player.demo.di.qualifier.CCLangPref
import com.sean.ratel.player.demo.di.qualifier.CCLoadPolicy
import com.sean.ratel.player.demo.di.qualifier.Control
import com.sean.ratel.player.demo.di.qualifier.EnableJsApi
import com.sean.ratel.player.demo.di.qualifier.FS
import com.sean.ratel.player.demo.di.qualifier.IVLoadPolicy
import com.sean.ratel.player.demo.di.qualifier.Mute
import com.sean.ratel.player.demo.di.qualifier.Rel
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
data class YouTubePlayerOptions
    @Inject
    constructor(
        @AutoPlay val autoPlay: Int,
        @Mute val mute: Int,
        @Control var control: Int,
        @EnableJsApi var enableJsApi: Int,
        @FS var fs: Int,
        @Rel var rel: Int,
        @IVLoadPolicy var ivLoadPolicy: Int,
        @CCLoadPolicy var ccLoadPolicy: Int,
        @CCLangPref var ccLangPref: String,
    )
