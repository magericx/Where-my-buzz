package com.example.wheremybuzz.model.callback

import com.example.wheremybuzz.model.BusScheduleMeta

interface BusScheduleMetaCallBack{
    fun updateOnResult(busScheduleMeta: BusScheduleMeta)
}