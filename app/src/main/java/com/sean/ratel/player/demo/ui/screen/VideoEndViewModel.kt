package com.sean.ratel.player.demo.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.ratel.player.core.data.domain.model.InfoType
import com.sean.ratel.player.core.data.domain.model.PreviewInfoData
import com.sean.ratel.player.ui.MediaOptions
import com.sean.ratel.player.ui.control.component.options.MediaOptionKey
import com.sean.ratel.player.ui.control.component.options.MediaOptionValue
import com.sean.ratel.player.ui.control.component.share.ImageShareManager
import com.sean.ratel.player.utils.log.RLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import so.smartlab.video.player.ad.admob.AdsSdk
import so.smartlab.video.player.ad.admob.data.model.AdMobInterstitialAdState
import javax.inject.Inject

@HiltViewModel
class VideoEndViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val imageShareManager: ImageShareManager,
    val adsSdk: AdsSdk): ViewModel() {


    val _mediaOptions = MutableStateFlow(MediaOptions())
    val mediaOptions: StateFlow<MediaOptions> = _mediaOptions

    fun setUpdateMediaOption(key: MediaOptionKey, value:  MediaOptionValue){


        when {
            key == MediaOptionKey.PLAYBACK_REPEAT && value is MediaOptionValue.Repeat ->{
                _mediaOptions.update { currentOptions ->
                    currentOptions.copy(repeatMode = value.value)
                    //preference 저장
                    //setRepeat(value.value)//
                }
            }


            key == MediaOptionKey.SCREEN_VOLUME && value is MediaOptionValue.FloatValue -> {

                _mediaOptions.update { currentOptions ->
                    currentOptions.copy(volume = value)
                    //preference 저장
                    //setVolume(value.value)//
                }
            }

            key == MediaOptionKey.SCREEN_SCALE && value is MediaOptionValue.Scale -> {
                _mediaOptions.update { currentOptions ->
                    currentOptions.copy(contentScale = value.value)
                    //preference 저장
                    //setScale(value.value)
                }
            }

            key == MediaOptionKey.SCREEN_BRIGHT && value is MediaOptionValue.FloatValue -> {
                _mediaOptions.update { currentOptions ->
                    currentOptions.copy(screenBrightness = value)
                    //preference 저장
                    //setScreenBrightness(value.value)
                }

            }

            key == MediaOptionKey.PLAYBACK_SPEED && value is MediaOptionValue.Speed -> {
                _mediaOptions.update { currentOptions ->
                    currentOptions.copy(speed = value.value)
                    //preference 저장
                    //setSpeed(value.value)
                }

            }

            key == MediaOptionKey.SHUFFLE && value is MediaOptionValue.Toggle -> {

                _mediaOptions.update { currentOptions ->
                    currentOptions.copy(shuffle = value)
                    //preference 저장
                    //setShuffle(value.value)
                }
            }
        }


        RLog.d("optionChanged", "after optionChanged ${_mediaOptions.value}")
    }

    fun initInterstitialAd(activity: Context) {
        viewModelScope.launch {

            adsSdk.initInterstitialAd(
                adsSdk.config.interstitialUnitId)
        }
        interstitialAdEvent()
    }


   private  fun  interstitialAdEvent(){
        viewModelScope.launch {
            adsSdk.interstitialState.collect { event->

                if(event is AdMobInterstitialAdState.AdLoadComplete){
                    adsSdk.showInterstitialAds()
                }



            }
        }

    }

    fun getDummyData():PreviewInfoData{

//        val mainInfoList = listOf<Pair<String,String>>(
//            "스크랩 앱" to "FACEBOOK",
//            "스크랩 일자" to "2026.02.21",
//            "조회수" to "1345k",
//            "작성자" to "hbungshin",
//            "설명" to "동해물과 백두산이 마르고 닳토록 하느님이 보호하사 우리나라만세 무궁화삼천리 화려강산 대한사람 대한으로 길이 보전하세 남산위의 저소나무"
//        )
//
//
//        val subInfoList = listOf<Pair<String,String>>(
//
//            "재생 시간" to "00:11",
//            "포스트 url" to "https://www.facebook.com/share/r/123456",
//            "저장 경로" to "/storage/emulator/0/Download/scrap_pro",
//            "SD 파일명(크기)" to "FACEBOO_SD_1234543444444.mp4(20MB)",
//            "HD 파일명(크기)" to "FACEBOO_HD_1234543444444.mp4(3G)",
//            "AUDIO 파일명(크기)" to "FACEBOO_AUDIO_1234543444444.mp4(1.7MB)",
//        )
//
//        return PreviewInfoData(
//            infoType = InfoType.ScrapVideoInfo,
//            bitmap =null ,
//            title = "그날을 어찌 있을 수있겠냐마는",
//            mainInfoList =mainInfoList ,
//            subInfoList =subInfoList,
//            confirmText = "확인",
//            cancelText = "취소"
//        )
//        val mainInfoList = listOf<Pair<String, String>>(
//            "파일 이름" to localVideoItem.displayName,
//            "폴더 명" to localVideoItem.bucketName,
//            "재생 시간" to TimeUtil.formatDuration(localVideoItem.duration.toDouble()),
//            "상대 경로" to localVideoItem.relativePath,
//            "저장 위치" to localVideoItem.filePath,
//            "파일 크기 " to localVideoItem.size.toReadableSize()
//        )
//        val subInfoList = listOf<Pair<String, String>>(
//            "타입" to (localVideoItem.mimeType?:"video/mp4"),
//            "추가 시간" to localVideoItem.dateAdded.toLocalDateKst().toString(),
//            "해상도" to "${localVideoItem.width} x ${localVideoItem.height}",
//            "소유자" to badgeFromOwner(localVideoItem.ownerPackageName).name,
//            "orientation" to (mapVideoOrientation(localVideoItem.orientation?:0,localVideoItem.width?:0,localVideoItem.height?:0).name)
//        )
//        return PreviewInfoData(
//            infoType = InfoType.VideoInfo,
//            title = localVideoItem.displayName,
//            mainInfoList = mainInfoList,
//            subInfoList = subInfoList
//        )
        val mainLocalInfoList = listOf<Pair<String,String>>(
            "메인 폴더" to "카메라",
            "폴더명" to "line",
            "mime Type" to "video/mp4",
            "재생 시간" to "00:11",
            "파일 크기 " to "320MB",
            "파일명" to "그저그런.mp4",
            "상대경로" to "/DCIM",
            "저장위치"  to "/storage/emulator/0/DCIM/line/12356789.mp4",
        )
        val subLocalInfoList = listOf<Pair<String,String>>(
            "추가된 시간" to "00:11",
            "해상도" to "1090 * 1920(HD)",
            "소유자" to  "FaceBook",
            "orientation" to "PORTRAIT")

        return PreviewInfoData(
            infoType = InfoType.LocalVideoInfo,
            bitmap =null ,
            title = "영상정보이다 개새끼야 ",
            mainInfoList =mainLocalInfoList ,
            subInfoList =subLocalInfoList,
            confirmText = "확인",
            cancelText = "취소"
        )

    }

}

