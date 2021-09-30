package com.example.wheremybuzz.utils.helper.sharedpreference

import android.R.attr.data
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class SharedPreferenceHelper(
    private val preferenceKeyName: String,
    private val sharedPreference: SharedPreferences
) {

    companion object{
        const val TAG = "SharedPreferenceHelper"
    }
    private val gsonInstance by lazy {
        return@lazy Gson()
    }

    fun getTimeSharedPreference(): Long {
        return sharedPreference.getLong(
            this.preferenceKeyName, 0
        )
    }

    fun setTimeSharedPreference() {
        val timestamp: Long = System.currentTimeMillis()
        sharedPreference.edit().let {
            it.putLong(this.preferenceKeyName, timestamp)
            it.apply()
        }
    }

    fun getSharedPreferenceAsMap(): Map<String, String>? {
        val tempString = sharedPreference.getString(this.preferenceKeyName, "")
        return tempString?.let { convertToMap(it) }
    }

    fun appendSharedPreferenceIntoList(busStopName: String, busStopCode: String) {
        sharedPreference.edit().let {
            val tempString = sharedPreference.getString(this.preferenceKeyName, "")
            //if first store
            if (tempString.isNullOrEmpty()) {
                val mutableMap: Map<String, String> = mapOf(busStopCode to busStopName)
                val jsonText = gsonInstance.toJson(mutableMap)
                it.putString(this.preferenceKeyName, jsonText)
                //if subsequent store
            } else {
                val mapString: Map<String, String> = convertToMap(tempString)
                if (!mapString.containsKey(busStopCode)) {
                    val mutableMap = mapString.toMutableMap()
                    mutableMap[busStopCode] = busStopName
                    val jsonText = gsonInstance.toJson(mutableMap)
                    it.putString(this.preferenceKeyName, jsonText)
                }
            }
            it.commit()
        }
    }

    //remove sharedpreference
    fun removeSharedPreferenceFromList(text: String) {
        val tempString = sharedPreference.getString(this.preferenceKeyName, "")
        if (!tempString.isNullOrEmpty()) {
            val mapString: Map<String, String> = convertToMap(tempString)
            if (mapString.containsKey(text)) {
                val mutableMapString = mapString.toMutableMap()
                mutableMapString.remove(text)
                overrideSharedPreference(mutableMapString.toMap())
            }
        }
    }

    private fun overrideSharedPreference(newMap: Map<String,String>) {
        sharedPreference.edit().let {
            val jsonText = gsonInstance.toJson(newMap)
            it.putString(this.preferenceKeyName, jsonText)
            it.commit()
        }
    }

    fun checkIfExistsInList(text: String): Boolean {
        val tempString = sharedPreference.getString(this.preferenceKeyName, "")
        if (!tempString.isNullOrEmpty()) {
            val map = convertToMap(tempString)
            return map.containsKey(text)
        }
        return false
    }

    fun checkIfListIsEmpty(): Boolean {
        val tempString = sharedPreference.getString(this.preferenceKeyName, "") ?: return true
        if (tempString.isEmpty()) return true
        tempString.let{
            val mapString: Map<String, String> = convertToMap(it)
            return mapString.isEmpty()
        }
    }

    private fun convertToMap(tempString: String): Map<String, String> {
        val mapType: Type = object :
            TypeToken<Map<String, String?>?>() {}.type
        return gsonInstance.fromJson(tempString, mapType)
    }
}