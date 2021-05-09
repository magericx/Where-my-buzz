package com.example.wheremybuzz.viewModel

import android.app.Application
import android.util.Log
import android.widget.ExpandableListAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wheremybuzz.adapter.CustomExpandableListAdapter
import com.example.wheremybuzz.model.*
import com.example.wheremybuzz.model.callback.BusScheduleMetaCallBack
import com.example.wheremybuzz.model.callback.StatusCallBack
import com.example.wheremybuzz.repository.BusScheduleRepository
import com.example.wheremybuzz.repository.BusStopCodeRepository
import com.example.wheremybuzz.repository.NearestBusRepository
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class NearestBusStopsViewModel(application: Application) : AndroidViewModel(application) {

    companion object{
        private val TAG = "NearestBusStopsView"
    }

    private val applicationContext = getApplication<Application>().applicationContext
    lateinit var nearestBusStopsGeoListObservable: MutableLiveData<StatusEnum>
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    private val executorService2: ExecutorService = Executors.newFixedThreadPool(6)

    private var expandableListDetail: HashMap<String, MutableList<StoredBusMeta>>
    private lateinit var expandableListAdapter: ExpandableListAdapter
    private lateinit var expandableListTitle: List<String>

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

    private fun setExpandableListDetail(key: String, list: MutableList<StoredBusMeta>) {
        expandableListDetail[key] = list
    }

    private fun setBusStopCodeInExpendableListDetail(key: String, busStopCode: String) {
        Log.d(TAG,"setBusStopCodeInExpendableListDetail here")
        if (expandableListDetail.containsKey(key)) {
            val oldValue = expandableListDetail[key]
            oldValue?.get(0)?.BusStopCode = busStopCode
            oldValue?.let {
                expandableListDetail[key] = it
            }
        }
    }

    //Implementation for services
    private fun setServicesInExpendableListDetail(key: String, serviceList: List<Service>) {
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

    private fun setupExpandableListTitle() {
        expandableListTitle = ArrayList<String>(expandableListDetail.keys)
    }

    fun setUpExpandableListAdapter(): ExpandableListAdapter {
        setupExpandableListTitle()
        expandableListAdapter =
            CustomExpandableListAdapter(
                applicationContext,
                expandableListTitle,
                expandableListDetail
            )
        return expandableListAdapter
    }

    fun getExpandableListTitle(): List<String> {
        return expandableListTitle
    }

    fun updateExpandableListAdapter() {
        (expandableListAdapter as CustomExpandableListAdapter).notifyDataSetChanged()
    }

    fun getGeoLocationBasedOnBusStopName(busStopName: String): GeoLocation {
        return GeoLocation(
            expandableListDetail[busStopName]!![0].Geolocation.latitude,
            expandableListDetail[busStopName]!![0].Geolocation.longitude
        )
    }

    fun getNearestBusStopsGeoListObservable(location: String): LiveData<StatusEnum>? {
        nearestBusStopsGeoListObservable = MutableLiveData()
        executorService2.submit {
            nearestBusRepository!!.getNearestBusStops(location) {
                if (!it.BusStopMetaList.isNullOrEmpty()) {
                    val nearestBusStopsList = it.BusStopMetaList
                    for (i in it.BusStopMetaList.indices) {
                        val busStopArrayList: MutableList<StoredBusMeta> = ArrayList()
                        val geoLocation = GeoLocation(
                            nearestBusStopsList[i]!!.latitude,
                            nearestBusStopsList[i]!!.longitude
                        )
                        val finalBusMeta = StoredBusMeta("0", geoLocation, null)
                        busStopArrayList.add(finalBusMeta)
                        setExpandableListDetail(
                            nearestBusStopsList[i]!!.busStopName,
                            busStopArrayList
                        )
                    }
                    nearestBusStopsGeoListObservable.postValue(StatusEnum.Success)
                } else {
                    nearestBusStopsGeoListObservable.postValue(StatusEnum.UnknownError)
                }
            }
        }
        return nearestBusStopsGeoListObservable
    }

    fun getBusStopCodeListObservable(
        busStopName: String,
        latitude: Double,
        longtitude: Double
    ) {
        executorService2.submit {
            Log.d(TAG,"Retrieve cache here")
            busStopCodeRepository!!.getBusStopCodeFromCache(
                busStopCodeTempCache,
                busStopName,
                latitude,
                longtitude
            ) {
                setBusStopCodeInExpendableListDetail(
                    busStopName,
                    it.busStopCode
                )
                getBusScheduleListObservable(it.busStopCode.toLong(), busStopName)
            }
        }
    }

    //if API call is success, update temp cache
    fun retrieveBusStopCodesAndSaveCache() {
        executorService2.submit {
            busStopCodeRepository!!.retrieveBusStopCodesToCache { busStopsCodesResponse ->
                busStopCodeTempCache = busStopsCodesResponse
                Log.d(TAG, "Retrieved cache is $busStopCodeTempCache")
            }
        }
    }

    private fun getBusScheduleListObservable(
        busStopCode: Long,
        busStopName: String
    ) {
        executorService2.submit {
            busScheduleRepository!!.getBusScheduleMetaList(busStopCode, object:
                BusScheduleMetaCallBack {
                override fun updateOnResult(busScheduleMeta: BusScheduleMeta) {
                    Log.d(TAG,"calling busScheduleMeta here")
                    if (busScheduleMeta.Services.isNotEmpty()) {
                        setServicesInExpendableListDetail(busStopName, busScheduleMeta.Services)
                        updateExpandableListAdapter()
                    }
                }
            })
        }
    }

    fun refreshExpandedBusStops(
        busStopList: HashMap<String, String>,
        callback: StatusCallBack
    ) {
        executorService.submit {
            Log.d(TAG, "Current thread executing is ${Thread.currentThread().name}")
            //callback method
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
                    updateExpandableListAdapter()
                    callback.updateOnResult(true)
                } else {
                    Log.d(TAG,"Trigger callback here")
                    callback.updateOnResult(false)
                }
            }
        }
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
