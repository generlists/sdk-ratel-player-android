package com.sean.ratel.player.demo.ui.screen

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.sean.ratel.player.core.data.domain.model.InfoType
import com.sean.ratel.player.core.data.domain.model.PlayMediaItem
import com.sean.ratel.player.core.data.domain.model.PreviewInfoData
import com.sean.ratel.player.core.data.domain.model.Quality
import com.sean.ratel.player.core.data.player.viewmodel.PlayerViewModel
import com.sean.ratel.player.ui.ThemeMode
import com.sean.ratel.player.ui.control.MediaScreen
import com.sean.ratel.player.ui.control.component.options.PrevViewInfoDialog
import com.sean.ratel.player.ui.control.component.share.ShareBottomSheet
import dagger.hilt.android.UnstableApi
import kotlinx.coroutines.launch
import java.net.URLDecoder

@OptIn(UnstableApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun EndPlayerScreen(
    modifier: Modifier,
    themeMode: ThemeMode,
    urls: String,
    startIndex: Int,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    endViewModel: VideoEndViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var shareButtonClick by remember {
        mutableStateOf<Pair<Boolean, PreviewInfoData?>>(
            Pair(
                false,
                null,
            ),
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        // endViewModel.initInterstitialAd(context)

        val receivedArg = urls

        val decodedArg = URLDecoder.decode(receivedArg, "UTF-8")

        val localList: List<Pair<String, List<Pair<Quality, PlayMediaItem>>>> =
            listOf(
                "1052137116925296" to
                    listOf(
                        Quality.SD to
                            PlayMediaItem(
                                "1052137116925296_SD",
                                "https://video-icn2-1.xx.fbcdn.net/o1/v/t2/f2/m412/" +
                                    "AQMJtPvQSKkemRRtOfy7weWS-rawsunwKOVJMpLlris00ek" +
                                    "5lYVzC1Hcd5DVgvqjKYGuImt8mz5RSFF3belMuWjzWseId4aAAAzXdLXCtA.mp4" +
                                    "?_nc_cat=111&_nc_sid=8bf8fe&_nc_ht=video-icn2-1.xx.fbcdn.net&_nc_oh" +
                                    "c=FIdCWvGGf94Q7kNvwHKZmKD&efg=eyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVz" +
                                    "c2l2ZS5GQUNFQk9PSy4uQzMuMzYwLnN2ZV9zZCIsInhwdl9hc3NldF9pZCI6MTM0NTIxNzk" +
                                    "5MDQ0MDUwNCwiYXNzZXRfYWdlX2RheXMiOjE2MCwidmlfdXNlY2FzZV9pZCI6MTAxMjMsImR1cmF" +
                                    "0aW9uX3MiOjEwLCJ1cmxnZW5fc291cmNlIjoid3d3In0%3D&ccb=17-1&_nc_gid=DTCCwxKbF" +
                                    "W6KORl3n-AtuA&_nc_ss=7a389&_nc_zt=28&oh=00_Af35_U-SL1uFHLTVMeRR9QAeEQjR0qQ12K" +
                                    "USFtPfTyra1g&oe=69DCD843&bitrate=327865&tag=sve_sd",
                                "",
                            ),
                        Quality.HD to
                            PlayMediaItem(
                                "1052137116925296_SD",
                                "https://video-icn2-1.xx.fbcdn.net/o1/v/t2/f2/m82/AQOvDOalD18qBhm" +
                                    "WOcRXmP-CAS0G0FIREzqC4GmXDVbpK_j1iMCWLAwuWdq4PHzcnWBSB5ff_pmRzu" +
                                    "cDURFJJSy1IEHrG05745gfpmw.mp4?_nc_cat=111&_nc_sid=5e9851&_" +
                                    "nc_ht=video-icn2-1.xx.fbcdn.net&_nc_ohc=wC5Ni4cuTnYQ7kNvwGTfgZi&efg" +
                                    "=eyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5GQUNFQk9PSy4uQzMuNDgwLmRhc2hf" +
                                    "YmFzZWxpbmVfMl92MSIsInhwdl9hc3NldF9pZCI6NjYxMzY0NTU5MDE5OTQwLCJhc3NldF9hZ2VfZG" +
                                    "F5cyI6MTIwOCwidmlfdXNlY2FzZV9pZCI6MTAwOTksImR1cmF0aW9uX3MiOjkwMCwidXJsZ2VuX3Nvd" +
                                    "XJjZSI6Ind3dyJ9&ccb=17-1&vs=2029211455f06c38&_nc_vs=HBkcFQIYT2lnX3hwdl9yZWVsc19wZX" +
                                    "JtYW5lbnRfcHJvZC9BRTQ4OTI5RDVFM0FBMTk4MkNBNjc5Q0M4OTI1NUJBQ192aWRlb19kYXNoaW5pdC5tcDQ" +
                                    "VAALIARIAKAAYABsCiAd1c2Vfb2lsATEScHJvZ3Jlc3NpdmVfcmVjaXBlATEVAAAmyP7b-b3grAIVAi" +
                                    "gCQzMsF0CMIAAAAAAAGBJkYXNoX2Jhc2VsaW5lXzJfdjERAHUCZeadAQA&_nc_gid=lF_qrbrKJ0oQRlTw" +
                                    "_fQ4bg&_nc_ss=7a389&_nc_zt=28&oh=00_Af2NpetkKnVxQ7hy29jFMXzYMpssYWbeHz9t4o7hZf6g_A&oe=" +
                                    "69D8E1F4&bitrate=0&tag=dash_baseline_2_v1",
                                "",
                            ),
                        // Quality.AUDIO to PlayMediaItem("AUDIO", "1052137116925296_SD")
                    ),
                "1DcqemYF35" to
                    listOf(
                        Quality.SD to
                            PlayMediaItem(
                                "1DcqemYF35_SD",
                                "https://video-icn2-1.xx.fbcdn.net/o1/v/t2/f2/m82/AQOvDOalD18qBh" +
                                    "mWOcRXmP-CAS0G0FIREzqC4GmXDVbpK_j1iMCWLAwuWdq4PHzcnWBSB5" +
                                    "ff_pmRzucDURFJJSy1IEHrG05745gfpmw.mp4?" +
                                    "_nc_cat=111&_nc_sid=5e9851&_nc_ht=video-icn2-1.xx." +
                                    "fbcdn.net&_nc_ohc=wC5Ni4cuTnYQ7kNvwGTfgZi&efg=eyJ2ZW5jb2RlX3RhZyI6I" +
                                    "nhwdl9wcm9ncmVzc2l2ZS5GQUNFQk9PSy4uQzMuNDgwLmRhc2hfYmFzZWxpbmVfMl92M" +
                                    "SIsInhwdl9hc3NldF9pZCI6NjYxMzY0NTU5MDE5OTQwLCJhc3NldF9hZ2VfZGF5cyI6MTIwO" +
                                    "CwidmlfdXNlY2FzZV9pZCI6MTAwOTksImR1cmF0aW9uX3MiOjkwMCwidXJsZ2VuX3NvdXJjZSI6" +
                                    "Ind3dyJ9&ccb=17-1&vs=2029211455f06c38&_nc_vs=HBkcFQIYT2lnX3hwdl9yZWVsc19wZXJtYW5lbn" +
                                    "RfcHJvZC9BRTQ4OTI5RDVFM0FBMTk4MkNBNjc5Q0M4OTI1NUJBQ192aWRlb19kYXNoaW5pdC5tcDQVAALIARIAK" +
                                    "AAYABsCiAd1c2Vfb2lsATEScHJvZ3Jlc3NpdmVfcmVjaXBlATEVAAAmyP7b-b3grAIVAigCQzMsF0CMIAAAAAAAGB" +
                                    "JkYXNoX2Jhc2VsaW5lXzJfdjERAHUCZeadAQA&_nc_gid=NyfoN7F9P7aZQJ2Gkk_X9g&_nc_ss=7a389&_nc_zt=28&oh=00_" +
                                    "Af2z0gHXcnY5xkMw9-cPX2FH4tCMx4OeGXb6h0lSXCfynQ&oe=69D8E1F4&bitrate=0&tag=dash_baseline_2_v1",
                                "",
                            ),
                        Quality.HD to
                            PlayMediaItem(
                                "1DcqemYF35_SD",
                                "https://video-icn2-1.xx.fbcdn.net/o1/v/t2/f2/m82/AQOvDOalD18qBhmWOc" +
                                    "RXmP-CAS0G0FIREzqC4GmXDVbpK_j1iMCWLAwuWdq4PHzcnWB" +
                                    "SB5ff_pmRzucDURFJJSy1IEHrG05745gfpmw.mp4?" +
                                    "_nc_cat=111&_nc_sid=5e9851&_nc_ht=video-icn2-1.xx.fbcdn.net" +
                                    "&_nc_ohc=wC5Ni4cuTnYQ7kNvwGTfgZi&efg=eyJ2ZW5jb2RlX3R" +
                                    "hZyI6Inhwdl9wcm9ncmVzc2l2ZS5GQUNFQk9PSy4uQzMuNDgwLmRhc2hfYmFzZWxp" +
                                    "bmVfMl92MSIsInhwdl9hc3NldF9pZCI6NjYx" +
                                    "MzY0NTU5MDE5OTQwLCJhc3NldF9hZ2VfZGF5cyI6MTIwOCwidmlfdXNlY2FzZV9pZCI6MTAwOTksImR1" +
                                    "cmF0aW9uX3MiOjkwMCwidXJsZ2VuX3NvdXJjZSI6Ind3dyJ9&ccb=17-1&vs=2029211455f06c38&_" +
                                    "nc_vs=HBkcFQIYT2lnX3hwdl9yZWVsc19wZXJtYW5lbnRfcHJvZC9BRTQ4OTI5RDVFM0FBMTk4MkNBNjc5" +
                                    "Q0M4OTI1NUJBQ192aWRlb19kYXNoaW5pdC5tcDQVAALIARIAKAAYABsCiAd1c2Vfb2lsATEScHJvZ3Jl" +
                                    "c3NpdmVfcmVjaXBlATEVAAAmyP7b-b3grAIVAigCQzMsF0CMIAAAAAAAGBJkYXNoX2Jhc2VsaW5lXzJfd" +
                                    "jERAHUCZeadAQA&_nc_gid=lF_qrbrKJ0oQRlTw_fQ4bg&_nc_ss=7a389&_nc_zt=28&oh=00_Af2NpetkKn" +
                                    "VxQ7hy29jFMXzYMpssYWbeHz9t4o7hZf6g_A&oe=69D8E1F4&bitrate=0&tag=dash_baseline_2_v1",
                                "",
                            ),
                        Quality.HD to
                            PlayMediaItem(
                                "1DcqemYF35_SD",
                                "https://video.twimg.com/amplify_video/2039915509064646656/" +
                                    "pl/mp4a/64000/D-rsMm6EWfVonkzJ.m3u8",
                                "",
                            ),
                        // Quality.AUDIO to PlayMediaItem("AUDIO", "2088456712345678_SD")
                    ),
                //            "3099988877766655" to listOf(
//                Quality.SD to PlayMediaItem("SD", "3099988877766655_SD"),
//                Quality.HD to PlayMediaItem("HD", "3099988877766655_SD"),
//                Quality.AUDIO to PlayMediaItem("AUDIO", "3099988877766655_SD")
//            )
            )

//        val restoredList = decodedArg.split(",").map { itemStr ->
//            val parts = itemStr.split(":")
//            val id = parts[0]
//            val qList = parts[1].split("|").map { qStr ->
//                val qParts = qStr.split("@")
//                // Quality Enum 복구와 URL
//                //Pair(Quality.valueOf(qParts[0]), qParts[1])
//            }
// //
//            Pair(id, qList)
//        }
//        PlayMediaItem(
//            key = parts[0],
//            url = parts[1]
        // (1052137116925296, [(SD, 1052137116925296_SD), (HD, 1052137116925296_SD), (AUDIO, 1052137116925296_SD)])
//        val urlDecodeList =listOf<String>()
//           // restoredList // (URLDecoder.decode(urls, StandardCharsets.UTF_8.toString())).split(",")
//        val localContent = listOf("content://media/external/file/1000000052")
        // val localFile = listOf("file:///storage/emulated/0/Download/kakaotallk/14902146_1080_1920_25fps.mp4")
//        restoredList.forEach {
//            Log.d("SHINHN","11 it : $it startIndex : $startIndex")
//        }
        // list: List<Pair<String, List<Pair<Quality, String>>>>
//        val localList = listOf(
//            "123445566" to emptyList<Pair<Quality,String>>()
//        )
        // val reorder = reorderMediaList(urlDecodeList+localList, startIndex)

//        reorder.forEach {
//            RLog.d("LOG_TAG", "222 it : $it startIndex : $startIndex")
//        }
        MediaScreen(
            modifier = Modifier,
            mediaOptions = endViewModel.mediaOptions,
            themeMode = themeMode,
            mediaList = localList,
            startIndex = 0,
            listStartIndex = startIndex,
            qualityStartIndex = 0,
            topBar = { onInfoClick, onOptionClick ->
                EndTopBar(
                    modifier = Modifier,
                    historyBack = {},
                    shareButtonClick = {
                        playerViewModel.getShareScreenCapture(onInfo = { shareData ->
                            shareData?.let {
                                shareButtonClick = Pair(true, shareData)
                            }
                        }, endViewModel.mediaOptions.value.speed)
                    },
                    infoClick = { onInfoClick(endViewModel.getDummyData()) },
                    bookMarkClick = {},
                    optionClick = { onOptionClick() },
                )
            },
            onOptionChanged = { k, v ->
                Log.d("LOG_TAG", "optionChanged k = $k v = $v")
                endViewModel.setUpdateMediaOption(k, v)
            },
        )
        val scope = rememberCoroutineScope()
        var openShareSheet by remember { mutableStateOf<Pair<Boolean, Uri?>>(Pair(false, null)) }

        if (shareButtonClick.first) {
            shareButtonClick.second?.let { previewInfoData ->
                PrevViewInfoDialog(previewInfoData, themeMode, onConfirm = { infoType ->
                    scope.launch {
                        previewInfoData.bitmap?.let { bitmap ->

                            playerViewModel.saveCaptureFile(
                                context,
                                previewInfoData.infoType,
                                bitmap,
                                saveComplete = { file, uri ->
                                    val uri =
                                        file?.let {
                                            FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file,
                                            )
                                        } ?: run {
                                            uri
                                        }

                                    openShareSheet = Pair(true, uri)
                                },
                            )
                        } ?: run {
                            if (infoType == InfoType.ScreenShot) {
                                Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    shareButtonClick = Pair(false, null)
                }, onDismiss = {
                    shareButtonClick = Pair(false, null)
                })
            }
        }
        if (openShareSheet.first) {
            openShareSheet.second?.let {
                ShareBottomSheet(
                    uri = it,
                    themeMode,
                    imageShareManager = endViewModel.imageShareManager,
                    onDismiss = { openShareSheet = Pair(false, null) },
                    onClick = {
                        Log.d("hbungshin", "라벨 $it")
                    },
                )
            }
        }
    }
}

fun reorderMediaList(
    list: List<Pair<String, List<Pair<Quality, String>>>>,
    startIndex: Int,
): List<Pair<String, List<Pair<Quality, String>>>> {
    if (list.isEmpty() || startIndex !in list.indices) return list

    // 1. startIndex부터 끝까지 (예: [1, 2])
    val head = list.subList(startIndex, list.size)

    // 2. 처음부터 startIndex 전까지 (예: [0])
    val tail = list.subList(0, startIndex)

    // 3. 둘이 합치기! ([1, 2] + [0])
    return head + tail
}
