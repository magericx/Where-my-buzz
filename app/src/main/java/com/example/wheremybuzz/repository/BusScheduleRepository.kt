package com.example.wheremybuzz.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.api.BusScheduleApiService
import com.example.wheremybuzz.model.BusScheduleMeta
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BusScheduleRepository {
    private val TAG: String = "BusScheduleRepository"
    private val baseUrl: String = "http://datamall2.mytransport.sg"
    private val context: Context = MyApplication.instance.applicationContext
    private val ai: ApplicationInfo = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    private val ltaApiKey: String = ai.metaData["com.lta.android.geo.LTA_KEY"] as String

    private val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
    private val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()

    //temp solution, need to change to singleton
    private fun getRetrofit(baseUrl: String): Retrofit {
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addInterceptor(loggingInterceptor)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            //.client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getBusScheduleMetaList(busStopCode: Long): LiveData<BusScheduleMeta>? {
        val data: MutableLiveData<BusScheduleMeta> =
            MutableLiveData()
        val retrofit = getRetrofit(baseUrl)
        val service = retrofit.create(BusScheduleApiService::class.java)
        val call = service.getBusScheduleMeta(
            ltaApiKey, busStopCode
        )
        call.enqueue(object : retrofit2.Callback<BusScheduleMeta> {
            override fun onResponse(
                call: Call<BusScheduleMeta>,
                response: Response<BusScheduleMeta>
            ) {
                if (response.code() == 200) {
                    val busStopCodeResponse = response.body()
                    if ((busStopCodeResponse.BusStopCode.isNotEmpty()) && (!busStopCodeResponse.Services.isNullOrEmpty())) {
                        Log.d(TAG,"Found bus schedules for bus stop code $busStopCode")
                        data.postValue(response.body())
                    }
                }
            }

            override fun onFailure(call: Call<BusScheduleMeta>?, t: Throwable?) {
                Log.d(TAG, "Encountered error " + t?.message)
            }
        })
        return data
    }
}