package com.example.wheremybuzz.api

import com.example.wheremybuzz.model.BusScheduleMeta
import com.example.wheremybuzz.model.BusStopsCodeResponse
import com.example.wheremybuzz.model.NearestBusStopsResponse
import io.reactivex.Observable
import retrofit2.Call

interface ApiHelper {

    fun getBusScheduleMeta(accountKey: String, skip: Long): Call<BusScheduleMeta>
    fun getBusScheduleMetaObservable(accountKey: String, skip: Long): Observable<BusScheduleMeta>
    fun getNearestBusStops(
        location: String,
        radius: Int,
        type: String,
        key: String
    ): Call<NearestBusStopsResponse>

    fun getBusStopsCode(accountKey: String, skip: Int): Call<BusStopsCodeResponse>
    fun getBusStopsCodeObservable(accountKey: String, skip: Int): Observable<BusStopsCodeResponse>
}