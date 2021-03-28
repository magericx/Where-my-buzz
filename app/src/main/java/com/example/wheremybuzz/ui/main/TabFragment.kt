package com.example.wheremybuzz.ui.main

//import com.example.wheremybuzz.data.ExpandableListDataPump.data
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.wheremybuzz.R
import com.example.wheremybuzz.ViewModelFactory
import com.example.wheremybuzz.adapter.CustomExpandableListAdapter
import com.example.wheremybuzz.model.*
import com.example.wheremybuzz.utils.helper.CacheHelper
import com.example.wheremybuzz.utils.CacheManager
import com.example.wheremybuzz.utils.SharedPreferenceManager
import com.example.wheremybuzz.utils.TimeUtil
import com.example.wheremybuzz.utils.helper.SharedPreferenceHelper
import com.example.wheremybuzz.viewModel.NearestBusStopsViewModel


class TabFragment : Fragment() {
    var position = 0
    val TAG: String = "TabFragment"

    var expandableListView: ExpandableListView? = null
    var expandableListAdapter: ExpandableListAdapter? = null
    var expandableListTitle: List<String>? = null
    var expandableListDetail: HashMap<String, List<FinalBusMeta>>? = null
    var viewModel: NearestBusStopsViewModel? = null
    private val timeUtil: TimeUtil = TimeUtil()
    lateinit var sharedPreference: SharedPreferenceHelper
    private var cacheHelper: CacheHelper? = null
    private val forceUpdateCache = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments!!.getInt("pos")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.sharedPreference = SharedPreferenceManager.getSharedPreferenceHelper
        this.viewModel =
            ViewModelProvider(requireActivity(), ViewModelFactory(activity!!.application)).get(
                NearestBusStopsViewModel::class.java
            )
        if (position == 0) {
            // check if busStopCode is empty or missing, retrieve and save to cache
            cacheHelper = CacheManager.initializeCacheHelper
            if (forceUpdateCache || !cacheHelper?.cacheExists()!! || timeUtil.checkTimeStampExceed3days(
                    sharedPreference.getSharedPreference()
                )
            ) {
                Log.d(TAG, "Cache file does not exists or expired")
                //let background thread handle the heavy workload
                Thread(Runnable {
                    viewModel?.retrieveBusStopCodesAndSaveCache()
                }).start()
                sharedPreference.setSharedPreference()
            }
        }
        observeNearestBusStopsModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_tab, container, false)
        expandableListView = view.findViewById(R.id.expandableListView)
        return view
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expandableListView!!.setOnGroupExpandListener { groupPosition ->
            Toast.makeText(
                activity!!.applicationContext,
                (expandableListTitle as ArrayList<String>)[groupPosition] + " List Expanded.",
                Toast.LENGTH_SHORT
            ).show()
            val geoLocation =
                viewModel?.getGeoLocationBasedOnBusStopName((expandableListTitle as ArrayList<String>)[groupPosition])
            observeBusStopCodeModel(
                (expandableListTitle as ArrayList<String>)[groupPosition],
                geoLocation!!.latitude,
                geoLocation.longitude
            )
        }

        expandableListView!!.setOnGroupCollapseListener { groupPosition ->
            Toast.makeText(
                activity!!.applicationContext,
                (expandableListTitle as ArrayList<String>).get(groupPosition) + " List Collapsed.",
                Toast.LENGTH_SHORT
            ).show()
        }

        expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            Toast.makeText(
                activity!!.applicationContext,
                (expandableListTitle as ArrayList<String>)[groupPosition] + " -> "
                        + expandableListDetail!![(expandableListTitle as ArrayList<String>).get(
                    groupPosition
                )]!![childPosition],
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    private fun createBusStopNameHeader(nearestBusStopMetaList: BusStopMeta) {
        if (position == 0) {
            Log.d(TAG, "Create header here")
            //amend here to push out the header
            if (!nearestBusStopMetaList.BusStopMetaList.isNullOrEmpty()) {
                val nearestBusStopsList = nearestBusStopMetaList.BusStopMetaList
                for (i in nearestBusStopMetaList.BusStopMetaList.indices) {
                    val busStopArrayList: MutableList<FinalBusMeta> = ArrayList()
                    val serviceArrayList: MutableList<Service> = ArrayList()
                    val geoLocation = GeoLocation(
                        nearestBusStopsList[i]!!.latitude,
                        nearestBusStopsList[i]!!.longitude
                    )
                    val finalBusMeta = FinalBusMeta("0", geoLocation, serviceArrayList)
//                    val innerBusStopMeta = InnerBusStopMeta(
//                        nearestBusStopsList[i]!!.busStopName,
//                        nearestBusStopsList[i]!!.latitude,
//                        nearestBusStopsList[i]!!.longitude,
//                        0
//                    )
                    busStopArrayList.add(finalBusMeta)
                    viewModel?.setExpandableListDetail(
                        nearestBusStopsList[i]!!.busStopName,
                        busStopArrayList
                    )
                }
                createExpandableListAdapter()
            }
        }
    }

    private fun observeBusStopCodeModel(
        expandableListTitle: String,
        latitude: Double,
        longtitude: Double
    ) {
        if (position == 0) {
            Log.d(TAG, "Call bus Stop code list API ")
            // Update the list when the data changes
            viewModel?.getBusStopCodeListObservable(expandableListTitle, latitude, longtitude)
                ?.observe(viewLifecycleOwner,
                    Observer<BusStopCode> { busStopCode ->
                        if (busStopCode != null) {
                            //update hashmap for adapter
                            viewModel?.setBusStopCodeInExpendableListDetail(
                                expandableListTitle,
                                busStopCode.busStopCode
                            )
                            //proceed to busSchedule API
                            observeBusScheduleModel(
                                busStopCode.busStopCode.toLong(),
                                expandableListTitle
                            )
                            Log.d(
                                TAG,
                                "getBusStopCodeListObservable API result is $busStopCode"
                            )
                        }
                    })
        }
    }

    //TODO Add logic to update adapter
    private fun observeBusScheduleModel(busStopCode: Long, busStopName: String) {
        viewModel?.getBusScheduleListObservable(busStopCode)
            ?.observe(viewLifecycleOwner, Observer<BusScheduleMeta> {
                viewModel?.setServicesInExpendableListDetail(busStopName, it.Services)
                updateExpandableListAdapter()
                Log.d(
                    TAG,
                    "getBusScheduleListObservable API result is ${it.Services}"
                )
            })
    }

    private fun createExpandableListAdapter() {
        expandableListDetail = viewModel?.getExpandableListDetail()
        expandableListTitle = ArrayList<String>(expandableListDetail!!.keys)
        expandableListAdapter =
            CustomExpandableListAdapter(
                activity!!.applicationContext,
                expandableListTitle!!,
                expandableListDetail!!
            )
        expandableListView!!.setAdapter(expandableListAdapter)
    }

    private fun updateExpandableListAdapter() {
        (expandableListAdapter as CustomExpandableListAdapter).notifyDataSetChanged()
    }

    private fun observeNearestBusStopsModel() {
        if (position == 0) {
            Log.d(TAG, "Call nearest bus stop API ")
            // Update the list when the data changes
            viewModel?.getNearestBusStopsGeoListObservable(location)
                ?.observe(viewLifecycleOwner,
                    Observer<BusStopMeta> { nearestBusStopMeta ->
                        if (nearestBusStopMeta != null) {
                            Toast.makeText(
                                activity!!.applicationContext, "Update headers on page",
                                Toast.LENGTH_SHORT
                            ).show()
                            createBusStopNameHeader(nearestBusStopMeta)
                            Log.d(
                                TAG,
                                "getNearestBusStopsGeoListObservable API result is $nearestBusStopMeta"
                            )
                        }
                    })
        }
    }

    companion object {
        fun getInstance(position: Int): Fragment {
            val bundle = Bundle()
            bundle.putInt("pos", position)
            val tabFragment = TabFragment()
            tabFragment.arguments = bundle
            return tabFragment
        }

        private val location: String = "1.380308, 103.741256"
    }
}