package com.sean.ratel.player.core.data.player.download



import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class HeaderStore @Inject constructor() {

    private val store = mutableMapOf<String, Map<String, String?>>()

    fun saveHeaders(id: String, headers: Map<String, String?>) {
        store[id] = headers
    }

    fun getHeaders(id: String): Map<String, String?>? {
        return store[id]
    }
}