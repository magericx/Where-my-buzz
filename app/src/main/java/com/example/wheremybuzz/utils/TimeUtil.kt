package com.example.wheremybuzz.utils

class TimeUtil {

    fun checkTimeStampExceed3days(searchTimestamp:Long): Boolean{
        val currentTimeStamp : Long = System.currentTimeMillis()
        return ((currentTimeStamp - searchTimestamp) > threeDays)
    }

    companion object {
        private const val threeDays = 3*24*60*60*1000
    }
}