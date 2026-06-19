package com.sean.ratel.player.ui.control.component.share

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShareIconItem(
    item: ShareItem,
    uri: Uri,
    onDismiss: () -> Unit,
    onClick: (String) -> Unit,
) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .width(72.dp)
                .clickable {
                    item.action(context, uri)
                    onDismiss()
                    onClick(item.label)
                },
    ) {
        Box(
            modifier =
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(item.size),
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            item.label,
            modifier = Modifier.wrapContentSize(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
