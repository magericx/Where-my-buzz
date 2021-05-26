package com.example.wheremybuzz

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MyApplication : Application(), Application.ActivityLifecycleCallbacks {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        Log.d(TAG, "Created instance of MyApplication here")
        instance = this
        poolThread = Executors.newFixedThreadPool(4)
        mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper())
    }
    companion object {
        val TAG = "MyApplication"
        lateinit var instance: MyApplication
            private set
        lateinit var poolThread: ExecutorService
            private set
        lateinit var mainThreadHandler: Handler
            private set
    }

    override fun onActivityPaused(p0: Activity) {

    }

    override fun onActivityStarted(p0: Activity) {

    }

    override fun onActivityDestroyed(p0: Activity) {
        if (!poolThread.isTerminated || !poolThread.isShutdown){
            Log.d(TAG,"Shutdown threads here")
            poolThread.shutdown()
            poolThread.shutdownNow()
        }
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

    }

    override fun onActivityStopped(p0: Activity) {

    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        Log.d(TAG,"Called application onActivityCreated here")
//        if (poolThread.isShutdown || poolThread.isTerminated){
//            poolThread = Executors.newFixedThreadPool(4)
//        }
    }

    override fun onActivityResumed(p0: Activity) {
        Log.d(TAG,"Called application onActivityResumed here")
        if (poolThread.isShutdown || poolThread.isTerminated){
            poolThread = Executors.newFixedThreadPool(4)
        }
    }
}