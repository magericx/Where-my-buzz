package com.example.wheremybuzz.model

data class BusScheduleMetaRefresh(
    val servicesList: List<Pair<String,BusScheduleMeta>>
)

data class BusScheduleRefreshStatus(
    val refreshstatus: Boolean
)

