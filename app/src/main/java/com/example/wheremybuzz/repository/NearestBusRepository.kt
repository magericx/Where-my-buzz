package com.example.wheremybuzz.repository

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.wheremybuzz.api.ApiHelper
import com.example.wheremybuzz.model.BusStopMeta
import com.example.wheremybuzz.model.GeoLocation
import com.example.wheremybuzz.model.InnerBusStopMeta
import com.example.wheremybuzz.model.NearestBusStopsResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject


class NearestBusRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiHelper: ApiHelper
) {
    companion object {
        private val TAG: String = "NearestBusRepository"
        private val radius: Int = 300
        private val type: String = "transit_station"
    }

    private val googleApiKey: String = context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    ).metaData["com.google.android.geo.API_KEY"] as String


    //For 1 bus stop
    //private val radius: Int = 100
    //For multiple bus stops
    fun getNearestBusStops(
        location: GeoLocation,
        viewModelCallBack: (BusStopMeta) -> Unit
    ) {
        Log.d(TAG, "Passed in geoLocation is $location")
        var innerBusStopMeta: InnerBusStopMeta?
        val busStopMetaList: MutableList<InnerBusStopMeta?>? = mutableListOf()


        val call = apiHelper.getNearestBusStops(
            location.retrieveStringLocation(),
            radius,
            type, googleApiKey
        )

        call.enqueue(object : retrofit2.Callback<NearestBusStopsResponse> {
            override fun onResponse(
                call: Call<NearestBusStopsResponse>,
                response: Response<NearestBusStopsResponse>
            ) {
                Log.d(TAG, "Status code is ${response.code()}")
                Log.d(TAG, "Content is ${response.body()}")
                if (response.code() == 200) {
                    val nearestBusStopsResponse = response.body()
                    for (i in nearestBusStopsResponse!!.results.indices) {
                        val busStopName = nearestBusStopsResponse.results[i].name
                        //need to split location into latitude and longtitude
                        val busStopLatitude: Double = String.format(
                            "%.5f",
                            nearestBusStopsResponse.results[i].geometry.location.lat
                        ).toDouble()
                        val busStopLongtitude = String.format(
                            "%.5f",
                            nearestBusStopsResponse.results[i].geometry.location.lng
                        ).toDouble()
                        innerBusStopMeta =
                            InnerBusStopMeta(busStopName, busStopLatitude, busStopLongtitude, 0)
                        busStopMetaList?.add(innerBusStopMeta)
                    }
                    viewModelCallBack(BusStopMeta(busStopMetaList))
                } else {
                    Log.d(TAG, "Status code is " + response.code())
                    viewModelCallBack(BusStopMeta(busStopMetaList))
                }
            }

            override fun onFailure(call: Call<NearestBusStopsResponse>, t: Throwable) {
                Log.d(TAG, "Encountered error " + t.message)
                viewModelCallBack(BusStopMeta(busStopMetaList))
            }
        })
    }
}