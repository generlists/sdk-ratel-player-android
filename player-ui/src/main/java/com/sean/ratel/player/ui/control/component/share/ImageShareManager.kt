package com.sean.ratel.player.ui.control.component.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.sean.ratel.player.ui.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageShareManager @Inject constructor() {

    fun shareChooser(context: Context, uri: Uri) {
        val intent = baseImageShareIntent(context, uri)

        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.player_control_share))
        )
    }

    fun baseImageShareIntent(context: Context, uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE_IMAGE
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(
                context.contentResolver,
                context.getString(R.string.video_scrapping_video_share),
                uri
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun shareToLine(context: Context, uri: Uri) {
        val intent = baseImageShareIntent(context, uri).apply {
            setPackage(LINE_PACKAGE_NAME)
            putExtra(Intent.EXTRA_TEXT, R.string.video_scrapping_video_share)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareChooser(context, uri)
        }
    }

    fun shareToKakao(context: Context, uri: Uri) {
        val intent = baseImageShareIntent(context, uri).apply {
            setPackage(KAKAOTALLK_PACKAGE_NAME)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Intent.createChooser(intent, context.getString(R.string.player_control_share))
        }
    }

    fun shareToInstagram(context: Context, uri: Uri) {
        val intent = baseImageShareIntent(context, uri).apply {
            setPackage(INSTAGRAM_PACKAGE_NAME)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareChooser(context, uri)
        }
    }

    fun shareToInstagramStory(context: Context, uri: Uri) {
        val intent = Intent(INSTAGRAM_STORY).apply {
            setDataAndType(uri, MIME_TYPE_IMAGE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareChooser(context, uri)
        }
    }

    fun shareToX(context: Context, uri: Uri) {
        val intent = baseImageShareIntent(context, uri).apply {
            setPackage(X_PACKAGE_NAME)
            putExtra(Intent.EXTRA_TEXT, R.string.video_scrapping_video_share)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareChooser(context, uri)
        }
    }

    fun shareToFacebook(context: Context, uri: Uri) {
        val intent = baseImageShareIntent(context, uri).apply {
            setPackage(FACE_BOOK_PACKAGE_NAME)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareChooser(context, uri)
        }
    }
    companion object {
        private val FACE_BOOK_PACKAGE_NAME ="com.facebook.katana"
        private val X_PACKAGE_NAME ="com.twitter.android"
        private val KAKAOTALLK_PACKAGE_NAME ="com.kakao.talk"
        private val LINE_PACKAGE_NAME ="jp.naver.line.android"

        private val INSTAGRAM_STORY ="com.instagram.share.ADD_TO_STORY"
        private val MIME_TYPE_IMAGE ="image/*"
        private val INSTAGRAM_PACKAGE_NAME ="com.instagram.android"
    }
}