package com.sean.ratel.player.core.data.player.download

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import com.sean.ratel.player.utils.log.RLog
import java.io.IOException

@UnstableApi
class DynamicHeaderDataSource(
    private val httpDataSource: HttpDataSource,
    private val headerStore: HeaderStore,
) : DataSource by httpDataSource {


    @Throws(IOException::class)
    override fun open(dataSpec: DataSpec): Long {

        val headers = headerStore.getHeaders(dataSpec.key ?: "")
        RLog.d("DynamicHeaderDataSource","key : ${dataSpec.key} , headers : ${headers}")

        val defaultHttpDataSource = httpDataSource as DefaultHttpDataSource
        headers?.forEach { (key, value) ->
            value?.let {
               defaultHttpDataSource.setRequestProperty(key, it)
                RLog.d("DynamicHeaderDataSource","key : $key ,,,,,  value:  $it")
            }
        }

        // 복구 실패 시 원본으로 시도
        return httpDataSource.open(dataSpec)
    }

    override fun getResponseHeaders(): Map<String, List<String>> = httpDataSource.responseHeaders

}