package com.example.wheremybuzz.utils.helper.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import com.example.wheremybuzz.BusApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class SharedPreferenceManager @Inject constructor(private val context: Context) {

    companion object {
        private const val preferenceFilename = "busStopCodesConfig"
        private const val preferenceKeyName = "lastFetchedCache"
        private const val favouritePreferenceFilename = "favouriteBusStopsConfig"
        private const val favouritePreferenceKeyName = "favouriteBusStops"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        return@lazy context.getSharedPreferences(
            preferenceFilename, Context.MODE_PRIVATE
        )
    }
    val getSharedPreferenceHelper: SharedPreferenceHelper by lazy {
        return@lazy SharedPreferenceHelper(
            preferenceKeyName,
            sharedPreferences
        )
    }

    private val favouriteSharedPreferences: SharedPreferences by lazy {
        return@lazy context.getSharedPreferences(
            favouritePreferenceFilename, Context.MODE_PRIVATE
        )
    }
    val getFavouriteSharedPreferenceHelper: SharedPreferenceHelper by lazy {
        return@lazy SharedPreferenceHelper(
            favouritePreferenceKeyName,
            favouriteSharedPreferences
        )
    }
}