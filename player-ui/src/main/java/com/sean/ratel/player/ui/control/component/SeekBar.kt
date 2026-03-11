package com.sean.ratel.player.ui.control.component

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Suppress("ktlint:standard:function-naming")
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CustomSeekBar(
    modifier: Modifier = Modifier,
    progress: Float, // 0f ~ 1f
    onSeekPreview: (Float) -> Unit,   // 드래그 중
    onSeekCommit: (Float) -> Unit,    // 손 놓았을 때
    barHeight: Dp = 4.dp,
    thumbRadius: Dp = 8.dp,
    backgroundColor: Color = Color.DarkGray,
    progressColor: Color = Color.White
) {

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
       )
    {

        val widthPx = constraints.maxWidth.toFloat()
        val density = LocalDensity.current
        val thumbPx = with(density) { thumbRadius.toPx() }

        fun progressFromX(x: Float): Float {
            val usable = (widthPx - thumbPx * 2f).coerceAtLeast(1f)
            return ((x - thumbPx) / usable).coerceIn(0f, 1f)
        }

        // 터치 처리 레이어
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(widthPx) {
                    detectTapGestures { offset ->
                        val p = progressFromX(offset.x)
                        onSeekPreview(p)
                        onSeekCommit(p) // 탭은 즉시 이동
                    }
                }
                .pointerInput(widthPx) {
                    var lastX = 0f

                    detectDragGestures(


                        onDrag = { change, _ ->
                            change.consume()

                            lastX = change.position.x
                            onSeekPreview(progressFromX(lastX))
                        },

                        onDragEnd = {
                            onSeekCommit(progressFromX(lastX))
                        }
                    )
                }
        ) {
            /* -------------------------
             * Track (배경 + 진행)
             * ------------------------- */
            Canvas(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
            ) {
                val barHeightPx = barHeight.toPx()
                val barTop = center.y - barHeightPx / 2f

                // 배경 바
                drawRoundRect(
                    color = backgroundColor,
                    topLeft = Offset(0f, barTop),
                    size = Size(size.width, barHeightPx),
                    cornerRadius = CornerRadius(barHeightPx / 2f)
                )

                // 진행 바
                drawRoundRect(
                    color = progressColor,
                    topLeft = Offset(0f, barTop),
                    size = Size(
                        size.width * progress.coerceIn(0f, 1f),
                        barHeightPx
                    ),
                    cornerRadius = CornerRadius(barHeightPx / 2f)
                )
            }

            /* -------------------------
             * Thumb
             * ------------------------- */
            Canvas(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset {
                        IntOffset(
                            x = (
                                    progress.coerceIn(0f, 1f) *
                                            (widthPx - thumbPx * 2f)
                                    ).toInt(),
                            y = 0
                        )
                    }
                    .size(thumbRadius * 2)
            ) {
                drawCircle(
                    color = progressColor,
                    radius = size.minDimension / 2f,
                    center = center
                )
            }
        }
    }
}