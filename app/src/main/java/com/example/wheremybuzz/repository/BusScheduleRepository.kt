package com.example.wheremybuzz.repository

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.wheremybuzz.api.ApiHelper
import com.example.wheremybuzz.model.BusScheduleMeta
import com.example.wheremybuzz.model.BusScheduleMetaRefresh
import com.example.wheremybuzz.model.callback.BusScheduleMetaCallBack
import com.example.wheremybuzz.utils.helper.network.RXDisposableManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class BusScheduleRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiHelper: ApiHelper
) {
    companion object {
        private val TAG: String = "BusScheduleRepository"
    }

    private val ltaApiKey: String = context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    ).metaData["com.lta.android.geo.LTA_KEY"] as String

    //fetch for single bus stop
    fun getBusScheduleMetaList(busStopCode: Long, callback: BusScheduleMetaCallBack) {
        val call = apiHelper.getBusScheduleMeta(ltaApiKey, busStopCode)
        call.enqueue(object : retrofit2.Callback<BusScheduleMeta> {
            override fun onResponse(
                call: Call<BusScheduleMeta>,
                response: Response<BusScheduleMeta>
            ) {
                if (response.code() == 200) {
                    val busStopCodeResponse = response.body()
                    if ((busStopCodeResponse!!.BusStopCode.isNotEmpty()) && (!busStopCodeResponse.Services.isNullOrEmpty())) {
                        Log.d(TAG, "Found bus schedules for bus stop code $busStopCode")
                        callback.updateOnResult(response.body()!!)
                    }
                }
            }

            override fun onFailure(call: Call<BusScheduleMeta>?, t: Throwable?) {
                Log.d(TAG, "Encountered error " + t?.message)
            }
        })
    }

    //fetch for all the bus stops that require refresh
    fun getBusScheduleMetaRefreshList(
        busStopMap: HashMap<String, String>,
        viewModelCallBack: (BusScheduleMetaRefresh) -> Unit
    ) {
        val retrievedBusScheduleList: MutableList<Pair<String, BusScheduleMeta>> = mutableListOf()
        val requests = ArrayList<Observable<*>>()

        //setup observable based on items that needs to be refreshed
        if (busStopMap.isNotEmpty()) {
            busStopMap.forEach { (key, value) ->
                requests.add(
                    apiHelper.getBusScheduleMetaObservable(ltaApiKey, key.toLong())
                )
            }
            //do zipping of the results as a whole here
            val disposable = Observable
                .zip(requests) {
                    // do something with those results and emit new event
                    Log.d(TAG, "Retrieved list size is ${it.size}")
                    for (i in it.indices) {
                        val busScheduleMeta = it[i] as BusScheduleMeta
                        val busStopCode = busScheduleMeta.BusStopCode
                        if (busStopMap.contains(busStopCode)) {
                            if (busStopMap[busStopCode] != null) {
                                busStopMap[busStopCode].let { busStopName ->
                                    retrievedBusScheduleList.add(
                                        i,
                                        Pair(busStopName!!, busScheduleMeta)
                                    )
                                }
                            }
                        }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                // Will be triggered if all requests will end successfully (4xx and 5xx also are successful requests too)
                .subscribe({
                    viewModelCallBack(BusScheduleMetaRefresh(retrievedBusScheduleList))
                    Log.d(TAG, "All api retrieval is done $it ")
                    //Do something on successful completion of all requests
                }) {
                    //Do something on error completion of requests
                    Log.d(TAG, "Error due to $it")
                    viewModelCallBack(BusScheduleMetaRefresh(retrievedBusScheduleList))
                }
            RXDisposableManager.add(disposable)
        }
    }

    fun destroyDisposable() {
        RXDisposableManager.dispose()
    }
}

