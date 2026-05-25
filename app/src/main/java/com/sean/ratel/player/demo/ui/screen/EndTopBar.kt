package com.sean.ratel.player.demo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.player.demo.ui.view.BackButton
import com.sean.ratel.player.demo.ui.view.BookMarkButton
import com.sean.ratel.player.demo.ui.view.InfoButton
import com.sean.ratel.player.demo.ui.view.OptionButton
import com.sean.ratel.player.demo.ui.view.ShareButton

@Suppress("ktlint:standard:function-naming")
@Composable
fun EndTopBar(
    modifier: Modifier,
    historyBack: () -> Unit,
    infoClick: () -> Unit,
    shareButtonClick: () -> Unit,
    optionClick: () -> Unit = {},
    bookMarkClick: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(enabled = false, onClick = {})
                .background(Color.Transparent),
        contentAlignment = Alignment.CenterStart,
    ) {
        val density = LocalDensity.current

        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onClick = {
                historyBack()
            })

            Text(
                "가나다라마바사아자차카타파하아아아아아아아아아아아아아",
                modifier =
                    Modifier
                        .wrapContentSize()
                        .weight(0.8f)
                        .padding(start = 5.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(Modifier.wrapContentSize()) {
                InfoButton(onClick = {
                    infoClick()
                })
                BookMarkButton(onClick = {
                    bookMarkClick()
                })
                ShareButton(onClick = {
                    shareButtonClick()
                })
                OptionButton(onClick = {
                    optionClick()
                })
            }
        }
    }
}
