package com.example.wheremybuzz.utils.helper.network

import android.content.Context
import android.net.ConnectivityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkUtil @Inject constructor(@ApplicationContext private val context: Context) {

    private var isEnabled = false

    fun getNetworkConnection(cache: Boolean = true): Boolean {
        if (cache){
            return isEnabled
        }
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                isEnabled = true
            } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                // connected to mobile data
                isEnabled = true
            }
        } else {
            // not connected to the internet
            isEnabled = false
        }
        return isEnabled
    }
}