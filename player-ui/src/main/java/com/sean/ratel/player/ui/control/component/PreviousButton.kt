
package com.sean.ratel.player.ui.control.component

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.ui.R

@OptIn(UnstableApi::class)
@Composable
fun PreviousButton(viewModel: PlayerViewModel) {

    val enabled = viewModel.isBeforeButtonEnabled.collectAsState()

    Icon(
        Icons.Default.SkipPrevious,
        contentDescription = stringResource(R.string.previous_button),
        modifier = Modifier
            .width(48.dp)
            .height(48.dp)
            .clickable(enabled= enabled.value) {
                viewModel.pervPlay()
            },
        tint = if(enabled.value) Color.White else Color.LightGray)
}


