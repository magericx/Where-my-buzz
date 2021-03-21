package com.example.wheremybuzz.model

data class FinalBusMeta(
    val BusStopCode: String,
    val Geolocation: GeoLocation,
    val Services: List<Service>
)