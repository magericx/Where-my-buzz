package com.example.wheremybuzz

import android.app.Application
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        poolThread = Executors.newFixedThreadPool(4)
    }

    companion object {
        lateinit var instance: MyApplication
            private set
        lateinit var poolThread: ExecutorService
            private set
    }
}