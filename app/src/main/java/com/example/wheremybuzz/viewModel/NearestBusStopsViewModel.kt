package com.example.wheremybuzz.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.wheremybuzz.model.*
import com.example.wheremybuzz.repository.BusStopCodeRepository
import com.example.wheremybuzz.repository.NearestBusRepository
import java.util.HashMap


class NearestBusStopsViewModel(application: Application) : AndroidViewModel(application) {
    //private var nearestBusStopsListObservable: LiveData<List<NearestBusStopsResponse>>? = null
    private var nearestBusStopsGeoListObservable: LiveData<BusStopMeta>? = null
    private var busStopCodeListObservable: LiveData<BusStopCode>? = null
    private val TAG = "NearestBusStopsView"
    private var expandableListDetail: HashMap<String, List<InnerBusStopMeta>>

    var nearestBusRepository: NearestBusRepository? = null
    var busStopCodeRepository: BusStopCodeRepository? = null

    init {
        // If any transformation is needed, this can be simply done by Transformations class ...
//            projectListObservable = NearestBusRepository
        nearestBusRepository = NearestBusRepository()
        busStopCodeRepository = BusStopCodeRepository()
        expandableListDetail = HashMap()
    }

    fun getExpandableListDetail(): HashMap<String, List<InnerBusStopMeta>> {
        return expandableListDetail
    }

    fun setExpandableListDetail(key: String, list: List<InnerBusStopMeta>) {
        expandableListDetail[key] = list
    }

    fun getGeoLocationBasedOnBusStopName(busStopName: String): GeoLocation {
        return GeoLocation(
            expandableListDetail[busStopName]!![0].latitude,
            expandableListDetail[busStopName]!![0].longitude
        )
    }

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
            busStopCodeRepository!!.getBusStopCode(busStopName, latitude, longtitude , 0)
        return busStopCodeListObservable
    }

    fun retrieveBusStopCodesAndSaveCache(){
        busStopCodeRepository!!.retrieveBusStopCodesToCache()
    }

    fun checkCacheExists(): Boolean{
        return busStopCodeRepository!!.cacheExists()
    }

}