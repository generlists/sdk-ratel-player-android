package com.sean.ratel.player.demo.di

import com.google.gson.GsonBuilder
import com.sean.ratel.player.demo.BuildConfig
import com.sean.ratel.player.demo.data.download.api.PixaBayApi
import com.sean.ratel.player.demo.di.qualifier.PixaBayApiBaseUrl
import com.sean.ratel.player.demo.di.qualifier.PixaBayApiKey
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
    fun providePixaBayApi(
        @PixaBayApiBaseUrl baseUrl: String,
        okHttpClient: OkHttpClient,
    ): PixaBayApi =
        createPixaBayService(
            baseUrl,
            okHttpClient,
            PixaBayApi::class.java,
        )

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .readTimeout(60L, TimeUnit.SECONDS)
            .writeTimeout(60L, TimeUnit.SECONDS)
            .build()

    fun <T> createPixaBayService(
        @PixaBayApiBaseUrl baseUrl: String,
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
    @PixaBayApiKey
    fun providePixabayApiKey(): String = BuildConfig.PEXABAY_API_KEY

    @Provides
    @Singleton
    @PixaBayApiBaseUrl
    fun providePixaBayDownloadBaseUrl(): String = "https://pixabay.com/api/"
}
