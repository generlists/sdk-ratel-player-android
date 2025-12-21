package com.sean.ratel.player.demo


//import com.sean.ratel.player.demo.ui.navigation.NavGraph
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sean.ratel.player.demo.ui.home.HomeTopBar
import com.sean.ratel.player.demo.ui.home.MainTab
import com.sean.ratel.player.demo.ui.navigation.Destination
import com.sean.ratel.player.demo.ui.navigation.NavGraph
import com.sean.ratel.player.demo.ui.theme.Background_op_20
import com.sean.ratel.player.demo.ui.theme.DemoplayerTheme

@Composable
fun DemoPlayApp(
    mainViewModel: MainViewModel,
    finish: () -> Unit,
) {
    DemoplayerTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: Destination.Home.route
        val selectedTab = remember { mutableStateOf<MainTab>(MainTab.YOUTUBE) }

        Scaffold(
            modifier = Modifier.imePadding(),
            topBar = {
                if(currentRoute != Destination.EndPlayer.route && currentRoute != Destination.BasicPlayer.route
                    && currentRoute != Destination.AdvancePlayer.route){
                    HomeTopBar()
                }
            },

            floatingActionButtonPosition = FabPosition.End,
        ) { innerPaddingModifier ->

            Column(modifier = Modifier
                .fillMaxSize()
                .background(Background_op_20)
                .padding(innerPaddingModifier)) {

                if (currentRoute != Destination.EndPlayer.route && currentRoute != Destination.BasicPlayer.route
                    && currentRoute != Destination.AdvancePlayer.route
                ) {
                    TopTabBar(changeSelectedIndex = {
                        selectedTab.value = it
                    })
                }
                    LaunchedEffect(Unit) {
                        mainViewModel.loadBasicJsonData()
                    }

                NavGraph(
                    navController = navController,
                    modifier = Modifier.padding(innerPaddingModifier),
                    navigator = mainViewModel.navigator,
                    finish = finish,
                )
                LaunchedEffect(selectedTab.value) {
                    when(selectedTab.value) {
                        MainTab.YOUTUBE -> navController.navigate(Destination.Home.route)
                        MainTab.DOWNLOAD -> navController.navigate(Destination.Download.route)
                    }
                }

            }

        }
    }
}
@Suppress("ktlint:standard:function-naming")
@Composable
fun TopTabBar(changeSelectedIndex:(MainTab)->Unit) {
    val tabs = remember { MainTab.entries.toTypedArray().asList() }
    var selectedTabIndex by remember { mutableStateOf(0) }

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.White,
        // 탭 바의 좌우 여백
        edgePadding = 8.dp,
        indicator = {},
    ) {
        tabs.forEachIndexed { index, item ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    changeSelectedIndex(if(selectedTabIndex ==0) MainTab.YOUTUBE else MainTab.DOWNLOAD)
                  }

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
                        color = if (selectedTabIndex == index) Color.Black else Color.Gray,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    DemoplayerTheme {
    }
}
