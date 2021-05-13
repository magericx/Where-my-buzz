package com.example.wheremybuzz.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.example.wheremybuzz.ApiConstants
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.model.BusStopCode
import com.example.wheremybuzz.model.BusStopsCodeResponse
import com.example.wheremybuzz.model.Value
import com.example.wheremybuzz.utils.helper.cache.CacheManager
import com.example.wheremybuzz.utils.helper.network.RXDisposableManager
import com.example.wheremybuzz.utils.helper.cache.CacheHelper
import com.example.wheremybuzz.utils.helper.retrofit.LtaRetrofitHelper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Response
import java.util.*

class BusStopCodeRepository {
    companion object {
        private val TAG: String = "BusStopCodeRepository"
        private val context: Context = MyApplication.instance.applicationContext
        private val ai: ApplicationInfo = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        private val ltaApiKey: String = ai.metaData["com.lta.android.geo.LTA_KEY"] as String
    }

    var cacheHelper: CacheHelper = CacheManager.initializeCacheHelper!!

    //retrieve busStopCode from cache
    fun getBusStopCodeFromCache(
        busStopCodeTempCache: BusStopsCodeResponse?,
        busStopName: String,
        latitude: Double,
        longtitude: Double,
        viewModelCallBack: (BusStopCode) -> Unit
    ) {
        var found = false
//        val data: MutableLiveData<BusStopCode> =
//            MutableLiveData()
        val cacheData: BusStopsCodeResponse? = busStopCodeTempCache ?: cacheHelper.readJSONFile()
        if (cacheData != null) {
            for (i in cacheData.value.indices) {
                if (cacheData.value[i].Description == busStopName) {
                    if (String.format(
                            "%.5f",
                            cacheData.value[i].Latitude
                        ).toDouble().equals(latitude) && String.format(
                            "%.5f",
                            cacheData.value[i].Longitude
                        ).toDouble().equals(longtitude)
                    ) {
                        Log.d(
                            TAG,
                            "Found bus stop code is " + cacheData.value[i].BusStopCode + " for bus stop " + busStopName
                        )
                        //data.postValue(BusStopCode(cacheData.value[i].BusStopCode))
                        viewModelCallBack(BusStopCode(cacheData.value[i].BusStopCode))
                        found = true
                        break
                    }
                }
            }
            if (!found) {
                Log.d(
                    TAG,
                    "Bus stop not found in temporary cache and persistent cache, calling API now to retrieve"
                )
                searchForBusStopCode(0, busStopName, latitude, longtitude) {

                }
            }
        } else {
            Log.d(
                TAG,
                "Temporary cache & persistent cache not available"
            )
            searchForBusStopCode(0, busStopName, latitude, longtitude) {

            }
        }
    }

    fun searchForBusStopCode(
        skip: Int, busStopName: String,
        latitude: Double,
        longtitude: Double,
        viewModelCallBack: (BusStopCode) -> Unit
    ) {
        val service = LtaRetrofitHelper.busStopsCodeApiService
        val call = service.getBusStopsCode(
            ltaApiKey, skip
        )
        var found = false

        if (!found && (skip == 5500)) {
            viewModelCallBack(BusStopCode(""))
            //observerList.postValue(BusStopCode(""))
        }

        call.enqueue(object : retrofit2.Callback<BusStopsCodeResponse> {
            override fun onResponse(
                call: Call<BusStopsCodeResponse>,
                response: Response<BusStopsCodeResponse>
            ) {
                Log.d(TAG, "Status code is ${response.code()}")
                Log.d(TAG, "Content is ${response.body()}")
                Log.d(TAG, "Provided latitude is $latitude")
                Log.d(TAG, "Provided longtitude is $longtitude")

                if (response.code() == 200) {
                    val busStopCodeResponse = response.body()?.value
                    if (!busStopCodeResponse.isNullOrEmpty()) {
                        cacheHelper.writeJSONtoFile(response.body()!!)
                        for (i in busStopCodeResponse.indices) {
                            //add internal logic to check and iterate
                            if (busStopCodeResponse[i].Description == busStopName) {
                                if (String.format(
                                        "%.5f",
                                        busStopCodeResponse[i].Latitude
                                    ).toDouble().equals(latitude) && String.format(
                                        "%.5f",
                                        busStopCodeResponse[i].Longitude
                                    ).toDouble().equals(longtitude)
                                ) {
                                    found = true
                                    Log.d(
                                        TAG,
                                        "Found bus stop code is " + busStopCodeResponse[i].BusStopCode + " for bus stop " + busStopName
                                    )
                                    //observerList.postValue(BusStopCode(busStopCodeResponse[i].BusStopCode))
                                    viewModelCallBack(BusStopCode(busStopCodeResponse[i].BusStopCode))
                                    Log.d(
                                        TAG,
                                        "Retrieved from cache " + cacheHelper.readJSONFile()?.value
                                    )
                                }

                            }
                            //Log.d(TAG,"Bus stop code is " + busStopCodeResponse[i].BusStopCode + " for bus stop " + busStopName)
                        }
                        if (!found) {
                            searchForBusStopCode(
                                skip + 500,
                                busStopName,
                                latitude,
                                longtitude
                            ) {

                            }
                        }
                    }
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
    }

    //retrieve bus stop code from API and store into cache file
    fun retrieveBusStopCodesToCache(viewModelCallBack: (BusStopsCodeResponse) -> Unit) {
        val service = LtaRetrofitHelper.busStopsCodeApiServiceWithRx
        val busStopCodesList: MutableList<Value> = mutableListOf()
        val skip = 0
        val increment = ApiConstants.BUS_STOP_CODE_INCREMENT
        val max = ApiConstants.BUS_STOP_CODE_MAX
        //var busStopCodeCache: BusStopsCodeResponse? = null
        val requests = ArrayList<Observable<*>>()

        for (i in skip..max step increment) {
            requests.add(
                service.getBusStopsCodeObservable(
                    ltaApiKey, i
                )
            )
        }
        val disposable = Observable
            .zip(requests) {
                for (i in it.indices) {
                    val busStopCodeResponse = (it[i] as BusStopsCodeResponse).value
                    busStopCodesList.addAll(busStopCodeResponse)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                cacheHelper.writeJSONtoFile(BusStopsCodeResponse(busStopCodesList))
                //busStopCodeCache = BusStopsCodeResponse(busStopCodesList)
                viewModelCallBack(BusStopsCodeResponse(busStopCodesList))
            }) {
                Log.d(TAG, "Error occured due to $it")
            }
        RXDisposableManager.add(disposable)
    }
}