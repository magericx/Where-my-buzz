package com.example.wheremybuzz.utils.helper.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.wheremybuzz.MyApplication
import com.google.android.gms.location.LocationServices

object LocationPermissionHelper {

    const val TAG = "LocationHelper"

    private val mContext: Context = MyApplication.instance.applicationContext
    //private val locationServicesHelper: LocationServicesHelper? = null

    fun checkLocationPermission(): Boolean{
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //permission not granted here
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG,"Failed to get permission here")
            return false
        }
        return true
    }


}