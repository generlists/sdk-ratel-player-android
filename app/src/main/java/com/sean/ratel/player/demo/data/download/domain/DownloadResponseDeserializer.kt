package com.sean.ratel.player.demo.data.download.domain

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.sean.ratel.player.demo.data.download.FacebookDownloadInfo
import com.sean.ratel.player.demo.data.download.TikTokDownloadInfo
import java.lang.reflect.Type

class DownloadResponseDeserializer(
    val bland: DownloadBland,
) : JsonDeserializer<DownloadResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): DownloadResponse {
        val jsonObject = json.asJsonObject

        return when (bland) {
            DownloadBland.FACEBOOK -> {
                context.deserialize(
                    jsonObject,
                    FacebookDownloadInfo::class.java,
                )
            }

            DownloadBland.TIKTOK -> {
                context.deserialize(jsonObject, TikTokDownloadInfo::class.java)
            }
        }
    }
}
