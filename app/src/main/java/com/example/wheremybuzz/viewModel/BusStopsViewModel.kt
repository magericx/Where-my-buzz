package com.example.wheremybuzz.viewModel

import android.content.Context
import android.util.Log
import android.widget.ExpandableListAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wheremybuzz.BusApplication
import com.example.wheremybuzz.adapter.CustomExpandableListAdapter
import com.example.wheremybuzz.enum.FragmentType
import com.example.wheremybuzz.model.*
import com.example.wheremybuzz.model.callback.BusScheduleMetaCallBack
import com.example.wheremybuzz.model.callback.StatusCallBack
import com.example.wheremybuzz.repository.BusScheduleRepository
import com.example.wheremybuzz.repository.BusStopCodeRepository
import com.example.wheremybuzz.repository.NearestBusRepository
import com.example.wheremybuzz.utils.helper.network.NetworkUtil
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import javax.inject.Inject

@HiltViewModel
class BusStopsViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val networkHelper: NetworkUtil,
    private val busScheduleRepository: BusScheduleRepository,
    private val busStopCodeRepository: BusStopCodeRepository,
    private val nearestBusRepository: NearestBusRepository,
    private val sharedPreferenceManager: SharedPreferenceManager
) : ViewModel() {

    companion object {
        private val TAG = "ViewModel"
    }

    lateinit var nearestBusStopsGeoListObservable: MutableLiveData<StatusEnum>
    lateinit var favouriteBusStopsGeoListObservable: MutableLiveData<StatusEnum>
    var executorService: ExecutorService = BusApplication.poolThread
    var executorService2: ExecutorService = BusApplication.poolThread2

    var expandableNearestListDetail: ConcurrentHashMap<String, MutableList<StoredBusMeta>> =
        ConcurrentHashMap()
    var expandableFavouriteListDetail: ConcurrentHashMap<String, MutableList<StoredBusMeta>> =
        ConcurrentHashMap()
    private lateinit var expandableNearestListAdapter: ExpandableListAdapter
    private lateinit var expandableFavouriteListAdapter: ExpandableListAdapter
    private lateinit var expandableNearestListTitle: List<String>
    private lateinit var expandableFavouriteListTitle: List<String>

    private var busStopCodeTempCache: BusStopsCodeResponse? = null


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


    fun getBusStopCodeFromCache(
        busStopCodeTempCache: BusStopsCodeResponse?,
        busStopName: String,
        latitude: Double,
        longtitude: Double
    ): String? {
        return busStopCodeRepository.getBusStopCodeFromCache(
            busStopCodeTempCache,
            busStopName,
            latitude,
            longtitude
        )?.busStopCode
    }

    //TODO update expandableListDetail
    fun updateExpandableNearestListDetailNewLocation(location: Location) {
        executorService2.submit {
            nearestBusRepository.getNearestBusStops(
                GeoLocation(
                    latitude = location.lat,
                    longitude = location.lng
                )
            ) {
                val listInnerBusStopMeta: List<InnerBusStopMeta?>? = it.BusStopMetaList
                if (!listInnerBusStopMeta.isNullOrEmpty()) {
                    //get a new list with all the busStopName here
                    val needToAddList: List<String> =
                        listInnerBusStopMeta.map { innerBusStopMeta -> innerBusStopMeta!!.busStopName }
                            .toList()

                    //filter out all the new stops that needs to be added in
                    (listInnerBusStopMeta.filter { innerBusStopMeta ->
                        !expandableNearestListDetail.containsKey(innerBusStopMeta!!.busStopName)
                    } as MutableList<InnerBusStopMeta?>).forEach { item ->
                        val busStopCode = getBusStopCodeFromCache(
                            busStopCodeTempCache,
                            item!!.busStopName,
                            item.latitude,
                            item.longitude
                        )
                        busStopCode?.let {
                            val busStopArrayList: MutableList<StoredBusMeta> =
                                mutableListOf(
                                    StoredBusMeta(
                                        busStopCode,
                                        GeoLocation(item.latitude, item.longitude),
                                        null
                                    )
                                )
                            setExpandableNearestListDetail(
                                item.busStopName,
                                busStopArrayList
                            )
                        }
                    }
                    //TODO add edge case where there can be 2 bus stops with the same name but different busCode
                    //Remove entries that is not in the new list
                    val filteredHashMap = expandableNearestListDetail.filterKeys { keys ->
                        needToAddList.contains(keys)
                    }
                    expandableNearestListDetail = ConcurrentHashMap(filteredHashMap)

                    BusApplication.mainThreadHandler.post {
                        nearestBusStopsGeoListObservable.value = StatusEnum.ReloadAll
                        setupNearestExpandableListTitle()
                        (expandableNearestListAdapter as CustomExpandableListAdapter).refreshExpandableList(
                            expandableNearestListDetail,
                            expandableNearestListTitle
                        )
                    }
                }
            }
        }
    }

    //Implementation for services
    private fun setServicesInExpendableListDetail(
        key: String,
        serviceList: List<Service>,
        fragmentType: FragmentType
    ) {
        val expandableListDetail: ConcurrentHashMap<String, MutableList<StoredBusMeta>> =
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
                    expandableNearestListDetail,
                    sharedPreferenceManager
                )
            }
            FragmentType.FAVOURITE -> {
                setupFavouriteExpandableListTitle()
                expandableFavouriteListAdapter = CustomExpandableListAdapter(
                    applicationContext,
                    expandableFavouriteListTitle,
                    expandableFavouriteListDetail,
                    sharedPreferenceManager
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

    fun getFavouriteBusStopsGeoListObservable(listOfBusStopCodes: Map<String, String>): LiveData<StatusEnum> {
        favouriteBusStopsGeoListObservable = MutableLiveData()
        executorService2.submit {
            setInitialExpandableFavouriteListDetail(listOfBusStopCodes)
            favouriteBusStopsGeoListObservable.postValue(StatusEnum.Success)
        }
        return favouriteBusStopsGeoListObservable
    }

    fun getNearestBusStopsGeoListObservable(location: GeoLocation): LiveData<StatusEnum> {
        nearestBusStopsGeoListObservable = MutableLiveData()
        executorService2.submit {
            nearestBusRepository.getNearestBusStops(location) {
                if (!it.BusStopMetaList.isNullOrEmpty()) {
                    val nearestBusStopsList = it.BusStopMetaList
                    for (i in it.BusStopMetaList.indices) {
                        val busStopArrayList: MutableList<StoredBusMeta> = ArrayList()
                        val busStopCode = getBusStopCodeFromCache(
                            busStopCodeTempCache,
                            nearestBusStopsList[i]!!.busStopName,
                            nearestBusStopsList[i]!!.latitude,
                            nearestBusStopsList[i]!!.longitude
                        ) ?: continue
                        Log.d(TAG, "Bus stop code is retrieved here ")
                        val geoLocation = GeoLocation(
                            nearestBusStopsList[i]!!.latitude,
                            nearestBusStopsList[i]!!.longitude
                        )
                        val finalBusMeta =
                            StoredBusMeta(busStopCode, geoLocation, null)
                        busStopArrayList.add(finalBusMeta)
                        setExpandableNearestListDetail(
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

    //if API call is success, update temp cache
    fun retrieveBusStopCodesAndSaveCache() {
        executorService2.submit {
            busStopCodeRepository.retrieveBusStopCodesToCache { busStopsCodesResponse ->
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
            busScheduleRepository.getBusScheduleMetaList(busStopCode, object :
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
            val details: ConcurrentHashMap<String, MutableList<StoredBusMeta>> =
                if (fragmentType == FragmentType.NEAREST) {
                    expandableNearestListDetail
                } else {
                    expandableFavouriteListDetail
                }

            busScheduleRepository.getBusScheduleMetaRefreshList(busStopList) { it ->
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

    fun getNetworkConnection(cache: Boolean = true): Boolean {
        return networkHelper.getNetworkConnection(cache)
    }


    //destroy all references of repositories
    fun destroyRepositories() {
        executorService.shutdown()
        executorService2.shutdown()

    }

    //destroy observables
    fun destroyDisposable() {
        busScheduleRepository.destroyDisposable()
    }
}
