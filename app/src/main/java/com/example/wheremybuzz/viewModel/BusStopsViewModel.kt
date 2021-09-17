package com.example.wheremybuzz.viewModel

import android.app.Application
import android.util.Log
import android.widget.ExpandableListAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.adapter.CustomExpandableListAdapter
import com.example.wheremybuzz.model.*
import com.example.wheremybuzz.model.callback.BusScheduleMetaCallBack
import com.example.wheremybuzz.model.callback.StatusCallBack
import com.example.wheremybuzz.repository.BusScheduleRepository
import com.example.wheremybuzz.repository.BusStopCodeRepository
import com.example.wheremybuzz.repository.NearestBusRepository
import enum.FragmentType
import java.util.*
import java.util.concurrent.ExecutorService


class BusStopsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val TAG = "NearestBusStopsView"
    }

    private val applicationContext = getApplication<Application>().applicationContext
    lateinit var nearestBusStopsGeoListObservable: MutableLiveData<StatusEnum>
    lateinit var favouriteBusStopsGeoListObservable: MutableLiveData<StatusEnum>
    var executorService: ExecutorService
    var executorService2: ExecutorService

    var expandableNearestListDetail: HashMap<String, MutableList<StoredBusMeta>>
    var expandableFavouriteListDetail: HashMap<String, MutableList<StoredBusMeta>>
    private lateinit var expandableNearestListAdapter: ExpandableListAdapter
    private lateinit var expandableFavouriteListAdapter: ExpandableListAdapter
    private lateinit var expandableNearestListTitle: List<String>
    private lateinit var expandableFavouriteListTitle: List<String>

    private var busStopCodeTempCache: BusStopsCodeResponse? = null

    var nearestBusRepository: NearestBusRepository? = null
    var busStopCodeRepository: BusStopCodeRepository? = null
    var busScheduleRepository: BusScheduleRepository? = null

    init {
        nearestBusRepository = NearestBusRepository()
        busStopCodeRepository = BusStopCodeRepository()
        busScheduleRepository = BusScheduleRepository()
        expandableNearestListDetail = HashMap()
        expandableFavouriteListDetail = HashMap()
        executorService = MyApplication.poolThread
        executorService2 = MyApplication.poolThread2
    }

    fun getExpandableNearestListAdapter(): ExpandableListAdapter {
        return expandableNearestListAdapter
    }

    fun getexpandableFavouriteListAdapter(): ExpandableListAdapter {
        return expandableFavouriteListAdapter
    }

    private fun setExpandableNearestListDetail(key: String, list: MutableList<StoredBusMeta>) {
        expandableNearestListDetail[key] = list
    }

    private fun setExpandableFavouriteListDetail(key: String, list: MutableList<StoredBusMeta>) {
        expandableFavouriteListDetail[key] = list
    }

    private fun setInitialExpandableFavouriteListDetail(mapList: Map<String, String>) {
        for ((k, v) in mapList) {
            val staticBusMeta =
                StoredBusMeta(k, GeoLocation(1.0, 1.0), null)
            val staticListBusMeta: MutableList<StoredBusMeta> = mutableListOf(staticBusMeta)
            setExpandableFavouriteListDetail(v, staticListBusMeta)
        }
    }

    fun getExpandableNearestListBusStopCode(busStopName: String): BusStopCode? {
        return if (expandableNearestListDetail.containsKey(busStopName)) {
            BusStopCode(expandableNearestListDetail[busStopName]?.get(0)?.BusStopCode!!)
        } else {
            null
        }
    }

    fun getExpandableFavouriteListBusStopCode(busStopName: String): BusStopCode? {
        return if (expandableFavouriteListDetail.containsKey(busStopName)) {
            BusStopCode(expandableFavouriteListDetail[busStopName]?.get(0)?.BusStopCode!!)
        } else {
            null
        }
    }

    private fun setBusStopCodeInExpendableListDetail(key: String, busStopCode: String) {
        Log.d(TAG, "setBusStopCodeInExpendableListDetail here")
        if (expandableNearestListDetail.containsKey(key)) {
            val oldValue = expandableNearestListDetail[key]
            oldValue?.get(0)?.BusStopCode = busStopCode
            oldValue?.let {
                expandableNearestListDetail[key] = it
            }
        }
    }

    //Implementation for services
    private fun setServicesInExpendableListDetail(
        key: String,
        serviceList: List<Service>,
        fragmentType: FragmentType
    ) {
        //Log.d(TAG, "expandableListDetails is $expandableListDetail")
        val expandableListDetail: HashMap<String, MutableList<StoredBusMeta>> =
            if (fragmentType == FragmentType.NEAREST) expandableNearestListDetail else expandableFavouriteListDetail
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

    private fun setupNearestExpandableListTitle() {
        expandableNearestListTitle = ArrayList<String>(expandableNearestListDetail.keys)
    }

    private fun setupFavouriteExpandableListTitle() {
        expandableFavouriteListTitle = ArrayList<String>(expandableFavouriteListDetail.keys)
    }

    //flag = integer, 0 = nearest, 1 = favourite
    fun setUpExpandableListAdapter(fragmentType: FragmentType) {
        when (fragmentType) {
            FragmentType.NEAREST -> {
                setupNearestExpandableListTitle()
                expandableNearestListAdapter = CustomExpandableListAdapter(
                    applicationContext,
                    expandableNearestListTitle,
                    expandableNearestListDetail
                )
            }
            FragmentType.FAVOURITE -> {
                setupFavouriteExpandableListTitle()
                expandableFavouriteListAdapter = CustomExpandableListAdapter(
                    applicationContext,
                    expandableFavouriteListTitle,
                    expandableFavouriteListDetail
                )
            }
        }
    }

    fun getNearestExpandableListSize(): Int {
        return expandableNearestListDetail.size
    }

    fun getNearestExpandableListTitle(): List<String> {
        return expandableNearestListTitle
    }

    fun getFavouriteExpandableListTitle(): List<String> {
        return expandableFavouriteListTitle
    }

    fun updateNearestExpandableListAdapter() {
        (expandableNearestListAdapter as CustomExpandableListAdapter).notifyDataSetChanged()
    }

    fun updateFavouriteExpandableListAdapter() {
        (expandableFavouriteListAdapter as CustomExpandableListAdapter).notifyDataSetChanged()
    }

    fun getGeoLocationBasedOnBusStopName(busStopName: String): GeoLocation {
        return GeoLocation(
            expandableNearestListDetail[busStopName]!![0].Geolocation.latitude,
            expandableNearestListDetail[busStopName]!![0].Geolocation.longitude
        )
    }

    fun getFavouriteBusStopsGeoListObservable(listOfBusStopCodes: Map<String, String>): LiveData<StatusEnum>? {
        favouriteBusStopsGeoListObservable = MutableLiveData()
        executorService2.submit {
            setInitialExpandableFavouriteListDetail(listOfBusStopCodes)
            favouriteBusStopsGeoListObservable.postValue(StatusEnum.Success)
        }
        return favouriteBusStopsGeoListObservable
    }

    fun getNearestBusStopsGeoListObservable(location: GeoLocation): LiveData<StatusEnum>? {
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
                        busStopCodeRepository!!.getBusStopCodeFromCache(
                            busStopCodeTempCache,
                            nearestBusStopsList[i]!!.busStopName,
                            nearestBusStopsList[i]!!.latitude,
                            nearestBusStopsList[i]!!.longitude
                        ) { busStopCode ->
                            if (busStopCode.busStopCode.isNotEmpty()) {
                                Log.d(TAG, "Bus stop code is retrieved here ")
                                val finalBusMeta =
                                    StoredBusMeta(busStopCode.busStopCode, geoLocation, null)
                                busStopArrayList.add(finalBusMeta)
                                setExpandableNearestListDetail(
                                    nearestBusStopsList[i]!!.busStopName,
                                    busStopArrayList
                                )
                            }
                        }
                    }
                    nearestBusStopsGeoListObservable.postValue(StatusEnum.Success)
                } else {
                    nearestBusStopsGeoListObservable.postValue(StatusEnum.UnknownError)
                }
            }
        }
        return nearestBusStopsGeoListObservable
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

    //flag = 0, nearest, flag = 1, favourite
    fun getBusScheduleListObservable(
        busStopCode: Long,
        busStopName: String,
        fragmentType: FragmentType
    ) {
        executorService2.submit {
            busScheduleRepository!!.getBusScheduleMetaList(busStopCode, object :
                BusScheduleMetaCallBack {
                override fun updateOnResult(busScheduleMeta: BusScheduleMeta) {
                    Log.d(TAG, "calling busScheduleMeta here")
                    if (busScheduleMeta.Services.isNotEmpty()) {
                        setServicesInExpendableListDetail(
                            busStopName,
                            busScheduleMeta.Services,
                            fragmentType
                        )
                        if (fragmentType == FragmentType.NEAREST) updateNearestExpandableListAdapter() else updateFavouriteExpandableListAdapter()
                    }
                }
            })
        }
    }

    fun refreshExpandedBusStops(
        busStopList: HashMap<String, String>,
        callback: StatusCallBack,
        fragmentType: FragmentType
    ) {
        executorService.submit {
            Log.d(TAG, "Current thread executing is ${Thread.currentThread().name}")
            //callback method
            val details: HashMap<String, MutableList<StoredBusMeta>> =
                if (fragmentType == FragmentType.NEAREST) {
                    expandableNearestListDetail
                } else {
                    expandableFavouriteListDetail
                }

            busScheduleRepository?.getBusScheduleMetaRefreshList(busStopList) { it ->
                if (it.servicesList.isNotEmpty()) {
                    Log.d(TAG, "ServiceList size is ${it.servicesList.size}")
                    //update actual data holder
                    Log.d(TAG, "Retrieved key is ${it.servicesList[0].first}")
                    Log.d(TAG, "Full set of keys are ${details.keys}")
                    for (i in it.servicesList) {
                        if (details.containsKey(i.first)) {
                            Log.d(TAG, "Found key")
                            setServicesInExpendableListDetail(
                                i.first,
                                i.second.Services,
                                fragmentType
                            )
                        }
                    }
                    Log.d(TAG, "PostValue observer here")
                    when (fragmentType) {
                        FragmentType.NEAREST -> updateNearestExpandableListAdapter()
                        FragmentType.FAVOURITE -> updateFavouriteExpandableListAdapter()
                        else -> {
                            //do nothing
                        }

                    }
                    callback.updateOnResult(true)
                } else {
                    Log.d(TAG, "Trigger callback here")
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
