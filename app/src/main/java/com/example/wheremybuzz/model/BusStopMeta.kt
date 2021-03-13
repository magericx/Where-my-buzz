package com.example.wheremybuzz.model

data class BusStopMeta(
    val BusStopMetaList: MutableList<InnerBusStopMeta?>?

)
data class InnerBusStopMeta(
    val busStopName: String,
    val latitude: Double,
    val longitude: Double,
    val busStopCode: Long
)

data class GeoLocation(
    val latitude: Double,
    val longitude: Double
)