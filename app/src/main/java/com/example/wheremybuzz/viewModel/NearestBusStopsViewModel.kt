package com.example.wheremybuzz.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.wheremybuzz.model.*
import com.example.wheremybuzz.repository.BusScheduleRepository
import com.example.wheremybuzz.repository.BusStopCodeRepository
import com.example.wheremybuzz.repository.NearestBusRepository
import java.util.HashMap


class NearestBusStopsViewModel(application: Application) : AndroidViewModel(application) {
    //private var nearestBusStopsListObservable: LiveData<List<NearestBusStopsResponse>>? = null
    private var nearestBusStopsGeoListObservable: LiveData<BusStopMeta>? = null
    private var busStopCodeListObservable: LiveData<BusStopCode>? = null
    private var busScheduleListObservable: LiveData<BusScheduleMeta>? = null
    private val TAG = "NearestBusStopsView"

    private var expandableListDetail: HashMap<String, List<FinalBusMeta>>

    //    private var expandableListDetail: HashMap<String, FinalBusMeta>
    private var busStopCodeTempCache: BusStopsCodeResponse? = null

    var nearestBusRepository: NearestBusRepository? = null
    var busStopCodeRepository: BusStopCodeRepository? = null
    var busScheduleRepository: BusScheduleRepository? = null

    init {
        // If any transformation is needed, this can be simply done by Transformations class ...
//            projectListObservable = NearestBusRepository
        nearestBusRepository = NearestBusRepository()
        busStopCodeRepository = BusStopCodeRepository()
        busScheduleRepository = BusScheduleRepository()
        expandableListDetail = HashMap()
    }

    fun getExpandableListDetail(): HashMap<String, List<FinalBusMeta>> {
        return expandableListDetail
    }

//    fun getExpandableListDetail(): HashMap<String, FinalBusMeta> {
//        return expandableListDetail
//    }

    fun setExpandableListDetail(key: String, list: List<FinalBusMeta>) {
        expandableListDetail[key] = list
    }

//    fun setExpandableListDetail(key: String, finalBusMeta: FinalBusMeta) {
//        expandableListDetail[key] = finalBusMeta
//    }

    //TODO Add implementations for busStopCode
    fun setBusStopCodeInExpendableListDetail(key: String, busStopCode: BusStopCode) {
        if (expandableListDetail.containsKey(key)){

        }

    }

    //TODO Add implementation for services
    fun setServicesInExpendableListDetail(/*key: String, list: List<Service>*/) {
        Log.d(TAG, "expandableListDetails is $expandableListDetail")
        //       if (expandableListDetail.containsKey(key)) {
//            val oldExpandableListDetail = expandableListDetail[key]
//            val List<FinalBusMeta>
//            val newExpendableListDetail =
//                expandableListDetail.replace(key,)
        //    }
    }

    fun getGeoLocationBasedOnBusStopName(busStopName: String): GeoLocation {
        return GeoLocation(
            expandableListDetail[busStopName]!![0].Geolocation.latitude,
            expandableListDetail[busStopName]!![0].Geolocation.longitude
//            expandableListDetail[busStopName]!!.Geolocation.latitude,
//            expandableListDetail[busStopName]!!.Geolocation.longitude
        )
    }

    //TODO add new method to set data that are newly retrieved

    /**
     * Expose the LiveData Projects query so the UI can observe it.
     */
//    fun getNearestBusStopsListObservable(): LiveData<List<NearestBusStopsResponse>>? {
//        nearestBusStopsListObservable = mRepository!!.getNearestBusStops()
//        return nearestBusStopsListObservable
//    }

    fun getNearestBusStopsGeoListObservable(location: String): LiveData<BusStopMeta>? {
        nearestBusStopsGeoListObservable = nearestBusRepository!!.getNearestBusStops(location)
        return nearestBusStopsGeoListObservable
    }

    fun getBusStopCodeListObservable(
        busStopName: String,
        latitude: Double,
        longtitude: Double
    ): LiveData<BusStopCode>? {
        busStopCodeListObservable =
            busStopCodeRepository!!.getBusStopCodeFromCache(
                busStopCodeTempCache,
                busStopName,
                latitude,
                longtitude
            )
        return busStopCodeListObservable
    }

    //if API call is success, update temp cache
    fun retrieveBusStopCodesAndSaveCache() {
        if (busStopCodeRepository!!.retrieveBusStopCodesToCache() != null) {
            busStopCodeTempCache = busStopCodeRepository!!.retrieveBusStopCodesToCache()
        }
    }

    fun getBusScheduleListObservable(busStopCode: Long): LiveData<BusScheduleMeta>? {
        busScheduleListObservable = busScheduleRepository!!.getBusScheduleMetaList(busStopCode)
        //expandableListDetail[busStopCode] = busScheduleListObservable?.value?.Services
        return busScheduleListObservable
    }
}