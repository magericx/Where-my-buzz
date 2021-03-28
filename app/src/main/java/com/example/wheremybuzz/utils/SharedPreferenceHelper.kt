package com.example.wheremybuzz.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.wheremybuzz.MyApplication


object SharedPreferenceHelper {
    private val context: Context = MyApplication.instance.applicationContext
    private const val preferenceFilename = "busStopCodesConfig"
    private const val preferenceKeyName = "lastFetchedCache"
    private val sharedPreferences: SharedPreferences by lazy {
        return@lazy context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
    }

    fun getSharedPreference(): Long {
        return sharedPreferences.getLong(preferenceKeyName, 0)
    }

    fun setSharedPreference() {
        val timestamp: Long = System.currentTimeMillis()
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putLong(preferenceKeyName, timestamp)
        editor.apply()
    }
}