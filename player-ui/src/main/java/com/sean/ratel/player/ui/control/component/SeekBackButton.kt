/*
 * Copyright 2025 The Android Open Source Project
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

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.ui.R
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel


@OptIn(UnstableApi::class)
@Composable
fun SeekBackButton(viewModel: PlayerViewModel) {

  val incrementMs by viewModel.backWardMs.collectAsState()

  Icon(
    painter = painterResource(seekBackIconId(incrementMs)),
    seekBackContentDescription(incrementMs),
    modifier = Modifier
      .width(40.dp)
      .height(40.dp)
      .clickable {
        viewModel.seekBack()
      },
    tint = Color.White
  )
}

private fun seekBackIconId(seekBackAmountMs: Long): Int {
  return when (seekBackAmountMs) {
    in 2500..7500 -> R.drawable.media3_icon_skip_back_5
    in 7500..12500 -> R.drawable.media3_icon_skip_back_10
    in 12500..20000 -> R.drawable.media3_icon_skip_back_15
    in 20000..40000 -> R.drawable.media3_icon_skip_back_30
    else -> R.drawable.media3_icon_skip_back
  }
}

@Composable
private fun seekBackContentDescription(seekBackAmountMs: Long): String {
  return when (seekBackAmountMs) {
    in 2500..7500 -> pluralStringResource(R.plurals.seek_back_by_amount_button, count = 5)
    in 7500..12500 -> pluralStringResource(R.plurals.seek_back_by_amount_button, count = 10)
    in 12500..20000 -> pluralStringResource(R.plurals.seek_back_by_amount_button, count = 15)
    in 20000..40000 -> pluralStringResource(R.plurals.seek_back_by_amount_button, count = 30)
    else -> stringResource(R.string.seek_back_button)
  }
}
