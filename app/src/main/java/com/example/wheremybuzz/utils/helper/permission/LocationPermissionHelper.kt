package com.example.wheremybuzz.utils.helper.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.wheremybuzz.LocationConstants
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.ui.main.TabFragment
import java.lang.ref.WeakReference

object LocationPermissionHelper {

    const val TAG = "LocationHelper"
    private const val MY_PERMISSIONS_REQUEST_LOCATION = LocationConstants.MY_PERMISSIONS_REQUEST_LOCATION

    private val mContext: Context = MyApplication.instance.applicationContext

    @SuppressLint("InlinedApi")
    private val advancedLocationPermission: Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    private val basicLocationPermission: Array<String> =
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
    fun requestLocationPermission(tabFragment: TabFragment) {
        val mFragmentRef: WeakReference<TabFragment> = WeakReference(tabFragment)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mFragmentRef.get()?.requestPermissions(
                advancedLocationPermission,
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            mFragmentRef.get()?.requestPermissions(
                basicLocationPermission,
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }
}