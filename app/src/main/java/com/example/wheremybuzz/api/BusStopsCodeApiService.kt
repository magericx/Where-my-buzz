package com.example.wheremybuzz.api

import com.example.wheremybuzz.model.BusStopsCodeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BusStopsCodeApiService {
    @GET("ltaodataservice/BusStops?")
    fun getBusStopsCode(
        @Header("AccountKey") AccountKey: String,
        @Query("\$skip") skip: Int
    ): Call<BusStopsCodeResponse>
}