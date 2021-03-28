package com.example.wheremybuzz.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.utils.helper.SharedPreferenceHelper


object SharedPreferenceManager {
    private val context: Context = MyApplication.instance.applicationContext
    private const val preferenceFilename = "busStopCodesConfig"
    private const val preferenceKeyName = "lastFetchedCache"
    private val sharedPreferences: SharedPreferences by lazy {
        return@lazy context.getSharedPreferences(
            preferenceFilename, Context.MODE_PRIVATE
        )
    }
    val getSharedPreferenceHelper: SharedPreferenceHelper by lazy {
        return@lazy SharedPreferenceHelper(preferenceKeyName, sharedPreferences)
    }
}