package com.example.wheremybuzz.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.api.NearestBusStopApiService
import com.example.wheremybuzz.model.BusStopMeta
import com.example.wheremybuzz.model.InnerBusStopMeta
import com.example.wheremybuzz.model.NearestBusStopsResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class NearestBusRepository {
    private val TAG: String = "NearestBusRepository"
    private val baseUrl: String = "https://maps.googleapis.com"

    //For 1 bus stop
    //private val radius: Int = 100
    //For multiple bus stops
    private val radius: Int = 300
    private val type: String = "transit_station"
    private val context: Context = MyApplication.instance.applicationContext
    private val ai: ApplicationInfo = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    private val googleApiKey: String = ai.metaData["com.google.android.geo.API_KEY"] as String

    //temp solution, need to change to singleton
    private fun getRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getNearestBusStops(location: String): LiveData<BusStopMeta>? {
        val data: MutableLiveData<BusStopMeta> =
            MutableLiveData()
        var innerBusStopMeta: InnerBusStopMeta?
        val busStopMetaList: MutableList<InnerBusStopMeta?>? = mutableListOf()
        var busStopMeta: BusStopMeta?

        val retrofit = getRetrofit(baseUrl)
        val service = retrofit.create(NearestBusStopApiService::class.java)
        val call = service.getNearestBusStops(
            location,
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
                    for (i in nearestBusStopsResponse.results.indices) {
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
                            InnerBusStopMeta(busStopName, busStopLatitude, busStopLongtitude)
                        busStopMetaList?.add(innerBusStopMeta)
                    }
                    busStopMeta = BusStopMeta(busStopMetaList)
                    data.postValue(busStopMeta)
                } else {
                    Log.d(TAG, "Status code is " + response.code())
                    busStopMeta = BusStopMeta(busStopMetaList)
                    data.postValue(busStopMeta)

                }
            }

            override fun onFailure(call: Call<NearestBusStopsResponse>, t: Throwable) {
                Log.d(TAG, "Encountered error " + t.message)
                busStopMeta = BusStopMeta(busStopMetaList)
                data.postValue(busStopMeta)
            }
        })
        return data
    }
    //fix this later using dagger injection https://stackoverflow.com/questions/45840793/repository-module-implementation-with-context
//        val ai = context!!.packageManager
//            .getApplicationInfo(context!!.packageName, PackageManager.GET_META_DATA)
//        val google_api_key: String = ai.metaData["com.google.android.geo.API_KEY"] as String
}