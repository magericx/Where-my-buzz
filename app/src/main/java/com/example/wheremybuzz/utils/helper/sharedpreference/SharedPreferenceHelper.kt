package com.example.wheremybuzz.utils.helper.sharedpreference

import android.content.SharedPreferences

class SharedPreferenceHelper(private val preferenceKeyName: String, private val sharedPreference: SharedPreferences){

    fun getTimeSharedPreference(): Long {
        return sharedPreference.getLong(
           this.preferenceKeyName, 0)
    }

    fun setTimeSharedPreference() {
        val timestamp: Long = System.currentTimeMillis()
        sharedPreference.edit().let{
            it.putLong(this.preferenceKeyName, timestamp)
            it.apply()
        }
    }

    fun getSharedPreference(): String? {
        return sharedPreference.getString(this.preferenceKeyName,"")
    }

    fun appendSharedPreference(text: String) {
        sharedPreference.edit().let{
            val tempString = sharedPreference.getString(this.preferenceKeyName,"")
            if (tempString == ""){
                it.putString(this.preferenceKeyName,text)
            }else{
                it.putString(this.preferenceKeyName,"$tempString,$text")
            }
            it.apply()
        }
    }

    fun overrideSharedPreference(text: String){
        sharedPreference.edit().let{
            it.putString(this.preferenceKeyName,text)
            it.apply()
        }
    }


}