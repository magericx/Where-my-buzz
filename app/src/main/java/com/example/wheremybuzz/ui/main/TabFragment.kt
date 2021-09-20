package com.example.wheremybuzz.ui.main

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.location.Location
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wheremybuzz.R
import com.example.wheremybuzz.ViewModelFactory
import com.example.wheremybuzz.model.GeoLocation
import com.example.wheremybuzz.model.StatusEnum
import com.example.wheremybuzz.model.StoredBusMeta
import com.example.wheremybuzz.model.callback.StatusCallBack
import com.example.wheremybuzz.utils.helper.cache.CacheHelper
import com.example.wheremybuzz.utils.helper.cache.CacheManager
import com.example.wheremybuzz.utils.helper.intent.IntentHelper
import com.example.wheremybuzz.utils.helper.network.NetworkUtil
import com.example.wheremybuzz.utils.helper.permission.*
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceHelper
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceManager
import com.example.wheremybuzz.utils.helper.time.TimeUtil
import com.example.wheremybuzz.view.AlertDialogView
import com.example.wheremybuzz.view.DialogListener
import com.example.wheremybuzz.view.ErrorView
import com.example.wheremybuzz.viewModel.BusStopsViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.scopes.FragmentScoped
import enum.FragmentType

@FragmentScoped
class TabFragment : Fragment() {

    companion object {
        private const val firstIndex: Int = 0
        private const val TAG: String = "TabFragment"
        fun getInstance(position: Int): Fragment {
            val bundle = Bundle()
            bundle.putInt("pos", position)
            val tabFragment = TabFragment()
            tabFragment.arguments = bundle
            return tabFragment
        }

        private val timeUtil: TimeUtil =
            TimeUtil
        private const val forceUpdateCache = false
    }

    var position = 0
    var shimmeringLayoutView: ShimmerFrameLayout? = null
    var expandableListView: ExpandableListView? = null
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var expandableListAdapter: ExpandableListAdapter
    private lateinit var expandableListTitle: List<String>
    private lateinit var viewModel: BusStopsViewModel

    private lateinit var sharedPreference: SharedPreferenceHelper
    private lateinit var cacheHelper: CacheHelper
    private var allowRefresh = false
    lateinit var parentView: View
    private var errorView: ErrorView? = null
    var numberOfTries = 0
    private var isPermissionDialogActive: Boolean = false

    private lateinit var locationServicesHelper: LocationServicesHelper

    private val requestMultiplePermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                Log.d(TAG, "Permission here ${it.key} = ${it.value}")
            }
            if (permissions[ACCESS_FINE_LOCATION] != null && permissions[ACCESS_FINE_LOCATION] == false) {
                showPermissionDialog()
                return@registerForActivityResult
            }
            locationServicesHelper.retrieveLastLocation(locationCallback)
        }

    private fun handlePermissionResponse(permissionArray: Array<String>?) {
        permissionArray.let {
            requestMultiplePermissions.launch(permissionArray)
        }
    }

    private var locationCallback: LocationCallback = object : LocationCallback {
        override fun updateOnResult(location: Location?, statusEnum: StatusEnum) {
            when (statusEnum) {
                StatusEnum.Success -> {
                    var tempLatitude = 0.0
                    var tempLongitude = 0.0
                    location.let {
                        it?.latitude?.let { latitude ->
                            tempLatitude = latitude
                        }
                        it?.longitude?.let { longitude ->
                            tempLongitude = longitude
                        }
                    }
                    observeNearestBusStopsModel(
                        GeoLocation(
                            latitude = tempLatitude,
                            longitude = tempLongitude
                        )
                    )
                }
                StatusEnum.NoPermission -> {
                    startActivity(IntentHelper.locationServicesPermissionSettings())
                }
                else -> {
                    showErrorPage()
                    Toast.makeText(
                        activity?.applicationContext, R.string.location_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    disableShimmer()
                }
            }
        }
    }

    private var dialogCallback: DialogListener = object : DialogListener {
        override fun onClick(status: StatusEnum) {
            startActivity(IntentHelper.locationAppPermissionSettings())
            isPermissionDialogActive = false
        }

        override fun onCancel(status: StatusEnum) {
            numberOfTries += 1
            handlePermissionResponse(
                locationServicesHelper.requestForLocationPermission(
                )
            )
            isPermissionDialogActive = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getInt("pos")?.let { position = it }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.sharedPreference = SharedPreferenceManager.getSharedPreferenceHelper
        activity?.application?.let {
            viewModel =
                ViewModelProvider(requireActivity(), ViewModelFactory(it)).get(
                    BusStopsViewModel::class.java
                )
        }
        locationServicesHelper = LocationServicesHelper(this.activity as Activity)
        if (NetworkUtil.getNetworkConnection()) {
            // check if busStopCode is empty or missing, retrieve and save to cache
            CacheManager.initializeCacheHelper?.let {
                cacheHelper = it
            }
            if (forceUpdateCache || !cacheHelper.cacheExists() || timeUtil.checkTimeStampExceed3days(
                    sharedPreference.getTimeSharedPreference()
                )
            ) {
                Log.d(TAG, "Cache file does not exists or expired")
                //let background thread handle the heavy workload
                viewModel.retrieveBusStopCodesAndSaveCache()
                sharedPreference.setTimeSharedPreference()
            }

            handlePermissionResponse(locationServicesHelper.checkForLastLocation(locationCallback))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //refetch network status again for second time, since onCreate won't be called anymore
        Log.d(TAG, "Invoke onCreateView here")
        if (NetworkUtil.getNetworkConnection(cache=false)) {
            parentView = inflater.inflate(R.layout.fragment_tab, container, false)
            shimmeringLayoutView = parentView.findViewById(R.id.shimmer_view_container)
            swipeContainer = parentView.findViewById(R.id.swipeContainer)
            enableShimmer()
            expandableListView = parentView.findViewById(R.id.expandableListView)
            Log.d(TAG, "debug expendable $expandableListView")
        } else {
            errorView = activity?.let { fragmentActivity ->
                container?.let {
                    ErrorView(it)
                }
            }
            errorView?.build()?.let {
                parentView = it
            }
        }
        Log.d(TAG, "Return view here $view")
        return parentView
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        Log.d(TAG, "Called onViewCreated here $view")
        super.onViewCreated(view, savedInstanceState)
        if (NetworkUtil.getNetworkConnection()) {
            setListenersForOriginalView()
        } else {
            //callback from ErrorView listener
            errorView?.let { setListenersForErrorView(it) }
        }
    }

    private fun setListenersForErrorView(errorView: ErrorView) {
        //casting is very expensive, do it once here
        errorView.let {
            it.setupErrorListeners {
                if (NetworkUtil.getNetworkConnection(cache=false).not()){
                    Toast.makeText(requireContext(),R.string.disable_network,Toast.LENGTH_SHORT).show()
                    return@setupErrorListeners
                }
                (view as ViewGroup).let { container ->
                    Log.d(FavouriteFragment.TAG, "Number of child views ${container.childCount}")
                    container.removeAllViews()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        parentFragmentManager.beginTransaction().detach(this).commitNow()
                        parentFragmentManager.beginTransaction().attach(this).commitNow()
                    }else{
                        parentFragmentManager.beginTransaction()
                            .detach(this)
                            .attach(this)
                            .commit()
                    }
                }
            }
        }
    }

    private fun showErrorPage() {
        (view as ViewGroup).let { viewgroup ->
            if (errorView == null) {
                activity?.let { errorView = ErrorView(viewgroup) }
            }
            errorView?.let {
                viewgroup.removeAllViews()
                viewgroup.addView(it.build())
                setListenersForErrorView(it)
            }
        }
    }

    private fun setListenersForOriginalView() {
        expandableListView?.apply {
            setOnGroupExpandListener { groupPosition ->
                activity?.let {
                    Toast.makeText(
                        it.applicationContext,
                        (expandableListTitle as ArrayList<String>)[groupPosition] + " List Expanded.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                //don't allow refresh if cell is expanding
                allowRefresh = false
                viewModel.getGeoLocationBasedOnBusStopName((expandableListTitle as ArrayList<String>)[groupPosition])
                observeBusStopCodeModel(
                    (expandableListTitle as ArrayList<String>)[groupPosition]
                )
            }
            setOnGroupCollapseListener { groupPosition ->
                activity?.let {
                    Toast.makeText(
                        it.applicationContext,
                        (expandableListTitle as ArrayList<String>)[groupPosition] + " List Collapsed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
                activity?.let {
                    Toast.makeText(
                        it.applicationContext,
                        (expandableListTitle as ArrayList<String>)[groupPosition] + " -> "
                                + viewModel.expandableNearestListDetail[(expandableListTitle as ArrayList<String>)[groupPosition]]!![childPosition],
                        Toast.LENGTH_SHORT
                    ).show()
                }
                false
            }
        }
        swipeContainer.apply {
            setOnRefreshListener {
                Log.d(TAG, "Refresh here")
                refreshExpandedList(true)
            }
            setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
            )
        }
    }

    private fun observeBusStopCodeModel(
        expandableListTitle: String
    ) {
        Log.d(TAG, "Call bus Stop code list API ")
        // Update the list when the data changes
        val busStopCode =
            viewModel.getExpandableNearestListBusStopCode(expandableListTitle) ?: return
        viewModel.getBusScheduleListObservable(
            busStopCode.busStopCode.toLong(),
            expandableListTitle,
            FragmentType.NEAREST
        )
        allowRefresh = true

    }

    //adapter for 1st screen
    private fun createNearestExpandableListAdapter() {
        viewModel.setUpExpandableListAdapter(FragmentType.NEAREST)
        expandableListAdapter = viewModel.getExpandableNearestListAdapter()
        expandableListTitle = viewModel.getNearestExpandableListTitle()
        expandableListView?.setAdapter(expandableListAdapter)
    }

    private fun observeNearestBusStopsModel(location: GeoLocation) {
        Log.d(TAG, "Call nearest bus stop API ")
        // Update the list when the data changes
        viewModel.getNearestBusStopsGeoListObservable(location)
            ?.observe(viewLifecycleOwner,
                Observer<StatusEnum> { status ->
                    if (status != null) {
                        activity?.let {
                            Toast.makeText(
                                it.applicationContext, "Update headers on page",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (status == StatusEnum.Success) {
                            createNearestExpandableListAdapter()
                        } else {
                            //Show error placeholder page
                            showErrorPage()
                        }
                        disableShimmer()
                    }
                })

    }

    private fun enableShimmer() {
        shimmeringLayoutView?.startShimmerAnimation()
        shimmeringLayoutView?.visibility = View.VISIBLE
    }

    private fun disableShimmer() {
        shimmeringLayoutView?.stopShimmerAnimation()
        shimmeringLayoutView?.visibility = View.INVISIBLE
    }

    private fun refreshExpandedList(swipeRefresh: Boolean) {
        val list: HashMap<String, String>? = getCurrentExpandedList()
        if (!list.isNullOrEmpty()) {
            Log.d(TAG, "List of bus stop code that requires re-fetch are $list")
            viewModel.refreshExpandedBusStops(list, object : StatusCallBack {
                override fun updateOnResult(status: Boolean) {
                    if (status) {
                        if (!swipeRefresh) {
                            allowRefresh = true
                        } else {
                            swipeContainer.isRefreshing = false
                        }
                    } else {
                        //check if nothing retrieved due to network, show error placeholder page
                        showErrorPage()
                    }
                }
            }, FragmentType.NEAREST)
        } else {
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
        return visibleExpandedList
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Resume app here")
        IntentHelper.apply {
            if ((viewModel.getNearestExpandableListSize() == 0).and(navigateToAppSettings)) {
                setNavigationToExternalIntent()
                handlePermissionResponse(
                    locationServicesHelper.checkForLastLocation(
                        locationCallback
                    )
                )
            }
        }
        if (allowRefresh) {
            allowRefresh = false
            refreshExpandedList(false)
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause is called")
        viewModel.destroyDisposable()
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy is called")
        errorView = null
        expandableListView = null
        viewModel.destroyDisposable()
        viewModel.destroyRepositories()
        activity?.viewModelStore?.clear()
        locationServicesHelper.destroyLocationServicesHelper()
        super.onDestroy()
    }

    private fun showPermissionDialog() {
        if (isPermissionDialogActive) {
            return
        }
        val cancellable: Boolean = numberOfTries <= 1
        AlertDialogView(this.requireContext()).showDialog(
            cancellable,
            dialogCallback
        )
        isPermissionDialogActive = true
    }
}
