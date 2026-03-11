package com.sean.ratel.player.core.data.domain.model

import android.graphics.Bitmap

data class PreviewInfoData(
    val infoType:InfoType = InfoType.ScreenShot,
    val bitmap: Bitmap? = null,
    val title: String = "스크린샷 미리보기",
    val mainInfoList: List<Pair<String, String>>,
    val subInfoList: List<Pair<String, String>>?= null,
    val confirmText: String = "확인",
    val cancelText: String = "취소"
)

enum class InfoType {
    ScreenShot,
    Share,
    ScrapVideoInfo,
    LocalVideoInfo
}

