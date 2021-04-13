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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class BusScheduleRepository {
    private val TAG: String = "BusScheduleRepository"
    private val context: Context = MyApplication.instance.applicationContext
    private val ai: ApplicationInfo = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    private val ltaApiKey: String = ai.metaData["com.lta.android.geo.LTA_KEY"] as String

    //fetch for single bus stop
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
                    if ((busStopCodeResponse!!.BusStopCode.isNotEmpty()) && (!busStopCodeResponse.Services.isNullOrEmpty())) {
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

    //fetch for all the bus stops that require refresh
    fun getBusScheduleMetaRefreshList(
        busStopList: List<BusStopNameAndCode>,
        viewModelCallBack: (BusScheduleMetaRefresh) -> Unit
    ) {
        val retrievedBusScheduleList: MutableList<Pair<String,BusScheduleMeta>> = mutableListOf()
        val firstBusCodeData = busStopList[0].busStopCode.toLong()
        val secondBusCodeData = busStopList[1].busStopCode.toLong()
        val thirdBusCodeData = busStopList[2].busStopCode.toLong()
        val firstBusNameData = busStopList[0].busStopName
        val service = LtaRetrofitHelper.busScheduleApiServiceWithRx
        val call = service.getBusScheduleMetaObservable(
            ltaApiKey, firstBusCodeData
        )
//        call.enqueue(object : retrofit2.Callback<BusScheduleMeta> {
//            override fun onResponse(
//                call: Call<BusScheduleMeta>,
//                response: Response<BusScheduleMeta>
//            ) {
//                if (response.code() == 200) {
//                    val busStopCodeResponse = response.body()
//                    if ((busStopCodeResponse.BusStopCode.isNotEmpty()) && (!busStopCodeResponse.Services.isNullOrEmpty())) {
//                        Log.d(TAG, "Found refresh data for $firstBusCodeData")
//                        retrievedBusScheduleList.add(0, Pair(firstBusNameData,busStopCodeResponse))
//                        viewModelCallBack(BusScheduleMetaRefresh(retrievedBusScheduleList))
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<BusScheduleMeta>?, t: Throwable?) {
//                Log.d(TAG, "Encountered error " + t?.message)
//                viewModelCallBack(BusScheduleMetaRefresh(retrievedBusScheduleList))
//            }
//        })

        Log.d(TAG,"Added $firstBusCodeData")
        val requests = ArrayList<Observable<*>>()

        requests.add(service.getBusScheduleMetaObservable(
            ltaApiKey, firstBusCodeData
        ))
        requests.add(service.getBusScheduleMetaObservable(
            ltaApiKey, secondBusCodeData
        ))
        requests.add(service.getBusScheduleMetaObservable(
            ltaApiKey, thirdBusCodeData
        ))

        //do zipping of the results as a whole here
        Observable
            .zip(requests) {
                // do something with those results and emit new event
                Log.d(TAG,"Retrieved value ${it[0] as BusScheduleMeta}")
                Log.d(TAG,"Retrieved value ${it[1] as BusScheduleMeta}")
                Log.d(TAG,"Retrieved value ${it[2] as BusScheduleMeta}")
                Any() // <-- Here we emit just new empty Object(), but you can emit anything
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            // Will be triggered if all requests will end successfully (4xx and 5xx also are successful requests too)
            .subscribe({
                Log.d(TAG,"All api retrieval is done $it")
                //Do something on successful completion of all requests
            }) {
                //Do something on error completion of requests
                Log.d(TAG,"Error due to $it")
            }

//        val list = arrayListOf<io.reactivex.Observable<BusStopNameAndCode>>()
//        zip(list) { args -> Arrays.asList(args) }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe {
//                val response = it[0]
//                println("Response is $response")
//                //val imageUrlResponse = imageUrlObject as ImageUrlResponse
//                //urls.add(imageUrlResponse.)}
//    }
    }
}