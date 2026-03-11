package com.sean.ratel.player.ui.control.component

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
import com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.ui.R


@OptIn(UnstableApi::class)
@Composable
fun SeekBackButton(viewModel: PlayerViewModel) {

  val incrementMs by viewModel.backWardMs.collectAsState()

  Icon(
    painter = painterResource(seekBackIconId(incrementMs)),
    seekBackContentDescription(incrementMs),
    modifier = Modifier
      .width(35.dp)
      .height(35.dp)
      .clickable {
        viewModel.seekBack()
      },
    tint = Color.White
  )
}

private fun seekBackIconId(seekBackAmountMs: Long): Int {
  return when (seekBackAmountMs) {
    in 2500..7500 -> R.drawable.icon_skip_back_5
    in 7500..12500 -> R.drawable.icon_skip_back_10
    in 12500..20000 -> R.drawable.icon_skip_back_15
    in 20000..40000 -> R.drawable.icon_skip_back_30
    else -> R.drawable.icon_skip_back_default
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
