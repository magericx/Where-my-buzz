package com.example.wheremybuzz.utils.helper.permission

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.util.Log
import com.example.wheremybuzz.model.BusScheduleMeta
import com.example.wheremybuzz.model.StatusEnum
import com.google.android.gms.location.LocationServices

class LocationServicesHelper(activity: Activity) {

    companion object {
        private val TAG = "LocationServicesHelper"
    }

    private val locationServices by lazy {
        return@lazy LocationServices.getFusedLocationProviderClient(activity)
    }

    fun checkForLastLocation(locationCallback: LocationCallback) {
        try {
            if (!LocationPermissionHelper.checkLocationPermission()) {
                locationCallback.updateOnResult(null, StatusEnum.UnknownError)
                return
            }
            locationServices.lastLocation
                .addOnSuccessListener { location: Location? ->
                    locationCallback.updateOnResult(location, StatusEnum.UnknownError)
                    // Got last known location. In some rare situations this can be null.
                }
                .addOnFailureListener {
                    locationCallback.updateOnResult(null, StatusEnum.UnknownError)
                }
        } catch (e: Exception) {
            Log.d(TAG, "Encountered exception due to $e")
            locationCallback.updateOnResult(null, StatusEnum.UnknownError)
        }
    }
}

interface LocationCallback {
    fun updateOnResult(location: Location?, statusEnum: StatusEnum)
}