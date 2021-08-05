package com.example.wheremybuzz.utils.helper.permission

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.model.StatusEnum
import com.example.wheremybuzz.ui.main.TabFragment
import com.google.android.gms.location.LocationServices
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService

class LocationServicesHelper(activity: Activity) {

    companion object {
        private val TAG = "LocationServicesHelper"
    }

    private var mActivityRef: WeakReference<Activity>? = null
    private var executorService2: ExecutorService? = null

    init {
        mActivityRef = WeakReference(activity)
        executorService2 = MyApplication.poolThread2
    }

    private val locationServices by lazy {
        return@lazy LocationServices.getFusedLocationProviderClient(mActivityRef?.let { it.get() })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun checkForLastLocation(
        requestPermissionCallback: RequestPermissionCallback,
        locationCallback: LocationCallback
    ) {
        try {
            var locationPermission: Boolean
            executorService2?.submit {
                Log.d(
                    TAG,
                    "Current thread for checkLocationPermission is ${Thread.currentThread()}"
                )
                locationPermission = LocationPermissionHelper.checkLocationPermission()
                Log.d(
                    TAG,
                    "Current thread is requestForLocationPermission ${Thread.currentThread()}"
                )
                if (!locationPermission) {
                    mActivityRef?.get()?.runOnUiThread {
                        requestForLocationPermission(requestPermissionCallback)
                        return@runOnUiThread
                    }
                } else {
                    mActivityRef?.get()?.runOnUiThread {
                        retrieveLastLocation(locationCallback)
                    }
                }

            }
        } catch (e: Exception) {
            Log.d(TAG, "Encountered exception due to $e")
            locationCallback.updateOnResult(null, StatusEnum.UnknownError)
        }
    }

    @SuppressLint("MissingPermission")
    fun retrieveLastLocation(locationCallback: LocationCallback) {
        executorService2?.submit {
            locationServices.lastLocation
                .addOnSuccessListener { location: Location? ->
                    locationCallback.updateOnResult(location, StatusEnum.Success)
                }
                .addOnFailureListener {
                    Log.d(TAG, "Encountered exception due to $it")
                    locationCallback.updateOnResult(null, StatusEnum.UnknownError)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestForLocationPermission(requestPermissionCallback: RequestPermissionCallback) {
        LocationPermissionHelper.requestLocationPermission(requestPermissionCallback)
    }

    fun destroyLocationServicesHelper() {
        mActivityRef?.clear()
    }
}

interface LocationCallback {
    fun updateOnResult(location: Location?, statusEnum: StatusEnum)
}

