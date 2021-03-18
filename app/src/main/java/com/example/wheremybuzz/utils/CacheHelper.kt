package com.example.wheremybuzz.utils

import android.content.Context
import android.util.Log
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.model.BusStopsCodeResponse
import com.google.gson.Gson
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.locks.ReentrantReadWriteLock

class CacheHelper {
    private val context: Context = MyApplication.instance.applicationContext
    private val readWriteLock = ReentrantReadWriteLock()
    private val TAG = "CacheWriter"

    fun writeJSONtoFile(busStopCodeResponse: BusStopsCodeResponse) {
        readWriteLock.writeLock().lock()
        var fos: FileOutputStream? = null
        try {
            //Create a Object of Post
            val post = busStopCodeResponse
            //Create a Object of Gson
            val gson = Gson()
            //Convert the Json object to JsonString
            val jsonString: String = gson.toJson(post)
            fos = context.openFileOutput(Companion.fileName, Context.MODE_PRIVATE)
            fos.write(jsonString.toByteArray(Charsets.UTF_8))
        } catch (e: IOException) {
            Log.e(TAG, "saveCache: $e")
        } finally {
            readWriteLock.writeLock().unlock()
            try {
                fos?.close()
            } catch (e: Exception) {

            }
        }
    }

    //this will read JSON file as a whole
    fun readJSONFile(): BusStopsCodeResponse? {
        readWriteLock.readLock().lock()
        var fis: FileInputStream? = null
        return try {
            fis = context.openFileInput(fileName)
            val length = fis.available()
            val data = ByteArray(length)
            fis.read(data)
            val gson = Gson()
            return gson.fromJson(String(data), BusStopsCodeResponse::class.java)
        } catch (e: Exception) {
            null
            //Log.e(TAG, "readCache: $e")
        } finally {
            readWriteLock.readLock().unlock()
            try {
                fis?.close()
            } catch (e: Exception) {
            }
        }
    }

    fun cacheExists(): Boolean {
        try {
            val file = context.getFileStreamPath(fileName)
            return !(file == null || !file.exists())
        } catch (e: IOException) {
            Log.e(TAG, "Exception while checking for file $fileName")
        }
        return false
    }

    companion object {
        private const val fileName = "busStopCode.cache"
    }
}