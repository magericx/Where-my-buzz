package com.example.wheremybuzz

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.core.os.HandlerCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        poolThread = Executors.newFixedThreadPool(4)
        mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper())
    }

    companion object {
        lateinit var instance: MyApplication
            private set
        lateinit var poolThread: ExecutorService
            private set
        lateinit var mainThreadHandler: Handler
            private set
    }
}