package com.sean.ratel.player.ui.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.media3.common.util.UnstableApi
import kotlin.math.roundToInt

@Composable
internal fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
  clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null, // to prevent the ripple from the tap
  ) {
    onClick()
  }

@UnstableApi
@Composable
fun Modifier.resizeWithContentScale(
  contentScale: ContentScale,
  sourceSizeDp: Size?,
  density: Density = LocalDensity.current,
): Modifier =
  then(
    Modifier.fillMaxSize()
      .wrapContentSize()
      .then(
        sourceSizeDp?.let { srcSizeDp ->
          Modifier.layout { measurable, constraints ->
            val srcSizePx =
              with(density) { Size(Dp(srcSizeDp.width).toPx(), Dp(srcSizeDp.height).toPx()) }
            val dstSizePx = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
            val scaleFactor = contentScale.computeScaleFactor(srcSizePx, dstSizePx)
            val placeable =
              measurable.measure(
                constraints.copy(
                  maxWidth = (srcSizePx.width * scaleFactor.scaleX).roundToInt(),
                  maxHeight = (srcSizePx.height * scaleFactor.scaleY).roundToInt(),
                )
              )
            layout(placeable.width, placeable.height) { placeable.place(0, 0) }
          }
        } ?: Modifier
      )
  )

