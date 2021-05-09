package com.example.wheremybuzz.utils

import android.content.Context
import android.util.Log
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.model.BusStopsCodeResponse
import com.example.wheremybuzz.utils.helper.CacheHelper
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object CacheManager {
    private var context: Context = MyApplication.instance.applicationContext
    private const val TAG = "CacheWriter"
    private lateinit var cacheHelper: CacheHelper
    //private var cacheHelper: CacheHelper? = null
    private const val fileName: String = "busStopCode.cache"

    val initializeCacheHelper: CacheHelper? by lazy {
        cacheHelper = CacheHelper(
            context,
            fileName,
            readWriteLock
        )
        Log.d(TAG, "Instance of cacheHelper is $cacheHelper")
        return@lazy cacheHelper
    }

    private val readWriteLock: ReadWriteLock by lazy {
        return@lazy ReentrantReadWriteLock()
    }
}

