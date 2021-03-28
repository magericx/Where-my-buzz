package com.example.wheremybuzz.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.model.BusScheduleMeta
import com.example.wheremybuzz.utils.helper.LtaRetrofitHelper
import retrofit2.Call
import retrofit2.Response

class BusScheduleRepository {
    private val TAG: String = "BusScheduleRepository"
    private val context: Context = MyApplication.instance.applicationContext
    private val ai: ApplicationInfo = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    private val ltaApiKey: String = ai.metaData["com.lta.android.geo.LTA_KEY"] as String

    fun getBusScheduleMetaList(busStopCode: Long): LiveData<BusScheduleMeta>? {
        val data: MutableLiveData<BusScheduleMeta> =
            MutableLiveData()
        val service = LtaRetrofitHelper.busScheduleApiService
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