package com.example.wheremybuzz.utils.helper.cache

import android.content.Context
import android.util.Log
import com.example.wheremybuzz.model.BusStopsCodeResponse
import com.google.gson.Gson
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.locks.ReadWriteLock

class CacheHelper(
    private val appContext: Context,
    private val fileName: String,
    private val readWriteLock: ReadWriteLock
) {

    companion object {
        const val TAG = "CacheHelper"
    }

    fun writeJSONtoFile(busStopCodeResponse: BusStopsCodeResponse) {
        readWriteLock.writeLock().lock()
        var fos: FileOutputStream? = null
        try {
            val gson = Gson()
            //Convert the Json object to JsonString
            val jsonString: String = gson.toJson(busStopCodeResponse)
            fos = appContext.openFileOutput(fileName, Context.MODE_PRIVATE)
            fos.write(jsonString.toByteArray(Charsets.UTF_8))
            Log.d(TAG,"Writing into cache here ")
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
            fis = appContext.openFileInput(fileName)
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
            val file = appContext.getFileStreamPath(fileName)
            return !(file == null || !file.exists())
        } catch (e: IOException) {
            Log.e(TAG, "Exception while checking for file $fileName")
        }
        return false
    }


}