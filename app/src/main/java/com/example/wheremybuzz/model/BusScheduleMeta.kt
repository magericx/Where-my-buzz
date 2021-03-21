package com.example.wheremybuzz.model

data class BusScheduleMeta(
    val BusStopCode: String,
    val Services: List<Service>
    //val odata.metadata: String
)

data class Service(
    val NextBus: NextBus,
    val NextBus2: NextBus2,
    val NextBus3: NextBus3,
    val Operator: String,
    val ServiceNo: String
)

data class NextBus(
    val DestinationCode: String,
    val EstimatedArrival: String,
    val Feature: String,
    val Latitude: String,
    val Load: String,
    val Longitude: String,
    val OriginCode: String,
    val Type: String,
    val VisitNumber: String
)

data class NextBus2(
    val DestinationCode: String,
    val EstimatedArrival: String,
    val Feature: String,
    val Latitude: String,
    val Load: String,
    val Longitude: String,
    val OriginCode: String,
    val Type: String,
    val VisitNumber: String
)

data class NextBus3(
    val DestinationCode: String,
    val EstimatedArrival: String,
    val Feature: String,
    val Latitude: String,
    val Load: String,
    val Longitude: String,
    val OriginCode: String,
    val Type: String,
    val VisitNumber: String
)