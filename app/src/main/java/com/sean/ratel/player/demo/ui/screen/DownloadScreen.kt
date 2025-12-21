package com.sean.ratel.player.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.player.demo.data.download.domain.DownloadBland
import com.sean.ratel.player.demo.ui.download.VideoDownloadViewModel
import com.sean.ratel.player.demo.ui.home.DownloadTab

@Composable
fun DownLoadSample( viewModel: VideoDownloadViewModel){

    val selectedTab = remember { mutableStateOf<DownloadTab>(DownloadTab.FACEBOOK) }

    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(10.dp))
        DownloadTabBar(changeSelectedIndex ={
            selectedTab.value = it

        })
        Spacer(Modifier.height(10.dp))

        when (selectedTab.value) {
            DownloadTab.FACEBOOK -> DownloadFacebook(viewModel)
            DownloadTab.TIKTOK ->DownloadTikTok(viewModel)
        }

        LaunchedEffect(selectedTab.value) {
            viewModel.loadSampleData()
            val downloadBland: DownloadBland =
                if (selectedTab.value == DownloadTab.FACEBOOK) DownloadBland.FACEBOOK else if (selectedTab.value == DownloadTab.TIKTOK) DownloadBland.TIKTOK else DownloadBland.FACEBOOK
            viewModel.localDownloadList(downloadBland)
        }

    }

}
//북마크만 해놓고 나중에 다운로드
@Suppress("ktlint:standard:function-naming")
@Composable
fun DownloadTabBar(changeSelectedIndex:(DownloadTab)->Unit) {
    val tabs = remember { DownloadTab.entries.toTypedArray().asList() }
    var selectedTabIndex by remember { mutableStateOf(0) }

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Black,
        // 탭 바의 좌우 여백
        edgePadding = 8.dp,
        indicator = {},
    ) {
        tabs.forEachIndexed { index, item ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                     changeSelectedIndex(if(selectedTabIndex ==0) DownloadTab.FACEBOOK else DownloadTab.TIKTOK)
                          },

            ) {
                // 아이콘과 텍스트를 가로로 배치
                Row(
                    modifier =
                        Modifier
                            .wrapContentSize(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Text(
                        text = stringResource(item.title),
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontFamily = FontFamily.Default,
                        fontStyle = FontStyle.Normal,
                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp,
                        color = if (selectedTabIndex == index) Color.Yellow else Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}