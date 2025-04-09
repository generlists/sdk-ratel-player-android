package com.sean.ratel.player.demo.ui.home
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.theme.DemoplayerTheme
import com.sean.ratel.player.demo.ui.theme.TopColor

@Composable
fun HomeTopBar() {

        // 실제 TopBar 콘텐츠
        Box(
            Modifier
                .fillMaxWidth()
                .background(TopColor)
                .height(56.dp)
                .padding(WindowInsets.statusBars.asPaddingValues()),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, bottom = 10.dp),

                verticalAlignment = Alignment.CenterVertically,
            ) {
                TitleBox()
                Spacer(modifier = Modifier.weight(1f))
                OptionMenu({})
            }
        }
}

@Composable
fun TitleBox() {
    Text(
        stringResource(R.string.title),
        modifier =
            Modifier
                .wrapContentSize(),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
    )
}

@Composable
fun OptionMenu(
    privacyOptionClick: () -> Unit,
) {

        Box(
            modifier = Modifier.wrapContentSize(),
        ) {
            IconButton(onClick = privacyOptionClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
}


@Preview(showBackground = true)
@Composable
private fun HomeTopBarPreview() {
    DemoplayerTheme {
        //HomeTopBar(Modifier, MainViewModel(), {})
    }
}
