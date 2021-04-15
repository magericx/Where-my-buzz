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
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class NearestBusStopsViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var nearestBusStopsGeoListObservable: MutableLiveData<BusStopMeta>
    lateinit var busStopCodeListObservable: MutableLiveData<BusStopCode>
    lateinit var busScheduleListObservable: LiveData<BusScheduleMeta>
    lateinit var busScheduleListRefreshObservable: MutableLiveData<BusScheduleRefreshStatus>
    private val TAG = "NearestBusStopsView"
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    private val executorService2: ExecutorService = Executors.newFixedThreadPool(6)


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
        //Log.d(TAG, "expandableListDetails is $expandableListDetail")
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
        nearestBusStopsGeoListObservable = MutableLiveData()
        executorService2.submit {
            nearestBusRepository!!.getNearestBusStops(location) {
                nearestBusStopsGeoListObservable.postValue(it)
            }
        }
        return nearestBusStopsGeoListObservable
    }

    fun getBusStopCodeListObservable(
        busStopName: String,
        latitude: Double,
        longtitude: Double
    ): LiveData<BusStopCode>? {
        busStopCodeListObservable = MutableLiveData()
        executorService2.submit {
            busStopCodeRepository!!.getBusStopCodeFromCache(
                busStopCodeTempCache,
                busStopName,
                latitude,
                longtitude
            ) {
                busStopCodeListObservable.postValue(it)
            }
        }
        return busStopCodeListObservable
    }

    //if API call is success, update temp cache
    fun retrieveBusStopCodesAndSaveCache() {
        executorService2.submit {
            if (busStopCodeRepository!!.retrieveBusStopCodesToCache() != null) {
                busStopCodeTempCache = busStopCodeRepository!!.retrieveBusStopCodesToCache()
            }
        }
    }

    fun getBusScheduleListObservable(busStopCode: Long): LiveData<BusScheduleMeta>? {
        busScheduleListObservable = MutableLiveData()
        executorService2.submit {
            busScheduleRepository!!.getBusScheduleMetaList(busStopCode) {
                (busScheduleListObservable as MutableLiveData<BusScheduleMeta>).postValue(it)
            }
        }
        return busScheduleListObservable
    }

    fun refreshExpandedBusStops(busStopList: HashMap<String, String>): LiveData<BusScheduleRefreshStatus>? {
        //busScheduleListRefreshObservable = busScheduleRepository!!.getBusScheduleMetaRefreshList(busStopList)
        busScheduleListRefreshObservable = MutableLiveData()
        executorService.submit {
            Log.d(TAG, "Current thread executing is ${Thread.currentThread().name}")
            //TODO add callback method here
            busScheduleRepository?.getBusScheduleMetaRefreshList(busStopList) { it ->
                if (it.servicesList.isNotEmpty()) {
                    Log.d(TAG, "ServiceList size is ${it.servicesList.size}")
                    //update actual data holder
                    Log.d(TAG, "Retrieved key is ${it.servicesList[0].first}")
                    Log.d(TAG, "Full set of keys are ${expandableListDetail.keys}")
                    for (i in it.servicesList) {
                        if (expandableListDetail.containsKey(i.first)) {
                            Log.d(TAG, "Found key")
                            setServicesInExpendableListDetail(i.first, i.second.Services)
                        }
                    }
                    Log.d(TAG, "PostValue observer here")
                    busScheduleListRefreshObservable.postValue(BusScheduleRefreshStatus(true))
                } else {
                    busScheduleListRefreshObservable.postValue(BusScheduleRefreshStatus(false))
                }
            }
        }
        Log.d(TAG, "Return observer here")
        return busScheduleListRefreshObservable
    }


    //destroy all references of repositories
    fun destroyRepositories() {
        executorService.shutdown()
        executorService2.shutdown()
        nearestBusRepository = null
        busStopCodeRepository = null
        busScheduleRepository = null
    }

    //destroy observables
    fun destroyDisposable() {
        busScheduleRepository?.destroyDisposable()
    }
}