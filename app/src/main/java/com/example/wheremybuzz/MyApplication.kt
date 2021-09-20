package com.example.wheremybuzz

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@HiltAndroidApp
class MyApplication : Application(), Application.ActivityLifecycleCallbacks {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        instance = this
        poolThread = Executors.newFixedThreadPool(4)
        poolThread2 = Executors.newFixedThreadPool(6)
        mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper())
    }
    companion object {
        const val TAG = "MyApplication"
        lateinit var instance: MyApplication
            private set
        lateinit var poolThread: ExecutorService
            private set
        lateinit var mainThreadHandler: Handler
            private set
        lateinit var poolThread2: ExecutorService
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
        if (!poolThread2.isTerminated || !poolThread2.isShutdown){
            poolThread2.shutdown()
            poolThread2.shutdownNow()
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
        if (poolThread2.isShutdown || poolThread2.isTerminated){
            poolThread2 = Executors.newFixedThreadPool(4)
        }
    }
}