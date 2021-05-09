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
import com.example.wheremybuzz.utils.RXDisposableManager
import com.example.wheremybuzz.utils.helper.LtaRetrofitHelper
import retrofit2.Call
import retrofit2.Response
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.HashMap

class BusScheduleRepository {
    private val TAG: String = "BusScheduleRepository"
    private val context: Context = MyApplication.instance.applicationContext
    private val ai: ApplicationInfo = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    private val ltaApiKey: String = ai.metaData["com.lta.android.geo.LTA_KEY"] as String
    //private val disposableManager: RXDisposableManager = RXDisposableManager

    //fetch for single bus stop
    fun getBusScheduleMetaList(busStopCode: Long, viewModelCallBack: (BusScheduleMeta) -> Unit) {
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
                        viewModelCallBack(response.body()!!)
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
        val service = LtaRetrofitHelper.busScheduleApiServiceWithRx
        val requests = ArrayList<Observable<*>>()

        //setup observable based on items that needs to be refreshed
        if (busStopMap.isNotEmpty()) {
            busStopMap.forEach { (key, value) ->
                requests.add(
                    service.getBusScheduleMetaObservable(
                        ltaApiKey, key.toLong()
                    )
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
                    //Any()// <-- Here we emit just new empty Object(), but you can emit anything
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
        //disposables.add(disposable)
    }

    fun destroyDisposable() {
        RXDisposableManager.dispose()
    }
}