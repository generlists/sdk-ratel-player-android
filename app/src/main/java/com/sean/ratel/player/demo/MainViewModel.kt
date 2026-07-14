package com.sean.ratel.player.demo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.ratel.player.demo.data.repository.YouTubeRepository
import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModel
import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModelList
import com.sean.ratel.player.demo.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        val navigator: Navigator,
        val youTubeRepository: YouTubeRepository,
    ) : ViewModel() {
        private val _youtubeModel = MutableStateFlow<YouTubeModel?>(null)
        val youtubeModel: StateFlow<YouTubeModel?> = _youtubeModel

        private val _youtubeModelList = MutableStateFlow<YouTubeModelList?>(null)
        val youtubeModelList: StateFlow<YouTubeModelList?> = _youtubeModelList

        private val _youtubeGoogleCastModelList = MutableStateFlow<YouTubeModelList?>(null)
        val youtubeGoogleCastModelList: StateFlow<YouTubeModelList?> = _youtubeGoogleCastModelList

        private val _playState = MutableStateFlow<String>("UnKnown")
        val playState: StateFlow<String> = _playState

        private val _currentTime = MutableStateFlow<Float>(0f)
        val currentTime: StateFlow<Float> = _currentTime

        private val _duration = MutableStateFlow<Float>(0f)
        val duration: StateFlow<Float> = _duration

        private val _mutePlay = mutableStateOf<Boolean>(true)
        val mutePlay: MutableState<Boolean> = _mutePlay

        private val _appOpenComplete = MutableStateFlow<Boolean>(false)
        val appOpenComplete: StateFlow<Boolean> = _appOpenComplete

        init {
            viewModelScope.launch {
                loadBasicJsonData()
                loadAdvanceJsonData()
                loadGoogleCastJsonData()
            }
        }

        suspend fun loadBasicJsonData() {
            val model = youTubeRepository.getLocalContent(R.raw.youtube_basic_sample)
            model.collect {
                _youtubeModel.value = it
            }
        }

        suspend fun loadAdvanceJsonData() {
            val model = youTubeRepository.getLocalContentList(R.raw.youtube_advance_sample)
            model.collect {
                _youtubeModelList.value = it
            }
        }

        suspend fun loadGoogleCastJsonData() {
            val model = youTubeRepository.getLocalContentList(R.raw.youtube_google_cast_sample)
            model.collect {
                _youtubeGoogleCastModelList.value = it
            }
        }

        fun runNavigationBack(
            route: String? = null,
            recreate: Boolean = false,
        ) {
            navigator.navigateBack(recreate)
        }

        fun setPlayBackState(state: String) {
            _playState.value = state
        }

        fun setPlayCurrentTime(time: Float) {
            _currentTime.value = time
        }

        fun setPlayDuration(time: Float) {
            _duration.value = time
        }

        fun setMutePlay(mute: Boolean) {
            _mutePlay.value = mute
        }
    }
