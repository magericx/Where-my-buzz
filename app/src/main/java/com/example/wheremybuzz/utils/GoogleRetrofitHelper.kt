package com.example.wheremybuzz.utils

import com.example.wheremybuzz.api.NearestBusStopApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GoogleRetrofitHelper {

    private const val googleBaseUrl: String = "https://maps.googleapis.com"

    private val retrofit: Retrofit by lazy {
        return@lazy Retrofit.Builder()
            .baseUrl(googleBaseUrl)
            //.client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val nearestBusStopApiService: NearestBusStopApiService by lazy {
        retrofit.create(NearestBusStopApiService::class.java)
    }

}