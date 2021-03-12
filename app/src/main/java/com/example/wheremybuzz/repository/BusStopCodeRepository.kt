package com.example.wheremybuzz.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.api.BusStopsCodeApiService
import com.example.wheremybuzz.model.BusStopsCodeResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BusStopCodeRepository {
    private val TAG: String = "BusStopCodeRepository"
    private val baseUrl: String = "http://datamall2.mytransport.sg"
    private val context: Context = MyApplication.instance.applicationContext
    private val ai: ApplicationInfo = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    private val ltaApiKey: String = ai.metaData["com.lta.android.geo.LTA_KEY"] as String

    val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder();
    val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor();

    //temp solution, need to change to singleton
    private fun getRetrofit(baseUrl: String): Retrofit {
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addInterceptor(loggingInterceptor)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getBusStopCode(
        busStopName: String,
        latitude: Double,
        longtitude: Double
    ): LiveData<BusStopsCodeResponse>? {
        val data: MutableLiveData<BusStopsCodeResponse> =
            MutableLiveData()

        var skip = 0
        val retrofit = getRetrofit(baseUrl)
        val service = retrofit.create(BusStopsCodeApiService::class.java)
        val call = service.getBusStopsCode(
            ltaApiKey, skip
        )

        call.enqueue(object : retrofit2.Callback<BusStopsCodeResponse> {
            override fun onResponse(
                call: Call<BusStopsCodeResponse>,
                response: Response<BusStopsCodeResponse>
            ) {
                Log.d(TAG, "Status code is ${response.code()}")
                Log.d(TAG, "Content is ${response.body()}")
                if (response.code() == 200) {
                    val nearestBusStopsResponse = response.body()
                    //data.postValue(busStopMeta)
                } else {
                    Log.d(TAG, "Status code is " + response.code())
                    Log.d(TAG, "Message is " + response.message())
                    Log.d(TAG, "Body is " + response.body())
                    //busStopMeta = BusStopMeta(busStopMetaList)
                    //data.postValue(busStopMeta)

                }
            }

            override fun onFailure(call: Call<BusStopsCodeResponse>, t: Throwable) {
                Log.d(TAG, "Encountered error " + t.message)
//                busStopMeta = BusStopMeta(busStopMetaList)
//                data.postValue(busStopMeta)
            }

        })
        return data
    }
}