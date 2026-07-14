package com.sean.ratel.player.demo.ui.view.component

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackRate
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.cast.CastEvent
import com.sean.ratel.player.demo.ui.cast.CastSessionState
import com.sean.ratel.player.demo.ui.cast.YouTubePlayersManager

private val LimeGreen = Color(0xFFABFF43)
private val BarBg = Color(0xFF121212)
private val BarBorder = Color(0xFF2A2A2A)
private val IconOff = Color(0xFF888888)
private val ProgressBg = Color(0xFF2A2A2A)
private val PlaybackSpeeds =
    listOf(
        0.25f,
        0.5f,
        0.75f,
        1.0f,
        1.25f,
        1.5f,
        1.75f,
        2.0f,
    )

@Composable
@Suppress("ktlint:standard:function-naming")
fun CastControlBar(
    playerManager: YouTubePlayersManager,
    title: String,
    thumbNail: String,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSubtitle: () -> Unit,
    onSpeedUp: () -> Unit,
    onSpeedDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val castSessionState by playerManager.castEventManager.castSession.collectAsStateWithLifecycle(
        initialValue = CastSessionState.SessionUnKnown,
    )
    val currentTime: Float? by playerManager.currentTime.collectAsStateWithLifecycle()
    val duration: Float? by playerManager.duration.collectAsStateWithLifecycle()
    val playState by playerManager.playState.collectAsStateWithLifecycle()
    val isCaptionOnOff by playerManager.captionOnOff.collectAsStateWithLifecycle()
    val mute by playerManager.mute.collectAsStateWithLifecycle()
    val playbackSpeed by playerManager.currentPlaySpeed.collectAsStateWithLifecycle()
    var deviceName by remember { mutableStateOf<String?>(null) }
    val playButtonEvent by playerManager.castEventManager.playEvents.collectAsStateWithLifecycle(
        initialValue = CastEvent.Idle,
    )

    val floatDuration = duration ?: 0f
    val floatCurrentTime = currentTime ?: 0f
    var progress by remember { mutableFloatStateOf(0.0f) }

    val progressRate =
        if (floatDuration == 0f) {
            0f
        } else {
            (floatCurrentTime / floatDuration).coerceIn(0f, 1f)
        }

    LaunchedEffect(progressRate) {
        progress = progressRate
    }

    LaunchedEffect(Unit) {
        // initCaption =   playerManager.isCaptionAvailable()
    }
    Log.d("", "playButtonEvent : $playButtonEvent")
    deviceName =
        when {
            castSessionState is CastSessionState.SessionStart -> (castSessionState as CastSessionState.SessionStart).castDevice
            else -> ""
        }

    AnimatedVisibility(
        visible = castSessionState is CastSessionState.SessionStart,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(BarBg),
            ) {
                // 썸네일 + 제목 + Cast 상태
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 썸네일
                    AsyncImage(
                        model = thumbNail,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2A2A2A)),
                    )

                    // 제목 + Cast 기기명
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            // Cast 아이콘
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cast_connected_24dp),
                                contentDescription = null,
                                tint = LimeGreen,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = "$deviceName 으로 접속" ?: "알수 없음",
                                color = LimeGreen,
                                fontSize = 10.sp,
                            )
                        }
                    }
                }

                // 컨트롤 버튼 행
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // 이전
                    CastIconButton(
                        iconRes = R.drawable.ic_skip_previous,
                        contentDescription = "이전",
                        tint = if (playerManager.isFirst()) IconOff else LimeGreen,
                        onClick = onPrevious,
                    )
                    // 재생/정지
                    CastIconButton(
                        iconRes =
                            if (playButtonEvent is CastEvent.Pause) {
                                R.drawable.ayp_ic_play_36dp
                            } else {
                                R.drawable.ayp_ic_pause_36dp
                            },
                        contentDescription = if (playButtonEvent is CastEvent.Pause) "일시정지" else "재생",
                        tint = LimeGreen,
                        size = 28.dp,
                        onClick = onPlayPause,
                    )
                    // 다음
                    CastIconButton(
                        iconRes = R.drawable.ic_skip_next,
                        contentDescription = "다음",
                        tint = if (playerManager.isLast()) IconOff else LimeGreen,
                        onClick = onNext,
                    )

                    // 구분선
                    Spacer(
                        modifier =
                            Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(BarBorder),
                    )

                    // 자막
                    CastIconButton(
                        iconRes = if (isCaptionOnOff) R.drawable.ic_caption_enabled else R.drawable.ic_caption_disabled,
                        contentDescription = "자막",
                        tint = LimeGreen,
                        onClick = onToggleSubtitle,
                    )
                    // 소리
                    CastIconButton(
                        iconRes = if (mute) R.drawable.ic_volume_off else R.drawable.ic_volume_on,
                        contentDescription = if (mute) "음소거" else "소리 켬",
                        tint = LimeGreen,
                        onClick = onToggleMute,
                    )

                    // 구분선
                    Spacer(
                        modifier =
                            Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(BarBorder),
                    )

                    // 배속 (▲ 현재값 ▼)
                    SpeedControl(
                        currentSpeed = playbackSpeed,
                        onSpeedUp = onSpeedUp,
                        onSpeedDown = onSpeedDown,
                    )
                }
                // 진행바
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                    color = LimeGreen,
                    trackColor = ProgressBg,
                )
            }
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
private fun CastIconButton(
    iconRes: Int,
    contentDescription: String,
    tint: Color,
    size: androidx.compose.ui.unit.Dp = 22.dp,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size),
        )
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
private fun SpeedControl(
    currentSpeed: YouTubeStreamPlaybackRate,
    onSpeedUp: () -> Unit,
    onSpeedDown: () -> Unit,
) {
    val label =
        remember(currentSpeed) {
            currentSpeed.rate.toString().removeSuffix(".0") + "x"
        }

    Log.d("SPPPPED", "label : $label")

    val currentIndex =
        remember(currentSpeed) {
            YouTubeStreamPlaybackRate.entries.indexOf(currentSpeed)
        }

    val canSpeedUp = currentIndex < PlaybackSpeeds.lastIndex
    val canSpeedDown = currentIndex > 0

    Column(
        modifier = Modifier.width(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clickable(
                        enabled = canSpeedUp,
                        onClick = onSpeedUp,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "▲",
                color = if (canSpeedUp) IconOff else IconOff.copy(alpha = 0.3f),
                fontSize = 8.sp,
            )
        }

        Text(
            text = label,
            color = LimeGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clickable(
                        enabled = canSpeedDown,
                        onClick = onSpeedDown,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "▼",
                color = if (canSpeedDown) IconOff else IconOff.copy(alpha = 0.3f),
                fontSize = 8.sp,
            )
        }
    }
}
