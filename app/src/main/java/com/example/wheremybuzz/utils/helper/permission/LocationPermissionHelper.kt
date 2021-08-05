package com.example.wheremybuzz.utils.helper.permission

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.wheremybuzz.LocationConstants
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.model.PermissionEnum
import com.example.wheremybuzz.ui.main.TabFragment
import java.lang.ref.WeakReference

object LocationPermissionHelper {

    const val TAG = "LocationHelper"
    const val MY_PERMISSIONS_REQUEST_LOCATION =
        LocationConstants.MY_PERMISSIONS_REQUEST_LOCATION

    private val mContext: Context = MyApplication.instance.applicationContext

    @SuppressLint("InlinedApi")
    val advancedLocationPermission: Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    val basicLocationPermission: Array<String> =
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    fun checkLocationPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    //if checkLocation permission returns false, this method will be called
    @RequiresApi(Build.VERSION_CODES.M)
    fun requestLocationPermission(requestPermissionCallback: RequestPermissionCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissionCallback.updateOnResult(RequestStatus(PermissionEnum.Advanced))
        } else {
            requestPermissionCallback.updateOnResult(RequestStatus(PermissionEnum.Basic))
        }
    }
}

interface RequestPermissionCallback {
    fun updateOnResult(requestStatus: RequestStatus)
}

data class RequestStatus(
    val permissionType: PermissionEnum
)