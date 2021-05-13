package com.example.wheremybuzz.utils.helper.time

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimeUtil {
    private const val threeDays = 3 * 24 * 60 * 60 * 1000
    private const val minutes = 60 * 1000
    private const val arrive = "ARR"
    private const val TAG = "TimeUtil"

    fun checkTimeStampExceed3days(searchTimestamp: Long): Boolean {
        val currentTimeStamp: Long = System.currentTimeMillis()
        return ((currentTimeStamp - searchTimestamp) > threeDays)
    }

    //@SuppressLint("SimpleDateFormat")
    //Convert time into milliseconds
    private fun convertStringToMillis(timestamp: String): Long {
        //"2021-04-05T22:36:06+08:00"
        val trimmedDateFormat = timestamp.split("+")[0]
        val trimmedZone = timestamp.split("+")[1]
        //Log.d(TAG,"Retrieved datetime format is $timestamp")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+$trimmedZone")
        val date: Date =
            dateFormat.parse(trimmedDateFormat) //You will get date object relative to server/client timezone wherever it is parsed
        //Log.d(TAG,"Retrieved converted datetime format is $date")
        //Log.d(TAG, "Retrieve milliseconds is $milliseconds")
        return date.time
    }

    fun retrieveDifferenceFromNow(timestamp: String): String {
        if (timestamp.isEmpty()) {
            return ""
        }
        val convertedMillis =
            convertStringToMillis(
                timestamp
            )
        return calculateDifference(
            convertedMillis
        )
    }

    //Imlementation to calculate time difference
    private fun calculateDifference(comparisonTime: Long): String {
        val currentTime =
            getCurrentTimeStamp()
        if (currentTime > comparisonTime) {
            return arrive
        }
        val differenceMillis = comparisonTime - currentTime
        val differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(differenceMillis)
        //Log.d(TAG,"Comparison time is $comparisonTime and current time is $currentTime")
        //Log.d(TAG, "Difference in Millis is $differenceInMinutes")
        return differenceInMinutes.toString()
    }

    private fun getCurrentTimeStamp(): Long {
        return System.currentTimeMillis()
    }


}