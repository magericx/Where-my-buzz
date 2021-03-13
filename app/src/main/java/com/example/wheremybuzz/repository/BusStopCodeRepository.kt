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
    private val readWriteLock = ReentrantReadWriteLock()
    private val fileName = "busStopCode.cache"

    val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder();
    val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor();

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

    //write whenever there is data from server
    private fun writeJSONtoFile(busStopCodeResponse: BusStopsCodeResponse) {

        readWriteLock.writeLock().lock()
        var fos: FileOutputStream? = null
        try {
            //Create a Object of Post
            val post = busStopCodeResponse
            //Create a Object of Gson
            val gson = Gson()
            //Convert the Json object to JsonString
            val jsonString: String = gson.toJson(post)
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            fos.write(jsonString.toByteArray(Charsets.UTF_8))
        } catch (e: IOException) {
            Log.e(TAG, "saveCache: $e")
        } finally {
            readWriteLock.writeLock().unlock()
            try {
                fos?.close()
            } catch (e: Exception) {

            }
        }
    }

    //this will read JSON file as a whole
    private fun readJSONFile(): BusStopsCodeResponse? {
        readWriteLock.readLock().lock()
        var fis: FileInputStream? = null
        return try {
            fis = context.openFileInput(fileName)
            val length = fis.available()
            val data = ByteArray(length)
            fis.read(data)
            val gson = Gson()
            return gson.fromJson(String(data), BusStopsCodeResponse::class.java)
        } catch (e: Exception) {
            null
            //Log.e(TAG, "readCache: $e")
        } finally {
            readWriteLock.readLock().unlock()
            try {
                fis?.close()
            } catch (e: Exception) {

            }
        }
    }

    fun getBusStopCodeFromCache(
        busStopName: String,
        latitude: Double,
        longtitude: Double
    ) : LiveData<BusStopCode>? {
        val data: MutableLiveData<BusStopCode> =
            MutableLiveData()
        val cacheData = readJSONFile()
        if (cacheData != null) {
            for (i in cacheData.value.indices){
                if (cacheData.value[i].Description == busStopName){
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
                    }
                }
            }
        }
        return data


    }

    fun getBusStopCode(
        busStopName: String,
        latitude: Double,
        longtitude: Double
    ): LiveData<BusStopCode>? {
        val data: MutableLiveData<BusStopCode> =
            MutableLiveData()

        searchForBusStopCode(data, 0, busStopName, latitude, longtitude)
        return data
    }


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
                        writeJSONtoFile(response.body())
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
                                    Log.d(TAG, "Retrieved from cache" + readJSONFile()?.value)
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

    fun retrieveBusStopCodesToCache() {
        val retrofit = getRetrofit(baseUrl)
        val service = retrofit.create(BusStopsCodeApiService::class.java)
        val busStopCodesList: MutableList<Value> = mutableListOf()
        val skip = 0
        val increment = ApiConstants.BUS_STOP_CODE_INCREMENT
        val max = ApiConstants.BUS_STOP_CODE_MAX

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
                            writeJSONtoFile(BusStopsCodeResponse(busStopCodesList))
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
    }

    fun cacheExists(): Boolean {
        try {
            val file = context.getFileStreamPath(fileName)
            return !(file == null || !file.exists())
        } catch (e: IOException) {
            Log.e(TAG, "Exception while checking for file $fileName")
        }
        return false
    }
}