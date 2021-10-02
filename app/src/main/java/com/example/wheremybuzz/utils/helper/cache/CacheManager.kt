package com.example.wheremybuzz.utils.helper.cache

import android.content.Context
import android.util.Log
import com.example.wheremybuzz.BusApplication
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object CacheManager {
    private var context: Context = BusApplication.instance.applicationContext
    private const val TAG = "CacheWriter"
    private lateinit var cacheHelper: CacheHelper
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

