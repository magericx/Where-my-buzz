package com.example.wheremybuzz.model

data class BusScheduleMetaRefresh(
    val servicesList: List<Pair<String,BusScheduleMeta>>
)

data class BusScheduleRefreshStatus(
    val refreshstatus: Boolean
)

data class BusStopNameAndCode(
    val busStopCode:String,
    val busStopName:String
)