package com.sean.ratel.player.utils.log

object Utils {

    fun extractTtChainToken(cookie: String): String? {
        val regex = Regex("""tt_chain_token="?([^";]+)"?""")
        val match = regex.find(cookie)
        return match?.groups?.get(1)?.value
    }
}