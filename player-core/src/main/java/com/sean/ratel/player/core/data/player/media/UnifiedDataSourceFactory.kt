package com.sean.ratel.player.core.com.sean.ratel.player.core.data.player.media

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener

@UnstableApi
class UnifiedDataSourceFactory(
    private val httpFactory: DataSource.Factory,
    private val localFactory: DataSource.Factory,
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        return object : DataSource {
            private lateinit var delegate: DataSource
            private val listeners = mutableListOf<TransferListener>()

            override fun addTransferListener(transferListener: TransferListener) {
                listeners += transferListener
                if (::delegate.isInitialized) {
                    delegate.addTransferListener(transferListener)
                }
            }

            override fun open(dataSpec: DataSpec): Long {
                val scheme = dataSpec.uri.scheme

                delegate =
                    when (scheme) {
                        "http", "https", null -> httpFactory.createDataSource()
                        "content", "file" -> localFactory.createDataSource()
                        else -> httpFactory.createDataSource()
                    }

                // 🔥 여기 중요: 기존에 등록된 리스너 전부 주입
                listeners.forEach { delegate.addTransferListener(it) }

                return delegate.open(dataSpec)
            }

            override fun read(
                buffer: ByteArray,
                offset: Int,
                length: Int,
            ): Int = delegate.read(buffer, offset, length)

            override fun getUri(): Uri? = delegate.uri

            override fun getResponseHeaders(): Map<String, List<String>> = delegate.responseHeaders

            override fun close() {
                if (::delegate.isInitialized) {
                    delegate.close()
                }
            }
        }
    }
}
