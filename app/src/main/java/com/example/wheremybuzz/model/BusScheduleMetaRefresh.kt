package com.example.wheremybuzz.model

data class BusScheduleMetaRefresh(
    val ServicesList: List<Pair<String,BusScheduleMeta>>
)

data class BusScheduleRefreshStatus(
    val Refreshstatus: Boolean
)

data class BusStopNameAndCode(
    val busStopCode:String,
    val busStopName:String
)