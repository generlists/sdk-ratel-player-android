package com.sean.ratel.player.ui.control.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sean.ratel.player.ui.R


@Composable
fun PlayerErrorDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    message: String,
    confirmText: String? = stringResource(R.string.alert_confirm),
    onConfirm: (() -> Unit)? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
) {
    if (!show) return

    Dialog(
        onDismissRequest = {
            if (dismissOnBackPress || dismissOnClickOutside) onDismiss()
        }
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),

            )
        {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .widthIn(min = 280.dp, max = 340.dp)
            ) {

                title?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(12.dp))
                }

                // Message
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 16.sp,
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    if (onConfirm != null && !confirmText.isNullOrBlank()) {
                        Button(
                            onClick = {
                                onConfirm()
                                onDismiss()
                            }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                confirmText, color = Color.Black, fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}