package com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.viewmodel

import androidx.annotation.OptIn
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.CONTENT_SCALES
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.PlaySpeed
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.RepeatMode
import com.sean.ratel.player.core.data.domain.MediaStreamPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val mediaStreamPlayer: MediaStreamPlayer
) : ViewModel(){

    private val _isPlaying = MutableStateFlow<Boolean>(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _backWardMs = mediaStreamPlayer.seekBackIncrement
    val backWardMs: StateFlow<Long> = _backWardMs

    private val _forWardMs = mediaStreamPlayer.seekBackIncrement
    val forWardMs: StateFlow<Long> = _forWardMs

    private val _durationMs = MutableStateFlow<Long>(0L)
    val durationMs: StateFlow<Long> = _durationMs

    private val _currentTimeMs = MutableStateFlow<Long>(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs


    private val _isShuffle = MutableStateFlow<Boolean>(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle


    private val _repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.REPEAT_OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode


    private val _playSpeed = MutableStateFlow<PlaySpeed>(PlaySpeed.PlaySpeed_1_0)
    val playSpeed: StateFlow<PlaySpeed> = _playSpeed

    private val toggleModeSequence: List<RepeatMode> =
        listOf(RepeatMode.REPEAT_OFF, RepeatMode.REPEAT_ONE, RepeatMode.REPEAT_ALL)

    val speedSelection: List<PlaySpeed> = PlaySpeed.entries.map { it }

    private val _showControls = mutableStateOf<Boolean>(true)
    var showControls = _showControls

    private var _currentContentScaleIndex  = mutableIntStateOf(0)
    var currentContentScaleIndex = _currentContentScaleIndex

    fun getCurrentContentScaleIndex(index:Int):Int{
        _currentContentScaleIndex.intValue = index
        return CONTENT_SCALES[_currentContentScaleIndex.intValue].second
    }

    fun showControls(showControl:Boolean){
        _showControls.value = showControl
    }

    fun play(){
        if(!mediaStreamPlayer.isPlaying()){
            mediaStreamPlayer.resume()
        }
    }
    fun pause(){
        if(mediaStreamPlayer.isPlaying()){
            mediaStreamPlayer.pause()
        }
    }
    fun nextPlay(){
        if(mediaStreamPlayer.isNextItem())mediaStreamPlayer.nextPlay()
    }
    fun pervPlay(){
        if(mediaStreamPlayer.isPrevItem()) mediaStreamPlayer.pervPlay()
    }

    fun seekForward(){
        mediaStreamPlayer.seekForward()
    }

    fun seekBack(){
        mediaStreamPlayer.seekBack()
    }

    fun setPlaySpeedMode(speed: PlaySpeed) {
        _playSpeed.value = speed
        mediaStreamPlayer.setPlaySpeed(speed)
    }

    fun setRepeatMode(repeat: RepeatMode) {
        _repeatMode.value = repeat
        mediaStreamPlayer.setRepeat(repeat)
    }

    fun setShuffle(isShuffle: Boolean) {
        _isShuffle.value = isShuffle
        mediaStreamPlayer.setShuffleOn(isShuffle)
    }

    fun setPlaying(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun setDuration(){

        viewModelScope.launch {
            mediaStreamPlayer.duration.collect {
                it?.let{
                    _durationMs.value = it
                }
            }
        }
    }

    fun setCurrentTime(){

        viewModelScope.launch {
            mediaStreamPlayer.currentPosition.collect {
                it?.let{
                    _currentTimeMs.value = it
                }
            }
        }
    }

    fun seekTo(seekMs:Long){
        mediaStreamPlayer.seekTo(seekMs)
    }

    @OptIn(UnstableApi::class)
    fun mediaItem(url: String, cacheKey: String): MediaItem =
        MediaItem.Builder()
            .setUri(url)
            .setCustomCacheKey(cacheKey)
            .build()

    fun getRepeatToggleSequence(): RepeatMode{
        val currRepeatModeIndex = toggleModeSequence.indexOf(mediaStreamPlayer.repeatMode.value)
        return toggleModeSequence[(currRepeatModeIndex + 1) % toggleModeSequence.size]

    }
}