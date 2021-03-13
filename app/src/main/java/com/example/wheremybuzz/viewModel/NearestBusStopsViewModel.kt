package com.example.wheremybuzz.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.wheremybuzz.model.BusStopMeta
import com.example.wheremybuzz.model.BusStopsCodeResponse
import com.example.wheremybuzz.model.InnerBusStopMeta
import com.example.wheremybuzz.model.NearestBusStopsResponse
import com.example.wheremybuzz.repository.BusStopCodeRepository
import com.example.wheremybuzz.repository.NearestBusRepository
import java.util.HashMap


class NearestBusStopsViewModel(application: Application) : AndroidViewModel(application) {
    //private var nearestBusStopsListObservable: LiveData<List<NearestBusStopsResponse>>? = null
    private var nearestBusStopsGeoListObservable: LiveData<BusStopMeta>? = null
    private var busStopCodeListObservable: LiveData<BusStopsCodeResponse>? = null
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
    ): LiveData<BusStopsCodeResponse>? {
        busStopCodeListObservable =
            busStopCodeRepository!!.getBusStopCode(busStopName, latitude, longtitude)
        return busStopCodeListObservable
    }

}