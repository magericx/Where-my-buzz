package com.example.wheremybuzz.module

import com.example.wheremybuzz.BuildConfig
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Provides
    @Named("LTA_BASE_URL")
    fun provideLtaUrl() = BuildConfig.LTA_BASE_URL

    @Provides
    @Named("GOOGLE_BASE_URL")
    fun provideGoogleUrl() = BuildConfig.GOOGLE_BASE_URL


    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    @Provides
    @Singleton
    @Named("LtaRetrofit")
    fun provideLtaRetrofit(
        okHttpClient: OkHttpClient,
        @Named("LTA_BASE_URL") BASE_URL: String
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    @Named("LtaRetrofitRx")
    fun provideLtaRetrofitRx(
        okHttpClient: OkHttpClient,
        @Named("LTA_BASE_URL") BASE_URL: String
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    @Named("GoogleRetrofit")
    fun provideGoogleRetrofit(
        okHttpClient: OkHttpClient,
        @Named("GOOGLE_BASE_URL") BASE_URL: String
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideBusScheduleApiService(@Named("LtaRetrofitRx") retrofit: Retrofit): BusScheduleApiService =
        retrofit.create(
            BusScheduleApiService::class.java
        )

    @Provides
    @Singleton
    fun provideBusStopsCodeApiService(@Named("LtaRetrofitRx") retrofit: Retrofit): BusStopsCodeApiService =
        retrofit.create(
            BusStopsCodeApiService::class.java
        )

    @Provides
    @Singleton
    fun provideNearestBusStopApiService(@Named("GoogleRetrofit") retrofit: Retrofit): NearestBusStopApiService =
        retrofit.create(
            NearestBusStopApiService::class.java
        )

    @Provides
    @Singleton
    fun provideApiHelper(apiHelper: ApiHelperImpl): ApiHelper = apiHelper

}