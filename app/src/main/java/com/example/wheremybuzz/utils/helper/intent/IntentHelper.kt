package com.example.wheremybuzz.utils.helper.intent

import android.content.Intent
import android.provider.Settings

object IntentHelper {

    fun locationPermissionSettings(): Intent{
        return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }
}