package com.sean.ratel.player.core.data.player.download

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource

// DataSource.Factory 인터페이스를 직접 구현합니다.
@UnstableApi
class DynamicHeaderDataSourceFactory(
    private val httpDataSourceFactory: DefaultHttpDataSource.Factory,
    private val headerStore: HeaderStore,
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        val httpDataSource = httpDataSourceFactory.createDataSource() as HttpDataSource
        return DynamicHeaderDataSource(httpDataSource, headerStore)
    }
}
