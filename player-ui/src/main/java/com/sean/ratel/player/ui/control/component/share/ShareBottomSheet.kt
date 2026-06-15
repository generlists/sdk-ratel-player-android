package com.sean.ratel.player.ui.control.component.share

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.sean.ratel.player.ui.R
import com.sean.ratel.player.ui.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import so.smartlab.common.utils.log.RLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    uri: Uri,
    themeMode: ThemeMode,
    imageShareManager: ImageShareManager,
    onDismiss: () -> Unit,
    onClick: (String) -> Unit,
    shareLauncher: ActivityResultLauncher<Intent>?,
) {
    val context = LocalContext.current

    val shareItems =
        listOf(
            ShareItem(
                label = stringResource(R.string.kakaotallk),
                packageName = stringResource(R.string.kakaotallk_package_name),
                icon = painterResource(R.drawable.ic_kakaotallk),
                size = 42.dp,
            ) { ctx, u -> imageShareManager.shareToKakao(ctx, u, shareLauncher) },
            ShareItem(
                label = stringResource(R.string.x),
                packageName = stringResource(R.string.x_package_name),
                icon = painterResource(if (themeMode == ThemeMode.DARK) R.drawable.ic_x_logo_white else R.drawable.ic_x_logo_black),
                size = 36.dp,
            ) { ctx, u -> imageShareManager.shareToX(ctx, u, shareLauncher) },
            ShareItem(
                label = stringResource(R.string.line),
                packageName = stringResource(R.string.line_package_name),
                icon = painterResource(R.drawable.ic_line),
            ) { ctx, u -> imageShareManager.shareToLine(ctx, u, shareLauncher) },
            ShareItem(
                label = stringResource(R.string.instagram),
                packageName = stringResource(R.string.instagram_package_name),
                icon = painterResource(R.drawable.ic_instagram_logo),
            ) { ctx, u -> imageShareManager.shareToInstagram(ctx, u, shareLauncher) },
            ShareItem(
                label = stringResource(R.string.face_book),
                packageName = stringResource(R.string.face_book_package_name),
                icon = painterResource(R.drawable.ic_facebook_logo),
            ) { ctx, u -> imageShareManager.shareToFacebook(ctx, u, shareLauncher) },
        )

    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.background,
        sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.player_capture_share),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 20.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = if (themeMode == ThemeMode.DARK) 0.dp else 4.dp),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 1000.dp),
            ) {
                shareItems
                    .filter {
                        isAppInstalled(context, it.packageName)
                    }.forEach {
                        item {
                            ShareIconItem(
                                item = it,
                                uri = uri,
                                onDismiss = onDismiss,
                                onClick = onClick,
                            )
                        }
                    }
            }
        }
    }
}

private fun isAppInstalled(
    context: Context,
    packageName: String,
): Boolean =
    try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: Exception) {
        false
    }
