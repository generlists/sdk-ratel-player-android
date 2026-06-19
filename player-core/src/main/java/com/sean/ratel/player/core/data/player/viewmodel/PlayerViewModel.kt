package com.sean.ratel.player.core.data.player.viewmodel

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.annotation.OptIn
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.core.com.sean.ratel.player.core.util.PlayerUtil
import com.sean.ratel.player.core.data.domain.InfoManager
import com.sean.ratel.player.core.data.domain.MediaStreamPlayer
import com.sean.ratel.player.core.data.domain.model.InfoType
import com.sean.ratel.player.core.data.domain.model.PlayMediaItem
import com.sean.ratel.player.core.data.domain.model.PlaySpeed
import com.sean.ratel.player.core.data.domain.model.PlaybackState
import com.sean.ratel.player.core.data.domain.model.PreviewInfoData
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.core.data.domain.model.RepeatMode
import com.sean.ratel.player.core.data.player.pip.PIPManager
import com.sean.ratel.player.core.data.player.pip.PIPState
import com.sean.ratel.player.core.data.player.pip.PipAction
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import so.smartlab.common.utils.log.RLog
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val mediaStreamPlayer: MediaStreamPlayer,
        val infoManager: InfoManager,
        val pipManager: PIPManager,
    ) : ViewModel() {
        private val _isPlaying = MutableStateFlow<Boolean>(false)
        val isPlaying: StateFlow<Boolean> = _isPlaying

        private val _isPlayEnd = MutableStateFlow<Boolean>(false)
        val isPlayEnd: StateFlow<Boolean> = _isPlayEnd

        private val _isStop = MutableStateFlow<Boolean>(false)
        val isStop: StateFlow<Boolean> = _isStop

        private val _isNextButtonEnabled = MutableStateFlow<Boolean>(true)
        val isNextButtonEnabled: StateFlow<Boolean> = _isNextButtonEnabled

        private val _isBeforeButtonEnabled = MutableStateFlow<Boolean>(false)
        val isBeforeButtonEnabled: StateFlow<Boolean> = _isBeforeButtonEnabled

        private val _isReset = MutableStateFlow<Boolean>(false)
        val isReset: StateFlow<Boolean> = _isReset

        private val _isSeek = MutableStateFlow<Boolean>(false)
        var isSeek = _isSeek

        private val _backWardMs = mediaStreamPlayer.seekBackIncrement
        val backWardMs: StateFlow<Long> = _backWardMs

        private val _forWardMs = mediaStreamPlayer.seekBackIncrement
        val forWardMs: StateFlow<Long> = _forWardMs

        private val _durationMs = MutableStateFlow<Long>(0L)
        val durationMs: StateFlow<Long> = _durationMs

        private val _currentTimeMs = MutableStateFlow<Long>(0L)
        val currentTimeMs: StateFlow<Long> = _currentTimeMs

        private val _saveTimeMs = MutableStateFlow<Long>(0L)
        val saveTimeMs: StateFlow<Long> = _saveTimeMs

        private val _saveCurrentId = MutableStateFlow<String>("")
        val saveCurrentId: StateFlow<String> = _saveCurrentId

        private var _audioOnly = MutableStateFlow<Boolean>(false)
        val audioOnly = _audioOnly

        private val _videoQualityChanged =
            MutableStateFlow<Pair<Quality, PlayMediaItem>>(
                Pair(
                    Quality.SD,
                    PlayMediaItem("", "", ""),
                ),
            )
        val videoQualityChanged: StateFlow<Pair<Quality, PlayMediaItem>> = _videoQualityChanged

        private val toggleModeSequence: List<RepeatMode> =
            listOf(RepeatMode.REPEAT_OFF, RepeatMode.REPEAT_ONE, RepeatMode.REPEAT_ALL)

        val speedSelection: List<PlaySpeed> = PlaySpeed.entries.map { it }

        private val _optionQualityChanged = mutableStateOf<Boolean>(false)
        var optionQualityChanged = _optionQualityChanged

        private val _isSystemVolumeMute = MutableStateFlow<Boolean>(false)
        var isSystemVolumeMute = _isSystemVolumeMute

        private val _qualityList = MutableStateFlow<List<Pair<Quality, PlayMediaItem>>>(listOf())
        val qualityList: StateFlow<List<Pair<Quality, PlayMediaItem>>> = _qualityList

        private val _mediaList =
            MutableStateFlow<List<Pair<String, List<Pair<Quality, PlayMediaItem>>>>>(listOf())
        val mediaList: StateFlow<List<Pair<String, List<Pair<Quality, PlayMediaItem>>>>> = _mediaList

        private val _isHWYAccelerated = MutableStateFlow<Boolean>(true)
        val isHWYAccelerated: StateFlow<Boolean> = _isHWYAccelerated

        private val _surfaceView = mutableStateOf<View?>(null)
        val surfaceView: MutableState<View?> = _surfaceView

        private val _currentItemIndex = MutableStateFlow<Int>(0)
        val currentItemIndex: StateFlow<Int> = _currentItemIndex

        init {

            viewModelScope.launch {
                combine(
                    pipManager.pipAction,
                    pipManager.screenRect,
                    pipManager.videoSize,
                    pipManager.pipClick,
                ) { pipAction, rect, size, pipTarget ->
                    PIPState(pipAction, rect, size, pipTarget)
                }.collect { data ->

                    if (data.pipTarget.isPipClick) {
                        RLog.d(
                            "PIP_CLICK",
                            "isFirst : ${isFirst()} " +
                                "isLast : ${isLast()}, " +
                                "pipAction : ${data.pipAction} , " +
                                "isPipClick : ${data.pipTarget.isPipClick}, " +
                                "size=${data.screenSize} , " +
                                "isPIP=${data.pipTarget.isPipClick}",
                        )

                        when (data.pipAction) {
                            PipAction.PLAY -> play()
                            PipAction.PAUSE -> pause()
                            PipAction.SKIP_PREVIOUS -> pervPlay()
                            PipAction.SKIP_NEXT -> nextPlay()
                            PipAction.REPLAY -> rePlay()
                            else -> Unit
                        }

                        pipManager.updatePipParams(
                            isPlaying = mediaStreamPlayer.isPlaying(),
                            isFirst = isFirst(),
                            isLast = isLast(),
                            videoSize = data.screenSize,
                            rect = data.screenRect,
                        )
                    }
                }
            }

            viewModelScope.launch {
                mediaStreamPlayer.playbackState.collect { state ->
                    val size = _mediaList.value.size

                    when (state) {
                        is PlaybackState.Playing, is PlaybackState.Pause -> {
                            RLog.d(
                                "PIP_CLICK",
                                "Playing size = $size , index = ${currentItemIndex.value}isFirst : ${isFirst()} ,  isLast : ${isLast()}",
                            )
                            pipManager.updatePipParams(
                                isPlaying = mediaStreamPlayer.isPlaying(),
                                videoSize = pipManager.videoSize.value,
                                rect = pipManager.screenRect.value,
                                isFirst = isFirst(),
                                isLast = isLast(),
                            )
                        }

                        is PlaybackState.Complete -> {
                            RLog.d("PIP_CLICK", "Complete")
                            pipManager.updatePipParams(
                                isPlaying = false,
                                videoSize = pipManager.videoSize.value,
                                rect = pipManager.screenRect.value,
                                endPlay = true,
                                isFirst = isFirst(),
                                isLast = isLast(),
                            )
                        }

                        else -> {
                            Unit
                        }
                    }
                }
            }
        }

        fun isFirst() = _currentItemIndex.value == 0

        fun isLast() = mediaList.value.isNotEmpty() && mediaList.value.size - 1 == _currentItemIndex.value

        fun setCurrentIndex(currentIndex: Int) {
            _currentItemIndex.value = currentIndex
        }

        fun setSaveTimeMs(saveTimeMs: Long) {
            _saveTimeMs.value = saveTimeMs
        }

        fun setSaveCurrentId(currentId: String) {
            _saveCurrentId.value = currentId
        }

        fun audioOnly(audioOnly: Boolean) {
            _audioOnly.value = audioOnly
        }

        fun setSurfaceView(view: View?) {
            _surfaceView.value = view
        }

        fun setReset(reset: Boolean) {
            _isReset.value = reset
        }

        fun getScreenCapture(
            onInfo: (PreviewInfoData?) -> Unit,
            speed: PlaySpeed,
        ) {
            _surfaceView.value?.let {
                mediaStreamPlayer.getVideoCapture(it, infoCallback = { bitmap ->

                    bitmap?.let {
                        onInfo(
                            infoManager.buildPreviewData(
                                bitmap = bitmap,
                                currentPos = mediaStreamPlayer.currentPosition.value ?: 0L,
                                playbackSpeed = speed.speed,
                                videoFileName = "fileName",
                            ),
                        )
                    } ?: run {
                        onInfo(
                            PreviewInfoData(
                                infoType = InfoType.ScreenShot,
                                id = "",
                                mainInfoList = listOf(),
                            ),
                        )
                    }
                })
            }
        }

        fun getShareScreenCapture(
            onInfo: (PreviewInfoData?) -> Unit,
            speed: PlaySpeed,
        ) {
            _surfaceView.value?.let {
                mediaStreamPlayer.getVideoCapture(it, infoCallback = { bitmap ->

                    bitmap?.let {
                        onInfo(
                            infoManager.buildShareData(
                                bitmap = bitmap,
                                currentPos = mediaStreamPlayer.currentPosition.value ?: 0L,
                                playbackSpeed = speed.speed,
                            ),
                        )
                    } ?: run {
                        onInfo(
                            PreviewInfoData(
                                infoType = InfoType.ScreenShot,
                                id = "",
                                mainInfoList = listOf(),
                            ),
                        )
                    }
                })
            }
        }

        fun saveCaptureFile(
            context: Context,
            infoType: InfoType,
            bitmap: Bitmap,
            saveComplete: ((File?, Uri?) -> Unit)? = null,
        ) {
            // 2. 버전별 저장 분기 (이게 핵심!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 안드로이드 10 이상: MediaStore 방식 (권한 필요 없음)
                saveWithMediaStore(context, bitmap, infoType, saveComplete)
            } else {
                // 안드로이드 8, 9: 직접 파일 경로 방식 (WRITE_EXTERNAL_STORAGE 권한 필수)
                saveFileBitmap(context, infoType, bitmap, saveComplete)
            }
        }

        private fun saveFileBitmap(
            context: Context,
            infoType: InfoType,
            bitmap: Bitmap,
            saveComplete: ((File?, Uri?) -> Unit)? = null,
        ) {
            val filename = "video_scrap_pro_${System.currentTimeMillis()}.jpg"

            // Pictures/scap_pro/screen_shot 경로
            val path =
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "scap_pro/screen_shot",
                )

            if (!path.exists()) path.mkdirs()

            val file = File(path, filename)
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                // 갤러리 새로고침
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
                if (infoType == InfoType.Share) {
                    saveComplete?.let {
                        saveComplete(file, null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun saveWithMediaStore(
            context: Context,
            bitmap: Bitmap,
            infoType: InfoType,
            saveComplete: ((File?, Uri?) -> Unit)? = null,
        ) {
            val filename = "screenshot_${System.currentTimeMillis()}.jpg"
            val contentValues =
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/scap_pro/screen_shot")
                }

            val uri =
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues,
                )

            uri?.let { targetUri ->
                context.contentResolver.openOutputStream(targetUri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    RLog.d("Player", "최신 방식으로 MediaStore 저장 성공!")
                }
                if (infoType == InfoType.Share) {
                    saveComplete?.let {
                        saveComplete(null, uri)
                    }
                }
            }
        }

        fun setVideoQualityChanged(
            qualityChanged: Boolean,
            videoQuality: Pair<Quality, PlayMediaItem>,
        ) {
            _videoQualityChanged.value = videoQuality
            _optionQualityChanged.value = qualityChanged
        }

        fun setVideoQuality(videoQuality: Pair<Quality, PlayMediaItem>) {
            _videoQualityChanged.value = videoQuality
        }

        fun setOptionQualityChanged(qualityChanged: Boolean) {
            _optionQualityChanged.value = qualityChanged
        }

        fun setVolume(volume: Float) {
            mediaStreamPlayer.setVolume(volume)
        }

        fun setQuality(quality: List<Pair<Quality, PlayMediaItem>>) {
            _qualityList.value = quality
        }

        fun setHWAccelerated(isHWYAccelerated: Boolean) {
            _isHWYAccelerated.value = isHWYAccelerated
        }

        fun play() {
            if (!mediaStreamPlayer.isPlaying()) {
                mediaStreamPlayer.resume()
            }
        }

        fun pause() {
            if (mediaStreamPlayer.isPlaying()) {
                mediaStreamPlayer.pause()
            }
        }

        fun setIsStop(isStop: Boolean) {
            _isStop.value = isStop
        }

        fun setPlayAllEnd(isPlayEnd: Boolean) {
            _isPlayEnd.value = isPlayEnd
        }

        fun nextButtonEnabled(isEnabled: Boolean) {
            _isNextButtonEnabled.value = isEnabled
        }

        fun beforeButtonEnabled(isEnabled: Boolean) {
            _isBeforeButtonEnabled.value = isEnabled
        }

        fun rePlay(mediaIndex: Int = 0) {
            mediaStreamPlayer.rePlay(mediaIndex)
        }

        fun isSeek(isSeek: Boolean) {
            _isSeek.value = isSeek
        }

        fun nextPlay() {
            if (mediaStreamPlayer.isNextItem()) {
                RLog.d("MediaScreen", "_isReset.value : ${_isReset.value}")
                // 퀄러티 인덱스 초기화
                mediaStreamPlayer.nextPlay(isReset = _isReset.value)

                if (!_isPlaying.value) {
                    mediaStreamPlayer.resume()
                    // mediaStreamPlayer.pause()
                }
                val nextIndex =
                    if (currentItemIndex.value < _mediaList.value.size) currentItemIndex.value + 1 else currentItemIndex.value
                setVideoQualityChanged(
                    qualityChanged = false,
                    videoQuality = _mediaList.value[nextIndex].second[0],
                )
            }
        }

        fun pervPlay() {
            if (mediaStreamPlayer.isPrevItem()) {
                mediaStreamPlayer.pervPlay(_isReset.value)
                if (!_isPlaying.value) {
                    mediaStreamPlayer.resume()
                    // mediaStreamPlayer.pause()
                }
                val pervIndex =
                    if (currentItemIndex.value >= 0) currentItemIndex.value - 1 else currentItemIndex.value
                // 퀄러티 인덱스 초기화
                setVideoQualityChanged(
                    qualityChanged = false,
                    videoQuality = _mediaList.value[pervIndex].second[0],
                )
            }
        }

        fun seekForward() {
            mediaStreamPlayer.seekForward()
        }

        fun seekBack() {
            mediaStreamPlayer.seekBack()
        }

        fun setPlaySpeedMode(speed: PlaySpeed) {
            mediaStreamPlayer.setPlaySpeed(speed)
        }

        fun setRepeatMode(repeat: RepeatMode) {
            mediaStreamPlayer.setRepeat(repeat)
        }

        fun setShuffle(isShuffle: Boolean) {
            // _isShuffle.value = isShuffle
            mediaStreamPlayer.setShuffleOn(isShuffle)
        }

        fun setPlaying(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        fun setDuration() {
            viewModelScope.launch {
                mediaStreamPlayer.duration.collect {
                    it?.let {
                        _durationMs.value = it
                    }
                }
            }
        }

        fun setCurrentTime() {
            viewModelScope.launch {
                mediaStreamPlayer.currentPosition.collect {
                    it?.let {
                        _currentTimeMs.value = it
                        setSystemVolumeMute()
                    }
                }
            }
        }

        fun seekTo(seekMs: Long) {
            mediaStreamPlayer.seekTo(seekMs)
        }

        fun seekTo(
            mediaIndex: Int,
            seekMs: Long,
        ) {
            mediaStreamPlayer.seekTo(mediaIndex, seekMs)
        }

        @OptIn(UnstableApi::class)
        private fun mediaItem(
            url: String,
            cacheKey: String,
        ): MediaItem =
            MediaItem
                .Builder()
                .setUri(url)
                .setCustomCacheKey(cacheKey)
                .build()

        @OptIn(UnstableApi::class)
        fun buildMediaItem(
            playMediaItem: Pair<Quality, PlayMediaItem>,
            isConnectError: Boolean = false,
        ): MediaItem {
            RLog.d(
                "PlayerView",
                "videoUrl : ${playMediaItem.second.mediaKey} ,  cacheKey : ${playMediaItem.second.mediaUrl}",
            )
            val playItem = playMediaItem.second

            val uri = if (!isConnectError) playItem.mediaUrl.toUri() else playItem.filePath.toUri()

            when (uri.scheme) {
                ContentResolver.SCHEME_CONTENT -> {
                    return MediaItem.fromUri(uri)
                }

                ContentResolver.SCHEME_FILE -> {
                    return MediaItem.fromUri(uri)
                }

                else -> {
                    when {
                        playItem.mediaUrl.contains(".m3u8", ignoreCase = true) -> {
                            return MediaItem
                                .Builder()
                                .setUri(uri)
                                .setCustomCacheKey(playItem.mediaKey)
                                .setMimeType(MimeTypes.APPLICATION_M3U8)
                                .build()
                        }

                        else -> {
                            return MediaItem
                                .Builder()
                                .setUri(uri)
                                .setCustomCacheKey(playItem.mediaKey)
                                .build()
                        }
                    }
                }
            }
        }

        fun changeItemUrl(
            index: Int,
            newUrl: String,
        ) {
            // 1. 새 URL로 미디어 아이템 생성
            val newMediaItem = MediaItem.fromUri(newUrl)

            // 2. 0번 인덱스(첫 번째)의 아이템을 새로운 놈으로 교체!
            // false는 "기존 재생 상태를 유지하겠다"는 뜻이야.
            mediaStreamPlayer.replaceMediaItem(index, newMediaItem)
        }

        fun getRepeatToggleSequence(): RepeatMode {
            val currRepeatModeIndex = toggleModeSequence.indexOf(mediaStreamPlayer.repeatMode.value)
            return toggleModeSequence[(currRepeatModeIndex + 1) % toggleModeSequence.size]
        }

        fun setMediaList(media: List<Pair<String, List<Pair<Quality, PlayMediaItem>>>>) {
            _mediaList.value = media.filter { it.second.isNotEmpty() }
        }

        fun reorderMediaList(
            list: List<Pair<String, List<Pair<Quality, PlayMediaItem>>>>,
            saveId: String,
        ): List<Pair<String, List<Pair<Quality, PlayMediaItem>>>> {
            val savedIndex = list.indexOfFirst { it.first == saveId }

            // 1. startIndex부터 끝까지 (예: [1, 2])
            val head = list.subList(savedIndex, list.size)

            // 2. 처음부터 startIndex 전까지 (예: [0])
            val tail = list.subList(0, savedIndex)

            // 3. 둘이 합치기! ([1, 2] + [0])
            return head + tail
        }

        fun setSystemVolumeMute() {
            _isSystemVolumeMute.value = PlayerUtil.isSystemVolumeMute(context)
        }
    }
