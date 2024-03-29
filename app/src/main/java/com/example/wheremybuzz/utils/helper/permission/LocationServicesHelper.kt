package com.example.wheremybuzz.utils.helper.permission

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.location.LocationManagerCompat
import com.example.wheremybuzz.BusApplication
import com.example.wheremybuzz.model.StatusEnum
import com.google.android.gms.location.LocationServices
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService

class LocationServicesHelper(activity: Activity) {

    companion object {
        private val TAG = "LocationServicesHelper"
    }

    private var mActivityRef: WeakReference<Activity>? = null
    private var executorService2: ExecutorService? = null
    private var applicationContext: Context? = null

    init {
        mActivityRef = WeakReference(activity)
        executorService2 = BusApplication.poolThread2
        applicationContext = BusApplication.instance.applicationContext
    }

    private val locationServices by lazy {
        return@lazy LocationServices.getFusedLocationProviderClient(mActivityRef!!.get()!!)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun checkForLastLocation(
        ILocationCallback: ILocationCallback
    ): Array<String>? {
        try {
            val locationPermission: Boolean = LocationPermissionHelper.checkLocationPermission()
            if (!locationPermission) {
                Log.d(TAG, "requestForLocation Permission here")
                return requestForLocationPermission()
            } else {
                Log.d(TAG, "requestForLastLocation Permission here")
                retrieveLastLocation(ILocationCallback)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Encountered exception due to $e")
            ILocationCallback.updateOnResult(null, StatusEnum.UnknownError)
        }
        return null
    }

    @SuppressLint("MissingPermission")
    fun retrieveLastLocation(ILocationCallback: ILocationCallback) {
        //TODO remove mocked location
        executorService2?.submit {
            locationServices.lastLocation
                .addOnSuccessListener { location: Location? ->
                    Log.d(TAG, "Passed in location here is $location")
                    location?.let {
                        //if latitude and longitude == 0.0, means location services is not granted permmission
                        Log.d(TAG, "#2 Passed in location here is $location")
                        if (it.latitude != 0.0 && it.longitude != 0.0 || checkLocationServicesEnabled()) {
                            ILocationCallback.updateOnResult(it, StatusEnum.Success)
                            return@addOnSuccessListener
                        }
                    }
                    ILocationCallback.updateOnResult(null, StatusEnum.NoPermission)
                }
                .addOnFailureListener {
                    Log.d(TAG, "Encountered exception due to $it")
                    ILocationCallback.updateOnResult(null, StatusEnum.UnknownError)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestForLocationPermission(): Array<String> {
        return LocationPermissionHelper.requestLocationPermission()
    }

    fun destroyLocationServicesHelper() {
        mActivityRef?.clear()
        applicationContext = null
    }

    private fun checkLocationServicesEnabled(): Boolean {
        val locationManager: LocationManager =
            applicationContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }
}

interface ILocationCallback {
    fun updateOnResult(location: Location?, statusEnum: StatusEnum)
}