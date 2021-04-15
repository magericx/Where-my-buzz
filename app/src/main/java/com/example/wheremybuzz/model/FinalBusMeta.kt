package com.example.wheremybuzz.model

data class StoredBusMeta(
    var BusStopCode: String,
    val Geolocation: GeoLocation,
    var Services: Service?
)
