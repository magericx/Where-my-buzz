package com.example.wheremybuzz.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.model.BusStopMeta
import com.example.wheremybuzz.model.GeoLocation
import com.example.wheremybuzz.model.InnerBusStopMeta
import com.example.wheremybuzz.model.NearestBusStopsResponse
import com.example.wheremybuzz.utils.helper.retrofit.GoogleRetrofitHelper
import retrofit2.Call
import retrofit2.Response


class NearestBusRepository {
    companion object{
        private val TAG: String = "NearestBusRepository"
        private val radius: Int = 300
        private val type: String = "transit_station"
        private val context: Context = MyApplication.instance.applicationContext
        private val ai: ApplicationInfo = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        private val googleApiKey: String = ai.metaData["com.google.android.geo.API_KEY"] as String
    }

    //For 1 bus stop
    //private val radius: Int = 100
    //For multiple bus stops


    fun getNearestBusStops(
        location: GeoLocation,
        viewModelCallBack: (BusStopMeta) -> Unit
    ) {
        Log.d(TAG,"Passed in geoLocation is $location")
        var innerBusStopMeta: InnerBusStopMeta?
        val busStopMetaList: MutableList<InnerBusStopMeta?>? = mutableListOf()

        val service = GoogleRetrofitHelper.nearestBusStopApiService
        val call = service.getNearestBusStops(
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
                    //data.postValue(busStopMeta)
                } else {
                    Log.d(TAG, "Status code is " + response.code())
//                    data.postValue(busStopMeta)
                    viewModelCallBack(BusStopMeta(busStopMetaList))
                }
            }

            override fun onFailure(call: Call<NearestBusStopsResponse>, t: Throwable) {
                Log.d(TAG, "Encountered error " + t.message)
                //data.postValue(busStopMeta)
                viewModelCallBack(BusStopMeta(busStopMetaList))
            }
        })
    }
}