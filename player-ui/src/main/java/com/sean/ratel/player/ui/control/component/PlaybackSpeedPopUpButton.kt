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

import android.view.Gravity
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model.PlaySpeed
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun PlaybackSpeedPopUpButton(
  viewModel: PlayerViewModel,
) {
  val playSpeed = viewModel.playSpeed.collectAsState()
  var openDialog by remember { mutableStateOf(false) }
  Box(Modifier.width(55.dp).height(60.dp), contentAlignment = Alignment.Center){

    TextButton(onClick = { openDialog = true }, modifier = Modifier.wrapContentSize().padding(top=5.dp).align(Alignment.Center)) {
      BasicText(
        text = "%.1fx".format(playSpeed.value.speed),
        modifier = Modifier
          .width(55.dp)
          .height(60.dp).align(Alignment.Bottom),
        style = TextStyle(
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      )
    }
  }

  if (openDialog) {
    BottomDialogOfChoices(
      currentSpeed =playSpeed.value.speed,
      choices = viewModel.speedSelection.map { it.speed },
      onDismissRequest = { openDialog = false },
      onSelectChoice = {
        viewModel.setPlaySpeedMode(PlaySpeed.convertToValueToEnum(it))
      },
    )
  }
}

@Composable
private fun BottomDialogOfChoices(
  currentSpeed: Float,
  choices: List<Float>,
  onDismissRequest: () -> Unit,
  onSelectChoice: (Float) -> Unit,
) {
  Dialog(
    onDismissRequest = onDismissRequest,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
    dialogWindowProvider?.window?.let { window ->
      window.setGravity(Gravity.BOTTOM) // Move down, by default dialogs are in the centre
      window.setDimAmount(0f) // Remove dimmed background of ongoing playback
    }

    Box(modifier = Modifier.wrapContentSize().background(Color.LightGray)) {
      Column(
        modifier = Modifier.fillMaxWidth().wrapContentWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        choices.forEach { speed ->
          TextButton(
            onClick = {
              onSelectChoice(speed)
              onDismissRequest()
            }
          ) {
            var fontWeight = FontWeight(400)
            if (speed == currentSpeed) {
              fontWeight = FontWeight(1000)
            }
            Text("%.1fx".format(speed), fontWeight = fontWeight)
          }
        }
      }
    }
  }
}
