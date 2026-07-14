package com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.utils

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.MessageFromReceiver

/**
 * Utility class to read and parse JSON messages exchanged between sender and receiver.
 * The format of the messages is basic, no external library is needed.
 */
internal object JSONUtils {
  fun buildFlatJson(vararg args: Pair<String, String>): String {
    val jsonBuilder = StringBuilder("{")
    args.forEach { jsonBuilder.append("\"${it.first}\": \"${it.second}\",") }
    jsonBuilder.deleteCharAt(jsonBuilder.length - 1)
    jsonBuilder.append("}")

    return jsonBuilder.toString()
  }

  fun buildCommunicationConstantsJson(
    vararg pairs: Pair<String, Any>
  ): String {
    return buildString {
      append("{")
      pairs.forEachIndexed { index, (key, value) ->
        append("\"$key\":")

        if (key == "communicationConstants") {
          append(value) // 따옴표 없이 JSON 객체 삽입
        } else {
          when (value) {
            is String -> append("\"$value\"")
            else -> append(value)
          }
        }

        if (index != pairs.lastIndex) append(",")
      }
      append("}")
    }
  }

  fun parseMessageFromReceiverJson(json: String): MessageFromReceiver {
    val strings = json.split(",")
    val values = strings
      .map { it.split(":")[1].trim().replace("\"", "").replace("{", "").replace("}", "") }

    return MessageFromReceiver(values[0], values[1])
  }
}