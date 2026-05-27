package com.sean.ratel.player.demo.data.download.domain

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.sean.ratel.player.demo.data.download.PixabayVideoResponse
import java.lang.reflect.Type

class DownloadResponseDeserializer(
    val bland: DownloadBland,
) : JsonDeserializer<PixabayVideoResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): PixabayVideoResponse {
        val jsonObject = json.asJsonObject

        return when (bland) {
            DownloadBland.PIXABAY -> {
                context.deserialize(
                    jsonObject,
                    PixabayVideoResponse::class.java,
                )
            }
        }
    }
}
