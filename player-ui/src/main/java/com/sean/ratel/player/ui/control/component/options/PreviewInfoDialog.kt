package com.sean.ratel.player.ui.control.component.options

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sean.ratel.player.core.data.domain.model.InfoType
import com.sean.ratel.player.core.data.domain.model.PreviewInfoData
import com.sean.ratel.player.ui.R
import com.sean.ratel.player.ui.ThemeMode

@Composable
@Suppress("ktlint:standard:function-naming")
fun PrevViewInfoDialog(
    data: PreviewInfoData,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onConfirm: (InfoType) -> Unit,
    onMemoChange: (Boolean, String, String) -> Unit = { type, id, text -> },
    onDismiss: () -> Unit,
) {
    // 1. 펼침 상태 관리
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = data.title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                data.bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                    border =
                        BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (themeMode == ThemeMode.DARK) 0.dp else 4.dp),
                ) {
                    when (data.infoType) {
                        InfoType.ScreenShot -> {
                            val mainInfoFirst =
                                data.mainInfoList.filter {
                                    it.first in
                                        listOf(
                                            stringResource(com.sean.ratel.player.core.R.string.info_file_name),
                                            stringResource(com.sean.ratel.player.core.R.string.info_save_path),
                                        )
                                }
                            val mainInfoSecond =
                                data.mainInfoList.filter {
                                    it.first in
                                        listOf(
                                            stringResource(com.sean.ratel.player.core.R.string.info_screen_size),
                                            stringResource(com.sean.ratel.player.core.R.string.info_create_date),
                                        )
                                }

                            mainInfoFirst.forEach { (label, value) ->
                                CompactInfoItem(label = label, value = value, isFullWidth = true)
                            }

                            mainInfoSecond.chunked(2).forEach { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    rowItems.forEach { (label, value) ->
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CompactInfoItem(label = label, value = value)
                                        }
                                    }
                                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        InfoType.ScrapVideoInfo -> {
                            // 메인 박스 정보
                            Spacer(modifier = Modifier.height(16.dp))
                            val mainDescription =
                                data.mainInfoList.find { it.first in listOf("설명") }

                            val mainFilterList =
                                data.mainInfoList.filterNot { it.first in listOf("설명") }

                            mainFilterList.chunked(2).forEach { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    rowItems.forEach { (label, value) ->
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CompactInfoItem(label = label, value = value)
                                        }
                                    }
                                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, end = 20.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier.wrapContentSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CompactInfoItem(
                                            label = mainDescription?.first ?: "",
                                            value = mainDescription?.second ?: "",
                                            isFullWidth = true,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        InfoType.LocalVideoInfo, InfoType.Share -> {
                            // 메인 박스 정보
                            Spacer(modifier = Modifier.height(16.dp))
                            val mainFullWidthList =
                                data.mainInfoList.filter { it.first in listOf("상대경로", "저장위치") }

                            val mainFilterList =
                                data.mainInfoList.filterNot { it.first in listOf("상대경로", "저장위치") }

                            mainFilterList.chunked(2).forEach { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    rowItems.forEach { (label, value) ->
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CompactInfoItem(label = label, value = value)
                                        }
                                    }
                                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }

                            mainFullWidthList.forEach { (label, value) ->
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 20.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CompactInfoItem(
                                        label = label,
                                        value = value,
                                        isFullWidth = true,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (data.infoType == InfoType.ScrapVideoInfo ||
                    data.infoType == InfoType.LocalVideoInfo
                ) {
                    MemoTextView(data, onMemoChange = onMemoChange)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 4. [더보기] 버튼
                data.subInfoList?.let {
                    TextButton(
                        onClick = { expanded = !expanded },
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(
                            if (expanded) {
                                stringResource(R.string.player_control_info_off)
                            } else {
                                stringResource(
                                    R.string.player_control_info_more,
                                )
                            },
                            color = MaterialTheme.colorScheme.surfaceDim,
                        )
                    }
                }

                // 5. [추가 정보] 영역 (애니메이션 효과)
                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally),
                    ) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.LightGray.copy(alpha = 0.5f),
                        )

                        when (data.infoType) {
                            InfoType.ScreenShot -> {
                                data.subInfoList?.chunked(2)?.forEach { rowItems ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        rowItems.forEach { (label, value) ->
                                            Box(
                                                modifier = Modifier.weight(1f),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                CompactInfoItem(label = label, value = value)
                                            }
                                        }
                                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            InfoType.ScrapVideoInfo -> {
                                data.subInfoList?.forEach { (label, value) ->
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(start = 10.dp, end = 10.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CompactInfoItem(
                                                label = label,
                                                value = value,
                                                isFullWidth = true,
                                            )
                                        }
                                    }
                                }
                            }

                            InfoType.LocalVideoInfo, InfoType.Share -> {
                                val subScreenSize =
                                    data.subInfoList?.find { it.first in listOf("해상도") }

                                val subFilterList =
                                    data.subInfoList?.filterNot { it.first in listOf("해상도") }

                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(start = 10.dp, end = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier.wrapContentSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CompactInfoItem(
                                                label = subScreenSize?.first ?: "",
                                                value = subScreenSize?.second ?: "",
                                                isFullWidth = true,
                                            )
                                        }
                                    }
                                }
                                subFilterList?.chunked(2)?.forEach { rowItems ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        rowItems.forEach { (label, value) ->
                                            Box(
                                                modifier = Modifier.weight(1f),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                CompactInfoItem(label = label, value = value)
                                            }
                                        }
                                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // 하단 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (data.bitmap != null) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                data.cancelText,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 16.sp,
                            )
                        }
                    }
                    Button(
                        onClick = {
                            onConfirm(data.infoType)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(
                            data.confirmText,
                            color = Color.Black,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun CompactInfoItem(
    label: String,
    value: String,
    isFullWidth: Boolean = false,
) {
    Column(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        if (value.isNotEmpty()) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
            )
            Text(
                text = value,
                Modifier.then(if (isFullWidth) Modifier.fillMaxWidth() else Modifier.width(80.dp)),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = if (label == stringResource(R.string.player_control_discription)) 2 else Int.MAX_VALUE,
                overflow = if (label == stringResource(R.string.player_control_discription)) TextOverflow.Ellipsis else TextOverflow.Clip,
            )
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun MemoTextView(
    data: PreviewInfoData,
    onMemoChange: (Boolean, String, String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val memoMaxSize = 200
        var memoText by remember { mutableStateOf(data.memoText) }
        Box(
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = stringResource(R.string.recently_scrap_memo_input_title),
                color = MaterialTheme.colorScheme.surfaceDim,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = memoText ?: "",
                onValueChange = { newValue ->
                    memoText = newValue.take(memoMaxSize)
                },
                placeholder = {
                    Text(
                        stringResource(R.string.recently_scrap_memo_input_hint),
                        color = Color.LightGray,
                        fontSize = 14.sp,
                    )
                },
                textStyle =
                    TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp,
                    ),
                modifier =
                    Modifier
                        .weight(1.0f)
                        .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface,
                    ),
            )
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = {
                    onMemoChange(data.infoType == InfoType.LocalVideoInfo, data.id, memoText)
                },
                modifier = Modifier.wrapContentSize(),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    stringResource(R.string.memo_save),
                    color = Color.Black,
                    fontSize = 16.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text =
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                        ) {
                            append("(")
                            append("${memoText.length ?: 0}")
                        }
                        append(" / $memoMaxSize)")
                    },
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 12.sp,
            )
        }
    }
}
