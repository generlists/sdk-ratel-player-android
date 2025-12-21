package com.sean.ratel.player.core.data.domain.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

/**
 *
 */
@Parcelize
@Keep
data class HttpHeaders(
    val `User-Agent`: String?,
    val Accept: String?,
    val `Accept-Language`: String?,
    val `Sec-Fetch-Mode`: String?,
    val Referer: String?
): Parcelable