
package com.sean.ratel.player.core.com.sean.ratel.player.core.ui.control

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.ui.R
import com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun NextButton(viewModel: PlayerViewModel) {
    Icon(
        Icons.Default.SkipNext,
        contentDescription = stringResource(R.string.next_button),
        modifier = Modifier
            .width(48.dp)
            .height(48.dp)
            .clickable {
                viewModel.nextPlay()
            },
        tint = Color.White)
}
