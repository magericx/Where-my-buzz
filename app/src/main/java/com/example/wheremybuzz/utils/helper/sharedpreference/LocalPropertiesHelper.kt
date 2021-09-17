package com.example.wheremybuzz.utils.helper.sharedpreference

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.wheremybuzz.MyApplication

object LocalPropertiesHelper {

    private val context: Context = MyApplication.instance.applicationContext
    private val ai: ApplicationInfo = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)

    val packageName: String by lazy {
        return@lazy ai.metaData["com.lta.android.package.name"] as String
    }

}