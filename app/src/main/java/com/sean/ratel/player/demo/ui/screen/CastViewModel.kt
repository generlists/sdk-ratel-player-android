package com.sean.ratel.player.demo.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.cast.framework.CastContext
import com.sean.ratel.player.demo.ui.cast.YouTubePlayersManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CastViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val youTubePlayersManager: YouTubePlayersManager,
    ) : ViewModel() {
        private val castContext = CastContext.getSharedInstance(context)
        private val sessionManager = castContext.sessionManager

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected
    }
