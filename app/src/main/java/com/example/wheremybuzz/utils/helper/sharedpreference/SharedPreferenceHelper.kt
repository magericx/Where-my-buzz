package com.example.wheremybuzz.utils.helper.sharedpreference

import android.content.SharedPreferences

class SharedPreferenceHelper(private val preferenceKeyName: String, private val sharedPreference: SharedPreferences){

    fun getSharedPreference(): Long {
        return sharedPreference.getLong(
           this.preferenceKeyName, 0)
    }

    fun setSharedPreference() {
        val timestamp: Long = System.currentTimeMillis()
        sharedPreference.edit().let{
            it.putLong(this.preferenceKeyName, timestamp)
            it.apply()
        }
    }

}