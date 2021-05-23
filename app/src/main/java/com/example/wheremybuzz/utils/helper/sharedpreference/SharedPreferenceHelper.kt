package com.example.wheremybuzz.utils.helper.sharedpreference

import android.R.attr.data
import android.content.SharedPreferences
import com.google.gson.Gson


class SharedPreferenceHelper(
    private val preferenceKeyName: String,
    private val sharedPreference: SharedPreferences
) {

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

    fun getSharedPreference(): String? {
        return sharedPreference.getString(this.preferenceKeyName, "")
    }

    fun appendSharedPreferenceIntoList(text: String) {
        val gson = Gson()
        sharedPreference.edit().let {
            val tempString = sharedPreference.getString(this.preferenceKeyName, "")
            //if first store
            if (tempString.isNullOrEmpty()) {
                val textList: MutableList<String> =
                    ArrayList(data)
                textList.add(0, text)
                val jsonText = gson.toJson(textList)
                it.putString(this.preferenceKeyName, jsonText)
                //if subsequent store
            } else {
                val arrayString: Array<String> = gson.fromJson(
                    tempString,
                    Array<String>::class.java
                )
                if (!arrayString.contains(text)){
                    val tempArrayList = arrayString.asList().toMutableList()
                    tempArrayList.add(tempArrayList.size,text)
                    val jsonText = gson.toJson(tempArrayList.toTypedArray())
                    it.putString(this.preferenceKeyName, jsonText)
                }
            }
            it.apply()
        }
    }

    //remove sharedpreference
    fun removeSharedPreferenceFromList(text: String){

    }

    fun overrideSharedPreference(text: String) {
        sharedPreference.edit().let {
            it.putString(this.preferenceKeyName, text)
            it.apply()
        }
    }


}