package com.example.wheremybuzz.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wheremybuzz.ApiConstants
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.api.BusStopsCodeApiService
import com.example.wheremybuzz.model.BusStopCode
import com.example.wheremybuzz.model.BusStopsCodeResponse
import com.example.wheremybuzz.model.Value
import com.example.wheremybuzz.utils.CacheHelper
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.locks.ReentrantReadWriteLock

class BusStopCodeRepository {
    private val TAG: String = "BusStopCodeRepository"
    private val baseUrl: String = "http://datamall2.mytransport.sg"
    private val context: Context = MyApplication.instance.applicationContext
    private val ai: ApplicationInfo = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    private val ltaApiKey: String = ai.metaData["com.lta.android.geo.LTA_KEY"] as String
    private val cacheHelper: CacheHelper = CacheHelper()


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

    fun getBusStopCodeFromCache(
        busStopCodeTempCache: BusStopsCodeResponse?,
        busStopName: String,
        latitude: Double,
        longtitude: Double
    ): LiveData<BusStopCode>? {
        var found = false
        val data: MutableLiveData<BusStopCode> =
            MutableLiveData()
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
                        data.postValue(BusStopCode(cacheData.value[i].BusStopCode))
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
                searchForBusStopCode(data, 0, busStopName, latitude, longtitude)
            }
        } else {
            Log.d(
                TAG,
                "Temporary cache & persistent cache not available"
            )
            searchForBusStopCode(data, 0, busStopName, latitude, longtitude)
        }
        return data
    }

//    fun getBusStopCode(
//        busStopName: String,
//        latitude: Double,
//        longtitude: Double
//    ): LiveData<BusStopCode>? {
//        val data: MutableLiveData<BusStopCode> =
//            MutableLiveData()
//
//        searchForBusStopCode(data, 0, busStopName, latitude, longtitude)
//        return data
//    }


    fun searchForBusStopCode(
        observerList: MutableLiveData<BusStopCode>, skip: Int, busStopName: String,
        latitude: Double,
        longtitude: Double
    ) {
        val retrofit = getRetrofit(baseUrl)
        val service = retrofit.create(BusStopsCodeApiService::class.java)
        val call = service.getBusStopsCode(
            ltaApiKey, skip
        )
        var found = false

        if (!found && (skip == 5500)) {
            //val busStopsCodeResponse = BusStopsCodeResponse(value = listOf())
            observerList.postValue(BusStopCode(""))
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
                    val busStopCodeResponse = response.body().value
                    if (!busStopCodeResponse.isNullOrEmpty()) {
                        cacheHelper.writeJSONtoFile(response.body())
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
                                    observerList.postValue(BusStopCode(busStopCodeResponse[i].BusStopCode))
                                    Log.d(
                                        TAG,
                                        "Retrieved from cache" + cacheHelper.readJSONFile()?.value
                                    )
                                }

                            }
                            //Log.d(TAG,"Bus stop code is " + busStopCodeResponse[i].BusStopCode + " for bus stop " + busStopName)
                        }
                        if (!found) {
                            searchForBusStopCode(
                                observerList,
                                skip + 500,
                                busStopName,
                                latitude,
                                longtitude
                            )
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

    fun retrieveBusStopCodesToCache(): BusStopsCodeResponse? {
        val retrofit = getRetrofit(baseUrl)
        val service = retrofit.create(BusStopsCodeApiService::class.java)
        val busStopCodesList: MutableList<Value> = mutableListOf()
        val skip = 0
        val increment = ApiConstants.BUS_STOP_CODE_INCREMENT
        val max = ApiConstants.BUS_STOP_CODE_MAX
        var busStopCodeCache: BusStopsCodeResponse? = null

        var i = skip
        while (i < max) {
            println("Now at $i")
            val call = service.getBusStopsCode(
                ltaApiKey, i
            )
            call.enqueue(object : retrofit2.Callback<BusStopsCodeResponse> {
                override fun onResponse(
                    call: Call<BusStopsCodeResponse>,
                    response: Response<BusStopsCodeResponse>
                ) {
                    Log.d(TAG, "Status code is ${response.code()}")
                    Log.d(TAG, "Content is ${response.body()}")
                    if (response.code() == 200) {
                        val busStopCodeResponse = response.body().value
                        if (!busStopCodeResponse.isNullOrEmpty()) {
                            //writeJSONtoFile(response.body())
                            busStopCodesList.addAll(busStopCodeResponse)
                        }
                        if (i == max) {
                            Log.d(TAG, "Write to cache")
                            cacheHelper.writeJSONtoFile(BusStopsCodeResponse(busStopCodesList))
                            busStopCodeCache = BusStopsCodeResponse(busStopCodesList)
                        }
                    } else {
                        Log.d(TAG, "Status code is " + response.code())
                        Log.d(TAG, "Message is " + response.message())
                        Log.d(TAG, "Body is " + response.body())

                    }
                }

                override fun onFailure(call: Call<BusStopsCodeResponse>, t: Throwable) {
                    Log.d(TAG, "Encountered error " + t.message)
                }

            })
            i += increment
        }
        return busStopCodeCache
    }
}