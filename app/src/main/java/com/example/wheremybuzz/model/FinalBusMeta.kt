package com.example.wheremybuzz.model

data class FinalBusMeta(
    var BusStopCode: String,
    val Geolocation: GeoLocation,
    var Services: List<Service>
)