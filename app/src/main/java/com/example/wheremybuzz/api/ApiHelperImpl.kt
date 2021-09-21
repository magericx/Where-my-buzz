package com.example.wheremybuzz.api

import com.example.wheremybuzz.model.BusScheduleMeta
import com.example.wheremybuzz.model.BusStopsCodeResponse
import com.example.wheremybuzz.model.NearestBusStopsResponse
import io.reactivex.Observable
import retrofit2.Call
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(
    private val busScheduleService: BusScheduleApiService,
    private val busStopCodeApiService: BusStopsCodeApiService,
    private val nearestBusStopApiService: NearestBusStopApiService
) : ApiHelper {
    override fun getBusScheduleMeta(accountKey: String, skip: Long): Call<BusScheduleMeta> {
        return busScheduleService.getBusScheduleMeta(accountKey, skip)
    }

    override fun getBusScheduleMetaObservable(
        accountKey: String,
        skip: Long
    ): Observable<BusScheduleMeta> {
        return busScheduleService.getBusScheduleMetaObservable(accountKey, skip)
    }

    override fun getNearestBusStops(
        location: String,
        radius: Int,
        type: String,
        key: String
    ): Call<NearestBusStopsResponse> {
        return nearestBusStopApiService.getNearestBusStops(location, radius, type, key)
    }

    override fun getBusStopsCode(accountKey: String, skip: Int): Call<BusStopsCodeResponse> {
        return busStopCodeApiService.getBusStopsCode(accountKey, skip)
    }

    override fun getBusStopsCodeObservable(
        accountKey: String,
        skip: Int
    ): Observable<BusStopsCodeResponse> {
        return busStopCodeApiService.getBusStopsCodeObservable(accountKey, skip)
    }
}