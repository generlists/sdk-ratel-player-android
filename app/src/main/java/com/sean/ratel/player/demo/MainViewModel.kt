package com.sean.ratel.player.demo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.ratel.player.demo.data.repository.YouTubeRepository
import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModel
import com.sean.ratel.player.demo.data.youtube.domain.YouTubeModelList
import com.sean.ratel.player.demo.ui.navigation.Destination
import com.sean.ratel.player.demo.ui.navigation.Navigator
import com.sean.ratel.player.utils.log.RLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import so.smartlab.video.player.ad.admob.AdsSdk
import so.smartlab.video.player.ad.admob.data.model.AdMobAppOpenAdState
import so.smartlab.video.player.ad.admob.data.model.AdMobBannerState
import so.smartlab.video.player.ad.admob.data.model.AdMobInitState
import so.smartlab.video.player.ad.admob.data.model.AdMobNativeAdState
import so.smartlab.video.player.ad.admob.ui.utils.ActivityEvent
import so.smartlab.video.player.ad.admob.ui.utils.AppLifecycleBus
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val navigator: Navigator,
    val youTubeRepository: YouTubeRepository,
    private val appLifecycleBus: AppLifecycleBus,
    val adsSdk: AdsSdk
) : ViewModel() {

    private val _youtubeModel = MutableStateFlow<YouTubeModel?>(null)
    val youtubeModel: StateFlow<YouTubeModel?> = _youtubeModel

    private val _youtubeModelList = MutableStateFlow<YouTubeModelList?>(null)
    val youtubeModelList: StateFlow<YouTubeModelList?> = _youtubeModelList

    private val _playState = MutableStateFlow<String>("UnKnown")
    val playState: StateFlow<String> = _playState

    private val _currentTime = MutableStateFlow<Float>(0f)
    val currentTime: StateFlow<Float> = _currentTime

    private val _duration = MutableStateFlow<Float>(0f)
    val duration: StateFlow<Float> = _duration

    private val _mutePlay = mutableStateOf<Boolean>(true)
    val mutePlay: MutableState<Boolean> = _mutePlay

    //광고
    private val _adMobinitState = MutableStateFlow<AdMobInitState>(AdMobInitState.Idle)
    val adMobinitState: StateFlow<AdMobInitState> = _adMobinitState

    private val _fixedBannerState = MutableStateFlow<AdMobBannerState>(AdMobBannerState.Idle)
    val fixedBannerState: StateFlow<AdMobBannerState> = _fixedBannerState

    private val _adaptiveInlineBannerState = MutableStateFlow<AdMobBannerState>(AdMobBannerState.Idle)
    val adaptiveInlineBannerState: StateFlow<AdMobBannerState> = _adaptiveInlineBannerState

    private val _nativeAdState = MutableStateFlow<AdMobNativeAdState>(AdMobNativeAdState.Idle)
    val nativeAdState: StateFlow<AdMobNativeAdState> = _nativeAdState


    private val _appOpenComplete = MutableStateFlow<Boolean>(false)
    val appOpenComplete: StateFlow<Boolean> = _appOpenComplete

    init {
        viewModelScope.launch {
            loadBasicJsonData()
            loadAdvanceJsonData()
        }

        viewModelScope.launch {
            appLifecycleBus.events.collect { event ->
                when (event) {
                    is ActivityEvent.Started -> {
                        adsSdk.showAppOpenAd(event.activity,initAdMob = true, goPage = {
                            navigator.navigateTo(Destination.Home.route)
                            _appOpenComplete.value = true
                        })
                    }
                    else -> Unit
                }
            }
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

    fun runNavigationBack(
        route: String? = null,
        recreate: Boolean = false,
    ) {
        navigator.navigateBack(recreate)

    }
    fun setPlayBackState(state:String){
        _playState.value = state
    }
    fun setPlayCurrentTime(time:Float){
        _currentTime.value = time
    }
    fun setPlayDuration(time:Float){
        _duration.value = time
    }
    fun setMutePlay(mute:Boolean){
        _mutePlay.value = mute
    }
    fun setInitAdMobState(state: AdMobInitState){
        _adMobinitState.value = state
    }
    fun setFixedBannerState(state: AdMobBannerState){
        _fixedBannerState.value = state
    }

    fun setAdaptiveInLineBannerState(state: AdMobBannerState){
        _adaptiveInlineBannerState.value = state
    }
    fun setANativeAdState(state: AdMobNativeAdState){
        _nativeAdState.value = state
    }

    fun initAdMobSDK(activity: MainActivity) {
        adsSdk.initAdMob(activity)
        viewModelScope.launch {
            adsSdk.adInitState.collect {
                setInitAdMobState(it)
            }
        }
    }

    fun requestBannerAdView(activity: MainActivity) {
        viewModelScope.launch {
            adsSdk.requestBannerAdView(
                activity,
                adsSdk.config.bannerUnitId
            )
        }

        viewModelScope.launch {
            adsSdk.adFixedBannerState.collect { state->
                setFixedBannerState(state)
            }
        }
    }

    fun requestInLineBannerAdView(activity: MainActivity){
        viewModelScope.launch {
            adsSdk.requestInLineBannerAdView(activity,
                adsSdk.config.adaptiveBannerUnitId)

            adsSdk.adAdaptiveBannerState.collect { state->
                setAdaptiveInLineBannerState(state)
            }
        }

    }
    fun requestNativeAd(activity: MainActivity) {
        viewModelScope.launch {

            adsSdk.requestNativeAdView(
                activity,
                adsSdk.config.nativeAdUnitId)

            adsSdk.adNativeState.collect { state->
                RLog.d("ADView", "NativeAd bannerState : ${state}")
                setANativeAdState(state)
            }
        }
    }


    fun showAppOpenAd(activity: MainActivity) {
        viewModelScope.launch {
            _adMobinitState.collect {state->
                if (state == AdMobInitState.InitComplete) {
                    RLog.d("ADView","showAppOpenAd")
                    adsSdk.appOpenLoad()
                }
            }
        }
        appOpenAdEvent(activity)
    }

    private  fun appOpenAdEvent(activity: MainActivity){
        viewModelScope.launch {
            adsSdk.appOpenState.collect { event->

                RLog.d("LOG_TAG","appOpenAdEvent : $event")
                if (event is AdMobAppOpenAdState.AdLoad && !_appOpenComplete.value) {
                    delay(5000)
                    adsSdk.showAppOpenAd(
                        activity,
                        _adMobinitState.value == AdMobInitState.InitComplete,
                        goPage = {
                            navigator.navigateTo(Destination.Home.route)
                            _appOpenComplete.value = true
                        })

                }
            }
        }

    }

    fun setAppOpenComplete(appOpenComplete:Boolean){
        _appOpenComplete.value = appOpenComplete
    }

}

