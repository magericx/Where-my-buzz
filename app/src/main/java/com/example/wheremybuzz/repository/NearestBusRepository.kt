package com.example.wheremybuzz.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wheremybuzz.api.NearestBusStopApiService
import com.example.wheremybuzz.model.NearestBusStopsResponse
import com.example.wheremybuzz.ui.main.TabFragment
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class NearestBusRepository {
    //private val nearestBusStopApiService: NearestBusStopApiService? = null
    private val TAG: String = "NearestBusRepository"
    val baseUrl: String = "https://maps.googleapis.com"
    val location: String = "1.380308, 103.741256"
    val radius: Int = 100
    val type: String = "transit_station"
    val google_api_key = "AIzaSyCoaNTRExEFYU_QOYcyrKhcbPEGGBPsFUU"

    fun getNearestBusStops(): LiveData<List<NearestBusStopsResponse>>? {
        val data: MutableLiveData<List<NearestBusStopsResponse>> =
            MutableLiveData()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(NearestBusStopApiService::class.java)
        val call = service.getNearestBusStops(
            location,
            radius,
            type, google_api_key
        )

        call.enqueue(object : retrofit2.Callback<NearestBusStopsResponse> {
            override fun onResponse(
                call: Call<NearestBusStopsResponse>,
                response: Response<NearestBusStopsResponse>
            ) {
                Log.d(TAG, "Status code is ${response.code()}")
                Log.d(TAG, "Content is ${response.body()}")
                if (response.code() == 200) {
                    val nearestBusStopsResponse = response.body()!!

                    //currently hardcoded, needs to be changed
                    val stringBuilder = "Country: " +
                            nearestBusStopsResponse.results +
                            "\n" +
                            "Temperature: " + ""
                    data.value = listOf(response.body());
                }
            }

            override fun onFailure(call: Call<NearestBusStopsResponse>, t: Throwable) {
            }
        })
        return data
    }





        //fix this later using dagger injection https://stackoverflow.com/questions/45840793/repository-module-implementation-with-context
//        val ai = context!!.packageManager
//            .getApplicationInfo(context!!.packageName, PackageManager.GET_META_DATA)
//        val google_api_key: String = ai.metaData["com.google.android.geo.API_KEY"] as String
}