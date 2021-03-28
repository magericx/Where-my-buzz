package com.example.wheremybuzz.utils.helper

import com.example.wheremybuzz.api.BusScheduleApiService
import com.example.wheremybuzz.api.BusStopsCodeApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LtaRetrofitHelper {

    private const val ltaBaseUrl: String = "http://datamall2.mytransport.sg"

    private val retrofit: Retrofit by lazy{
        return@lazy Retrofit.Builder()
            .baseUrl(ltaBaseUrl)
            //.client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val busStopsCodeApiService: BusStopsCodeApiService by lazy {
        retrofit.create(BusStopsCodeApiService::class.java)
    }

    val busScheduleApiService: BusScheduleApiService by lazy {
        retrofit.create(BusScheduleApiService::class.java)
    }


}