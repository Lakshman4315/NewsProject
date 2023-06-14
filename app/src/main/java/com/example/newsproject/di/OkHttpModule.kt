package com.example.newsproject.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OkHttpModule {

    @Provides
    @Singleton
    fun provideOnlineInterceptor(): Interceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val maxAge = 60 * 60
        response
            .newBuilder()
            .header(CACHE_CONTROL, "public, max-age=$maxAge")
            .removeHeader(PRAGMA)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        interceptor: Interceptor,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addNetworkInterceptor(interceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        httpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("Replace this URL with Base URL on API")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(httpClient)
            .build()

    private const val CACHE_CONTROL = "Cache-Control"
    private const val PRAGMA = "Pragma"
}