package com.example.wheremybuzz.utils.helper.network

import android.content.Context
import android.net.ConnectivityManager
import com.example.wheremybuzz.MyApplication

object NetworkUtil {
    private val context: Context = MyApplication.instance.applicationContext
    fun haveNetworkConnection(): Boolean {
        var connected = false
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                connected = true
            } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                // connected to mobile data
                connected = true
            }
        } else {
            // not connected to the internet
            connected = false
        }
        return connected
    }
}