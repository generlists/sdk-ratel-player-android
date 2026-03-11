package com.sean.ratel.player.ui.control.component


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.ui.R

@Composable
fun MoreButton(viewModel: PlayerViewModel) {

    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(20.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Icon(
            Icons.Default.MoreVert,
            contentDescription = stringResource(R.string.previous_button),
            modifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .clickable {

                },
            tint = Color.White
        )
    }

}