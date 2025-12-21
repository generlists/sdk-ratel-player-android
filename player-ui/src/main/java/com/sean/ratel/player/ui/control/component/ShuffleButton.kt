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
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.ui.R

@OptIn(UnstableApi::class)
@Composable
fun ShuffleButton(viewModel: PlayerViewModel) {
    val isShuffle = viewModel.isShuffle.collectAsState()
  Log.d("hbungshin","isShuffle : ${isShuffle}")
  val icon = if (isShuffle.value) Icons.Default.ShuffleOn else Icons.Default.Shuffle
  val contentDescription =
    if (isShuffle.value) {
      stringResource(R.string.shuffle_button_shuffle_on)
    } else {
      stringResource(R.string.shuffle_button_shuffle_off)
    }

    IconButton(onClick =  {viewModel.setShuffle(!isShuffle.value)}) {
      Icon(icon, contentDescription = contentDescription, modifier  = Modifier
        .width(38.dp)
        .height(38.dp)
        ,
        tint = Color.White)
    }

}
