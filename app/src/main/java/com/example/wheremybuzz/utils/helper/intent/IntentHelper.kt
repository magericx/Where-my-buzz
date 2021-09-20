package com.example.wheremybuzz.utils.helper.intent

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.example.wheremybuzz.utils.helper.sharedpreference.LocalPropertiesHelper

object IntentHelper {

    var navigateToAppSettings: Boolean = false
    val TAG = "IntentHelper"

    fun setNavigationToExternalIntent() {
        navigateToAppSettings = navigateToAppSettings.not()
    }

    //To navigate to location services in android settings
    fun locationServicesPermissionSettings(): Intent {
        setNavigationToExternalIntent()
        return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }

    //To navigate to location app permission in android settings
    fun locationAppPermissionSettings(): Intent {
        setNavigationToExternalIntent()
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", LocalPropertiesHelper.packageName, null)
        }
    }
}