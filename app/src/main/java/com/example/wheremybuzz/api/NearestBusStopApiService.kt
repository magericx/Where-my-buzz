package com.example.wheremybuzz.api

import com.example.wheremybuzz.model.NearestBusStopsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NearestBusStopApiService {
    @GET("maps/api/place/nearbysearch/json?")
    fun getNearestBusStops(@Query("location") location: String, @Query("radius") radius: Int, @Query("type") type: String, @Query("key") key: String): Call<NearestBusStopsResponse>
}