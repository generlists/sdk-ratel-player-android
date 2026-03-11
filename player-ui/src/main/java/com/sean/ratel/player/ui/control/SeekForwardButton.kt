package com.sean.ratel.player.ui.control

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
fun SeekForwardButton(viewModel: PlayerViewModel) {

  val incrementMs by viewModel.forWardMs.collectAsState()


  Icon(
    painter = painterResource(seekForwardIconId(incrementMs)),
    seekForwardContentDescription(incrementMs),
    modifier = Modifier
      .width(35.dp)
      .height(35.dp)
      .clickable {
        viewModel.seekForward()
      },
    tint = Color.White
  )
}

private fun seekForwardIconId(seekForwardAmountMs: Long): Int {
  return when (seekForwardAmountMs) {
    in 2500..7500 -> R.drawable.icon_skip_forward_5
    in 7500..12500 -> R.drawable.icon_skip_forward_10
    in 12500..20000 -> R.drawable.icon_skip_forward_15
    in 20000..40000 -> R.drawable.icon_skip_forward_30
    else -> R.drawable.icon_skip_forward
  }
}

@Composable
private fun seekForwardContentDescription(seekForwardAmountMs: Long): String {
  return when (seekForwardAmountMs) {
    in 2500..7500 -> pluralStringResource(R.plurals.seek_forward_by_amount_button, count = 5)
    in 7500..12500 -> pluralStringResource(R.plurals.seek_forward_by_amount_button, count = 10)
    in 12500..20000 -> pluralStringResource(R.plurals.seek_forward_by_amount_button, count = 15)
    in 20000..40000 -> pluralStringResource(R.plurals.seek_forward_by_amount_button, count = 30)
    else -> stringResource(R.string.seek_forward_button)
  }
}