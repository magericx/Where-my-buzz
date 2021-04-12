package com.example.wheremybuzz.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.model.BusScheduleMeta
import com.example.wheremybuzz.model.BusScheduleMetaRefresh
import com.example.wheremybuzz.model.BusStopNameAndCode
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
                        Log.d(TAG, "Found bus schedules for bus stop code $busStopCode")
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

    fun getBusScheduleMetaRefreshList(
        busStopList: List<BusStopNameAndCode>,
        viewModelCallBack: (BusScheduleMetaRefresh) -> Unit
    ) {
        val retrievedBusScheduleList: MutableList<Pair<String,BusScheduleMeta>> = mutableListOf()
        val firstBusCodeData = busStopList[0].busStopCode.toLong()
        val firstBusNameData = busStopList[0].busStopName
        val service = LtaRetrofitHelper.busScheduleApiService
        val call = service.getBusScheduleMeta(
            ltaApiKey, firstBusCodeData
        )
        call.enqueue(object : retrofit2.Callback<BusScheduleMeta> {
            override fun onResponse(
                call: Call<BusScheduleMeta>,
                response: Response<BusScheduleMeta>
            ) {
                if (response.code() == 200) {
                    val busStopCodeResponse = response.body()
                    if ((busStopCodeResponse.BusStopCode.isNotEmpty()) && (!busStopCodeResponse.Services.isNullOrEmpty())) {
                        Log.d(TAG, "Found refresh data for $firstBusCodeData")
                        retrievedBusScheduleList.add(0, Pair(firstBusNameData,busStopCodeResponse))
                        viewModelCallBack(BusScheduleMetaRefresh(retrievedBusScheduleList))
                    }
                }
            }

            override fun onFailure(call: Call<BusScheduleMeta>?, t: Throwable?) {
                Log.d(TAG, "Encountered error " + t?.message)
                viewModelCallBack(BusScheduleMetaRefresh(retrievedBusScheduleList))
            }
        })
    }
}