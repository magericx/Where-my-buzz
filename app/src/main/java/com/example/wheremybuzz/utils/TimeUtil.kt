package com.example.wheremybuzz.utils

import android.annotation.SuppressLint
import android.icu.text.StringPrepParseException
import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {
    private const val threeDays = 3*24*60*60*1000

    fun checkTimeStampExceed3days(searchTimestamp:Long): Boolean{
        val currentTimeStamp : Long = System.currentTimeMillis()
        return ((currentTimeStamp - searchTimestamp) > threeDays)
    }

    //@SuppressLint("SimpleDateFormat")
    private fun convertToTime(timestamp: String) : String{
        //"2021-04-05T22:36:06+08:00"
        val trimmedDateFormat = timestamp.split("+")[0]
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val date: Date =
            dateFormat.parse(trimmedDateFormat) //You will get date object relative to server/client timezone wherever it is parsed
        val formatter =
            SimpleDateFormat("HH-mm-ss") //If you need time just put specific format for time like 'HH:mm:ss'
        return formatter.format(date)
    }

    fun retrieveDifferenceFromNow(timestamp: String) : String{
        return convertToTime(timestamp)
    }

    //TODO add method to calculate difference from now
    private fun calculateDifference(): String{
        return ""
    }



}