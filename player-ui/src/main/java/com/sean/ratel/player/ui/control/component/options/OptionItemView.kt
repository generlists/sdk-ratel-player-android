package com.sean.ratel.player.ui.control.component.options

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun OptionItemView(
    item: OptionItem
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable(
                enabled = item.enabled && !item.locked,
                onClick = item.onClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = when {
                            item.enabled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.77f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )

            }

            if (item.locked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "locked",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .background(Color.Black, CircleShape)
                )
            }
        }

        Spacer(Modifier.height(8.dp))


        Text(
            text = buildAnnotatedString {
                val label = item.label

                val start = label.indexOf("(")
                val end = label.indexOf(")")

                if (start != -1 && end != -1 && start < end) {
                    append(label.substring(0, start + 1))
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.ExtraBold,
                        )
                    ) {
                        append(label.substring(start + 1, end))
                    }
                    append(")")
                } else {
                    append(label)
                }
            },
            modifier = Modifier.wrapContentSize(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center

        )
    }

}