/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sean.ratel.player.core.com.sean.ratel.player.core.ui.control

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOneOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.ui.R
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.RepeatMode
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun RepeatButton(viewModel: PlayerViewModel) {

  val repeatModeState = viewModel.repeatMode.collectAsState()
  Log.d("hbungshin", "repeatModeState : ${repeatModeState.value}")
  val icon = repeatModeIcon(repeatModeState.value)
  val contentDescription = repeatModeContentDescription(repeatModeState.value)


  IconButton(
    onClick = {
      val repeatMode = viewModel.getRepeatToggleSequence()
      viewModel.setRepeatMode(repeatMode)
    },
  ) {
    Icon(
      icon, contentDescription = contentDescription, Modifier
        .width(38.dp)
        .height(38.dp),
      tint = Color.White
    )
  }
}


private fun repeatModeIcon(repeatMode: RepeatMode): ImageVector {
  return when (repeatMode) {
    RepeatMode.REPEAT_OFF -> Icons.Default.Repeat
    RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOneOn
    else -> Icons.Default.RepeatOn
  }
}

@Composable
private fun repeatModeContentDescription(repeatMode: RepeatMode): String {
  return when (repeatMode) {
    RepeatMode.REPEAT_OFF -> stringResource(R.string.repeat_button_repeat_off)
    RepeatMode.REPEAT_ONE -> stringResource(R.string.repeat_button_repeat_one)
    else -> stringResource(R.string.repeat_button_repeat_all)
  }
}

