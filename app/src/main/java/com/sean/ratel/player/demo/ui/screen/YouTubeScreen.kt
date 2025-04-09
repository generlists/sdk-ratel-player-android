package com.sean.ratel.player.demo.ui.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.player.demo.MainViewModel
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.navigation.Destination
import com.sean.ratel.player.demo.ui.theme.DemoplayerTheme


@Composable
fun YouTubeScreen(viewModel: MainViewModel) {
    var basicDataModel = viewModel.youtubeModel.collectAsState()
    var advanceDataModel = viewModel.youtubeModelList.collectAsState()



    Column(Modifier.fillMaxWidth()) {
        TitleArea(basicDataModel.value?.exampleTitle?:"")
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().height(60.dp).background(Color.White), verticalAlignment = Alignment.CenterVertically){
            BasicPlayer(viewModel)
        }
        Spacer(Modifier.height(10.dp))
        TitleArea(advanceDataModel.value?.exampleTitle?:"")
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().height(60.dp).background(Color.White), verticalAlignment = Alignment.CenterVertically){
            AdvancePlayer(viewModel)
        }
    }

}

@Composable
private fun TitleArea(
    title: String,
) {
    var textWidth by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .height(30.dp)
          ,
        contentAlignment = Alignment.CenterStart,
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .wrapContentSize(),
            ) {
                Column(Modifier.wrapContentSize()) {
                    Text(
                        text =
                            title,
                        Modifier
                            .padding(start = 7.dp)
                            .wrapContentSize(),
                        fontFamily = FontFamily.SansSerif,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White,
                        onTextLayout = { textLayoutResult: TextLayoutResult ->
                            textWidth = textLayoutResult.size.width.toFloat() // 렌더링된 픽셀 크기
                        },
                        style =
                            TextStyle(
                                shadow =
                                    Shadow(
                                        color = Color.White,
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f,
                                    ),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicPlayer(viewModel: MainViewModel) {
    val youTubeModel  = viewModel.youtubeModel.collectAsState()

    Column(Modifier
        .fillMaxSize()
        .padding(start = 7.dp, end = 7.dp)) {
        Row(Modifier
            .fillMaxWidth()
            .height(60.dp),
            verticalAlignment = Alignment.CenterVertically) {
            youTubeModel.value?.let {
                Text(
                    it.videoModel.title,
                    Modifier.weight(0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
                    .height(60.dp), contentAlignment = Alignment.CenterEnd
            ) {
                val safeVideoId = youTubeModel.value?.videoModel?.videoId?.takeIf { it.isNotBlank() } ?: ""
                Button(onClick = {
                    viewModel.navigator.navigateTo(Destination.BasicPlayer.dynamicRoute(safeVideoId))

                }) {
                    Text(stringResource(R.string.play))
                }
            }
        }

    }
}
@Composable
private fun AdvancePlayer(viewModel: MainViewModel) {
    val youTubeAdvanceModel  = viewModel.youtubeModelList.collectAsState()

    Column(Modifier
        .fillMaxSize()
        .padding(start = 7.dp, end = 7.dp)) {
        Row(Modifier
            .fillMaxWidth()
            .height(60.dp),
            verticalAlignment = Alignment.CenterVertically) {
            youTubeAdvanceModel.value?.videoList?.get(0)?.let {
                Text(
                    it.videoModel.title,
                    Modifier.weight(0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
                    .height(60.dp), contentAlignment = Alignment.CenterEnd
            ) {

                val safeVideoId =
                    youTubeAdvanceModel.value?.videoList?.joinToString(",") { it.videoModel.videoId }?.takeIf{ it.isNotBlank() } ?: ""
                Button(onClick = {
                    viewModel.navigator.navigateTo(Destination.AdvancePlayer.dynamicRoute(safeVideoId))

                }) {
                    Text(stringResource(R.string.play))
                }
            }
        }

    }
}
@Preview(showBackground = true)
@Composable
fun PlayerViewPreview() {
    DemoplayerTheme {
    }
}