package com.sean.ratel.player.demo.di

import com.google.gson.GsonBuilder

import com.sean.ratel.player.demo.data.download.api.ShortFormDownloadApi
import com.sean.ratel.player.demo.di.qualifier.DownloadApiBaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideShortFormDownloadApi(
        @DownloadApiBaseUrl baseUrl: String,
        okHttpClient: OkHttpClient,
    ): ShortFormDownloadApi {
        return createService(
            baseUrl,
            okHttpClient,
            ShortFormDownloadApi::class.java,
        )
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =  OkHttpClient
        .Builder()
        .readTimeout(60L, TimeUnit.SECONDS)
        .writeTimeout(60L, TimeUnit.SECONDS)
        .build()


    fun <T> createService(
        @DownloadApiBaseUrl baseUrl: String,
        okHttpClient: OkHttpClient,
        service: Class<T>,
    ): T =
        Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
            .create(service)


    @Provides
    @Singleton
    @DownloadApiBaseUrl
    fun provideShortFormDownloadBaseUrl(): String = "http://10.0.2.2:8000"

}