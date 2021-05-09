package com.example.wheremybuzz.ui.main

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wheremybuzz.R
import com.example.wheremybuzz.ViewModelFactory
import com.example.wheremybuzz.model.StatusEnum
import com.example.wheremybuzz.model.StoredBusMeta
import com.example.wheremybuzz.utils.CacheManager
import com.example.wheremybuzz.utils.SharedPreferenceManager
import com.example.wheremybuzz.utils.TimeUtil
import com.example.wheremybuzz.utils.helper.CacheHelper
import com.example.wheremybuzz.utils.helper.SharedPreferenceHelper
import com.example.wheremybuzz.viewModel.NearestBusStopsViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class TabFragment : Fragment() {
    var position = 0
    private val TAG: String = "TabFragment"

    var shimmeringLayoutView: ShimmerFrameLayout? = null
    var expandableListView: ExpandableListView? = null
    lateinit var swipeContainer: SwipeRefreshLayout
    lateinit var expandableListAdapter: ExpandableListAdapter
    lateinit var expandableListTitle: List<String>
    var viewModel: NearestBusStopsViewModel? = null
    private val timeUtil: TimeUtil = TimeUtil
    lateinit var sharedPreference: SharedPreferenceHelper
    lateinit var cacheHelper: CacheHelper
    private val forceUpdateCache = false
    private var allowRefresh = false
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)


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
            cacheHelper = CacheManager.initializeCacheHelper!!
            if (forceUpdateCache || !cacheHelper.cacheExists() || timeUtil.checkTimeStampExceed3days(
                    sharedPreference.getSharedPreference()
                )
            ) {
                Log.d(TAG, "Cache file does not exists or expired")
                //let background thread handle the heavy workload
                viewModel?.retrieveBusStopCodesAndSaveCache()
                sharedPreference.setSharedPreference()
            }
            observeNearestBusStopsModel()
        }
    }

    override fun onResume() {
        super.onResume()
        if (position == 0 && allowRefresh) {
            allowRefresh = false
            Log.d(TAG, "On resume app here")
            refreshExpandedList(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_tab, container, false)
        shimmeringLayoutView = view.findViewById(R.id.shimmer_view_container)
        swipeContainer = view.findViewById(R.id.swipeContainer)
        if (position == 0) {
            enableShimmer()
        } else {
            //position == 1, hide the shimmer
            hideShimmeringLayout()
            swipeContainer.visibility = View.INVISIBLE
        }
        expandableListView = view.findViewById(R.id.expandableListView)
        Log.d(TAG, "debug expendable $expandableListView")
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
            //don't allow refresh if cell is expanding
            allowRefresh = false
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
                (expandableListTitle as ArrayList<String>)[groupPosition] + " List Collapsed.",
                Toast.LENGTH_SHORT
            ).show()
        }

        expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            Toast.makeText(
                activity!!.applicationContext,
                (expandableListTitle as ArrayList<String>)[groupPosition] + " -> "
                        + viewModel?.getExpandableListDetail()!![(expandableListTitle as ArrayList<String>)[groupPosition]]!![childPosition],
                Toast.LENGTH_SHORT
            ).show()
            false
        }
        if (position == 0) {
            swipeContainer.setOnRefreshListener { // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                refreshExpandedList(true)
            }
            swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
            )
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
            allowRefresh = true
        }
    }

    private fun createExpandableListAdapter() {
        expandableListAdapter = viewModel?.setUpExpandableListAdapter()!!
        expandableListTitle = viewModel?.getExpandableListTitle()!!
        expandableListView!!.setAdapter(expandableListAdapter)
    }

    private fun observeNearestBusStopsModel() {
        if (position == 0) {
            Log.d(TAG, "Call nearest bus stop API ")
            // Update the list when the data changes
            viewModel?.getNearestBusStopsGeoListObservable(location)
                ?.observe(viewLifecycleOwner,
                    Observer<StatusEnum> { status ->
                        if (status != null) {
                            Toast.makeText(
                                activity!!.applicationContext, "Update headers on page",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (status == StatusEnum.Success) {
                                createExpandableListAdapter()
                                disableShimmer()
                            } else {
                                //Show error placeholder page
                                disableShimmer()
                            }
                        }
                    })
        }
    }

    private fun enableShimmer() {
        shimmeringLayoutView?.startShimmerAnimation()
        shimmeringLayoutView?.visibility = View.VISIBLE
    }

    private fun disableShimmer() {
        shimmeringLayoutView?.stopShimmerAnimation()
        shimmeringLayoutView?.visibility = View.INVISIBLE
    }

    private fun hideShimmeringLayout() {
        shimmeringLayoutView?.visibility = View.INVISIBLE
    }

    private fun refreshExpandedList(swipeRefresh: Boolean) {
        val list: HashMap<String, String>? = getCurrentExpandedList()
        if (!list.isNullOrEmpty()) {
            Log.d(TAG, "List of bus stop code that requires re-fetch are $list")
            viewModel?.refreshExpandedBusStops(list) { busScheduleRefreshStatus ->
                if (busScheduleRefreshStatus.refreshstatus) {
                    if (!swipeRefresh) {
                        allowRefresh = true
                    }else{
                        swipeContainer.isRefreshing = false
                    }
                } else {
                    //check if nothing retrieved due to network, show error placeholder page

                }

            }
        }
        else{
            if (swipeRefresh) {
                swipeContainer.isRefreshing = false
            }
        }
    }

    //method that will check for the expanded items and add into hashmap <busStopCode,busStopName>
    private fun getCurrentExpandedList(): HashMap<String, String>? {
        val groupCount = expandableListAdapter.groupCount
        Log.d(TAG, "total number of group count $groupCount")
        val visibleExpandedList: HashMap<String, String> = hashMapOf()
        for (i in 0 until groupCount) {
            val expanded = expandableListView?.isGroupExpanded(i) ?: false
            Log.d(TAG, "group position number $i is $expanded")
            if (expanded) {
                val busStopCode = (expandableListAdapter.getChild(
                    i, firstIndex
                ) as StoredBusMeta).BusStopCode
                val busStopName = (expandableListAdapter.getGroup(
                    i
                )
                        ).toString()
                if (busStopCode.isNotEmpty() && busStopName.isNotEmpty()) {
                    visibleExpandedList[busStopCode] = busStopName
                }
            }
        }
        if (visibleExpandedList.size == 0) {
            return null
        }
        return visibleExpandedList
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
        private val firstIndex: Int = 0
    }

    override fun onPause() {
        viewModel?.destroyDisposable()
        super.onPause()
    }

    override fun onDestroy() {
        expandableListView = null
        viewModel?.destroyDisposable()
        viewModel?.destroyRepositories()
        viewModel = null
        super.onDestroy()
    }
}