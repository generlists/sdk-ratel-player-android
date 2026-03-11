package com.sean.ratel.player.core.data.domain

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import com.sean.ratel.player.core.R
import com.sean.ratel.player.core.data.domain.model.InfoType
import com.sean.ratel.player.core.data.domain.model.PreviewInfoData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InfoManager @Inject constructor(
    @ApplicationContext val context: Context
) {

    fun buildPreviewData(
        bitmap: Bitmap,
        currentPos: Long,
        playbackSpeed: Float,
        videoFileName: String, // 원본 영상 이름
        memo: String = ""
    ): PreviewInfoData {

        val mainInfoList = mutableListOf<Pair<String, String>>()
        // 1. 파일명
        val timestamp = System.currentTimeMillis()
        mainInfoList.add(context.getString(R.string.info_file_name) to "scap_${timestamp}.jpg")
        // 2. 저장 경로 (미리 보여주기용)
        mainInfoList.add(context.getString(R.string.info_save_path) to "Pictures/scap_pro/screen_shot")
        // 3. 해상도
        mainInfoList.add(context.getString(R.string.info_screen_size) to "${bitmap.width} x ${bitmap.height}")
        // 4 생성 일시
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        mainInfoList.add(context.getString(R.string.info_create_date) to date)

        val subInfoList = mutableListOf<Pair<String, String>>()
        // 1.  원본 영상 이름
        subInfoList.add(context.getString(R.string.info_original_file_name) to videoFileName)
        // 2. 영상 타임라인 (00:00)
        subInfoList.add(context.getString(R.string.info_capture_time) to formatTime(currentPos))

        // 6. 예상 용량 (압축 전 비트맵 기준 대략 계산하거나 고정)
        val estimatedSize = "${(bitmap.byteCount / (1024 * 1024))} MB"
        subInfoList.add(context.getString(R.string.info_file_size) to estimatedSize)

        // 8. 기기 모델
        subInfoList.add(context.getString(R.string.info_device_model) to Build.MODEL)

        // 9. 배속 정보
        subInfoList.add(context.getString(R.string.info_speed_info) to "${playbackSpeed}x")

        // 10. 메모 (내용이 있을 때만 추가)
        if (memo.isNotEmpty()) {
            subInfoList.add(context.getString(R.string.info_memo) to memo)
        }

        return PreviewInfoData(
            infoType = InfoType.ScreenShot,
            bitmap = bitmap,
            title = context.getString(R.string.info_screen_shot_detail_title),
            mainInfoList = mainInfoList,
            subInfoList = subInfoList,
        )
    }
    fun buildShareData(
        bitmap: Bitmap,
        currentPos: Long,
        playbackSpeed: Float
    ): PreviewInfoData {

        val mainInfoList = mutableListOf<Pair<String, String>>()

        mainInfoList.add(context.getString(R.string.info_screen_size) to "${bitmap.width} x ${bitmap.height}")

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        mainInfoList.add(context.getString(R.string.info_create_date) to date)

        mainInfoList.add(context.getString(R.string.info_capture_time) to formatTime(currentPos))


        mainInfoList.add(context.getString(R.string.info_speed_info) to "${playbackSpeed}x")

        return PreviewInfoData(
            infoType = InfoType.Share,
            bitmap = bitmap,
            title = context.getString(R.string.info_screen_shot_share),
            mainInfoList = mainInfoList,
            confirmText = "공유"
        )

    }

    private fun formatTime(ms: Long): String {
        val s = (ms / 1000) % 60
        val m = (ms / (1000 * 60)) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }
}