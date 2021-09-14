package com.example.wheremybuzz.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wheremybuzz.LocationConstants
import com.example.wheremybuzz.MyApplication
import com.example.wheremybuzz.R
import com.example.wheremybuzz.ViewModelFactory
import com.example.wheremybuzz.model.GeoLocation
import com.example.wheremybuzz.model.PermissionEnum
import com.example.wheremybuzz.model.StatusEnum
import com.example.wheremybuzz.model.StoredBusMeta
import com.example.wheremybuzz.model.callback.StatusCallBack
import com.example.wheremybuzz.utils.helper.cache.CacheHelper
import com.example.wheremybuzz.utils.helper.cache.CacheManager
import com.example.wheremybuzz.utils.helper.network.NetworkUtil
import com.example.wheremybuzz.utils.helper.permission.*
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceHelper
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceManager
import com.example.wheremybuzz.utils.helper.time.TimeUtil
import com.example.wheremybuzz.view.AlertDialogView
import com.example.wheremybuzz.view.DialogListener
import com.example.wheremybuzz.view.ErrorView
import com.example.wheremybuzz.viewModel.NearestBusStopsViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import enum.FragmentType


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
        private const val MY_PERMISSIONS_REQUEST_LOCATION =
            LocationConstants.MY_PERMISSIONS_REQUEST_LOCATION
    }

    private val mContext: Context = MyApplication.instance.applicationContext
    var position = 0
    var shimmeringLayoutView: ShimmerFrameLayout? = null
    var expandableListView: ExpandableListView? = null
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var expandableListAdapter: ExpandableListAdapter
    private lateinit var expandableListTitle: List<String>
    private lateinit var viewModel: NearestBusStopsViewModel

    private lateinit var sharedPreference: SharedPreferenceHelper
    private lateinit var cacheHelper: CacheHelper
    private var allowRefresh = false
    private var enabledNetwork = false
    lateinit var parentView: View
    private var errorView: ErrorView? = null
    var numberOfTries = 0

    private lateinit var locationServicesHelper: LocationServicesHelper

    private var locationCallback: LocationCallback = object : LocationCallback {
        override fun updateOnResult(location: Location?, statusEnum: StatusEnum) {
            if (statusEnum == StatusEnum.Success) {
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
            } else {
                showErrorPage()
                Toast.makeText(
                    activity?.applicationContext, R.string.location_failed,
                    Toast.LENGTH_SHORT
                ).show()
                disableShimmer()
            }
        }
    }

    private var requestPermissionCallback: RequestPermissionCallback =
        object : RequestPermissionCallback {
            override fun updateOnResult(requestStatus: RequestStatus) {
                when (requestStatus.permissionType) {
                    PermissionEnum.Basic ->
                        requestPermissions(
                            LocationPermissionHelper.basicLocationPermission,
                            LocationPermissionHelper.MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    PermissionEnum.Advanced ->
                        requestPermissions(
                            LocationPermissionHelper.advancedLocationPermission,
                            LocationPermissionHelper.MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    else -> {
                        //do nothing here
                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getInt("pos")?.let { position = it }
        enabledNetwork = NetworkUtil.haveNetworkConnection()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.sharedPreference = SharedPreferenceManager.getSharedPreferenceHelper
        activity?.application?.let {
            viewModel =
                ViewModelProvider(requireActivity(), ViewModelFactory(it)).get(
                    NearestBusStopsViewModel::class.java
                )
        }
        locationServicesHelper = LocationServicesHelper(this.activity as Activity)
        if (enabledNetwork) {
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

            locationServicesHelper.checkForLastLocation(requestPermissionCallback, locationCallback)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //refetch network status again for second time, since onCreate won't be called anymore
        enabledNetwork = NetworkUtil.haveNetworkConnection()
        Log.d(TAG, "Invoke onCreateView here")
        Log.d(TAG, "enabledNetwork in onCreateView is $enabledNetwork")
        if (enabledNetwork) {
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
        if (enabledNetwork) {
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
                (view as ViewGroup).let { container ->
                    Log.d(TAG, "Number of child views ${container.childCount}")
                    container.removeAllViews()
                    parentFragmentManager.beginTransaction()
                        .detach(this)
                        .attach(this)
                        .commit()
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
                Log.d(TAG,"Refresh here")
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

    //TODO change to accept location as argument
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
        if (allowRefresh) {
            allowRefresh = false
            Log.d(TAG, "On resume app here")
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mContext.let {
                            ContextCompat.checkSelfPermission(
                                it,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        } == PackageManager.PERMISSION_GRANTED
                    ) {
                        locationServicesHelper.checkForLastLocation(
                            requestPermissionCallback,
                            locationCallback
                        )
                    }
                } else {
                    val cancellable: Boolean = numberOfTries <= 1
                    val dialog = AlertDialogView(this.requireContext()).buildDialog(
                        cancellable,
                        object : DialogListener {
                            override fun onClick(status: StatusEnum) {
                                locationServicesHelper.requestForLocationPermission(
                                    requestPermissionCallback
                                )
                            }

                            override fun onCancel(status: StatusEnum) {
                                numberOfTries += 1
                                locationServicesHelper.requestForLocationPermission(
                                    requestPermissionCallback
                                )
                            }

                        })
                    dialog.show()
                    val button: Button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    button.isEnabled = cancellable
                }
            }
        }
    }
}
