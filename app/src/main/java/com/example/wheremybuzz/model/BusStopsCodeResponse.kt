package com.example.wheremybuzz.model

data class BusStopsCodeResponse(
    //val odata.metadata: String,
    val value: List<Value>
)

data class Value(
    val BusStopCode: String,
    val Description: String,
    val Latitude: Double,
    val Longitude: Double,
    val RoadName: String
)