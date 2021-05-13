package com.example.wheremybuzz.utils.helper.retrofit

import com.example.wheremybuzz.api.BusScheduleApiService
import com.example.wheremybuzz.api.BusStopsCodeApiService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
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

    private val retrofitWithRx: Retrofit by lazy{
        return@lazy Retrofit.Builder()
            .baseUrl(ltaBaseUrl)
            //.client(clientBuilder.build())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val busStopsCodeApiService: BusStopsCodeApiService by lazy {
        retrofit.create(BusStopsCodeApiService::class.java)
    }

    val busStopsCodeApiServiceWithRx: BusStopsCodeApiService by lazy {
        retrofitWithRx.create(BusStopsCodeApiService::class.java)
    }

    val busScheduleApiService: BusScheduleApiService by lazy {
        retrofit.create(BusScheduleApiService::class.java)
    }

    val busScheduleApiServiceWithRx: BusScheduleApiService by lazy {
        retrofitWithRx.create(BusScheduleApiService::class.java)
    }

}