package com.example.wheremybuzz.api

import com.example.wheremybuzz.model.BusScheduleMeta
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BusScheduleApiService {
    @GET("ltaodataservice/BusArrivalv2?")
    fun getBusScheduleMeta(
        @Header("AccountKey") AccountKey: String,
        @Query("BusStopCode") skip: Long
    ): Call<BusScheduleMeta>
}