package com.example.wheremybuzz.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private var busScheduleListRefreshObservable: MutableLiveData<BusScheduleRefreshStatus>? = null
    private val TAG = "NearestBusStopsView"

    private var expandableListDetail: HashMap<String, MutableList<StoredBusMeta>>


    private var busStopCodeTempCache: BusStopsCodeResponse? = null

    var nearestBusRepository: NearestBusRepository? = null
    var busStopCodeRepository: BusStopCodeRepository? = null
    var busScheduleRepository: BusScheduleRepository? = null

    init {
        nearestBusRepository = NearestBusRepository()
        busStopCodeRepository = BusStopCodeRepository()
        busScheduleRepository = BusScheduleRepository()
        expandableListDetail = HashMap()
    }

    fun getExpandableListDetail(): HashMap<String, MutableList<StoredBusMeta>> {
        return expandableListDetail
    }

    fun setExpandableListDetail(key: String, list: MutableList<StoredBusMeta>) {
        expandableListDetail[key] = list
    }

    fun setBusStopCodeInExpendableListDetail(key: String, busStopCode: String) {
        if (expandableListDetail.containsKey(key)) {
            val oldValue = expandableListDetail[key]
            oldValue?.get(0)?.BusStopCode = busStopCode
            oldValue?.let {
                expandableListDetail[key] = it
            }
        }
    }

    //TODO Add implementation for services
    fun setServicesInExpendableListDetail(key: String, serviceList: List<Service>) {
        Log.d(TAG, "expandableListDetails is $expandableListDetail")
        if (expandableListDetail.containsKey(key)) {
            val currentExpandableHashMap = expandableListDetail[key]
            val oldBusStopCode = currentExpandableHashMap?.get(0)?.BusStopCode
            val oldGeoLocation = currentExpandableHashMap?.get(0)?.Geolocation
            currentExpandableHashMap?.clear()
            for (i in serviceList.indices) {
                val newFinalBusMeta =
                    StoredBusMeta(oldBusStopCode!!, oldGeoLocation!!, serviceList[i])
                currentExpandableHashMap.add(newFinalBusMeta)
            }
        }
    }

    fun getGeoLocationBasedOnBusStopName(busStopName: String): GeoLocation {
        return GeoLocation(
            expandableListDetail[busStopName]!![0].Geolocation.latitude,
            expandableListDetail[busStopName]!![0].Geolocation.longitude
        )
    }

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

    fun refreshExpandedBusStops(busStopList: List<BusStopNameAndCode>): LiveData<BusScheduleRefreshStatus>? {
        //busScheduleListRefreshObservable = busScheduleRepository!!.getBusScheduleMetaRefreshList(busStopList)
        busScheduleListRefreshObservable = MutableLiveData()
        //TODO add callback method here
        busScheduleRepository?.getBusScheduleMetaRefreshList(busStopList) { it ->
            if (it.servicesList.isNotEmpty()) {
                Log.d(TAG, "Execute callback when data is returned")
                //update actual data holder
                Log.d(TAG,"Retrieved key is ${it.servicesList[0].first}")
                Log.d(TAG,"Full set of keys are ${expandableListDetail.keys}")
                if (expandableListDetail.containsKey(it.servicesList[0].first)) {
                    Log.d(TAG,"Found key")
                    setServicesInExpendableListDetail(it.servicesList[0].first,it.servicesList[0].second.Services)
                    busScheduleListRefreshObservable?.postValue(BusScheduleRefreshStatus(true))
                }
            } else {
                busScheduleListRefreshObservable?.postValue(BusScheduleRefreshStatus(false))
            }
        }
        return busScheduleListRefreshObservable
    }


    //destroy all references of repositories
    fun destroyRepositories() {
        nearestBusRepository = null
        busStopCodeRepository = null
        busScheduleRepository = null
    }
}