package com.example.wheremybuzz.ui.main

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.location.Location
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wheremybuzz.R
import com.example.wheremybuzz.enum.FragmentType
import com.example.wheremybuzz.model.GeoLocation
import com.example.wheremybuzz.model.StatusEnum
import com.example.wheremybuzz.model.StoredBusMeta
import com.example.wheremybuzz.model.callback.StatusCallBack
import com.example.wheremybuzz.utils.helper.cache.CacheHelper
import com.example.wheremybuzz.utils.helper.cache.CacheManager
import com.example.wheremybuzz.utils.helper.intent.IntentHelper
import com.example.wheremybuzz.utils.helper.permission.*
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceHelper
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceManager
import com.example.wheremybuzz.utils.helper.time.TimeUtil
import com.example.wheremybuzz.view.AlertDialogView
import com.example.wheremybuzz.view.DialogListener
import com.example.wheremybuzz.view.error.ErrorView
import com.example.wheremybuzz.viewModel.BusStopsViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@AndroidEntryPoint
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
    private val viewModel: BusStopsViewModel by activityViewModels()


    @Inject lateinit var sharedPreferenceManager: SharedPreferenceManager
    private lateinit var sharedPreference: SharedPreferenceHelper
    private lateinit var cacheHelper: CacheHelper
    private var allowRefresh = false
    lateinit var parentView: View
    private var errorView: ErrorView? = null
    private var isPermissionDialogActive: Boolean = false
    private lateinit var locationServicesHelper: LocationServicesHelper
    private lateinit var expandedGrouplistView: ArrayList<Int>

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
            locationServicesHelper.retrieveLastLocation(iLocationCallback)
        }

    private fun handlePermissionResponse(permissionArray: Array<String>?) {
        permissionArray.let {
            requestMultiplePermissions.launch(permissionArray)
        }
    }

    private var iLocationCallback: ILocationCallback = object : ILocationCallback {
        override fun updateOnResult(location: Location?, statusEnum: StatusEnum) {
            when (statusEnum) {
                StatusEnum.Success -> {
                    loadComponents(
                        location
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
            showErrorPage()
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
        this.sharedPreference = sharedPreferenceManager.getSharedPreferenceHelper
        locationServicesHelper = LocationServicesHelper(this.activity as Activity)
        if (viewModel.getNetworkConnection()) {
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

            handlePermissionResponse(locationServicesHelper.checkForLastLocation(iLocationCallback))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //refetch network status again for second time, since onCreate won't be called anymore
        Log.d(TAG, "Invoke onCreateView here")
        if (viewModel.getNetworkConnection(cache = false)) {
            parentView = inflater.inflate(R.layout.fragment_tab, container, false)
            shimmeringLayoutView = parentView.findViewById(R.id.shimmer_view_container)
            swipeContainer = parentView.findViewById(R.id.swipeContainer)
            enableShimmer()
            expandableListView = parentView.findViewById(R.id.expandableListView)
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
        if (viewModel.getNetworkConnection()) {
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
                if (viewModel.getNetworkConnection(cache = false).not()) {
                    Toast.makeText(requireContext(), R.string.disable_network, Toast.LENGTH_SHORT)
                        .show()
                    return@setupErrorListeners
                }
                (view as ViewGroup).let { container ->
                    Log.d(FavouriteFragment.TAG, "Number of child views ${container.childCount}")
                    container.removeAllViews()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        parentFragmentManager.beginTransaction().detach(this).commitNow()
                        parentFragmentManager.beginTransaction().attach(this).commitNow()
                    } else {
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
        shimmeringLayoutView?.let{
            if (it.isAnimationStarted){
                disableShimmer()
            }
        }
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
        expandedGrouplistView = arrayListOf()
        expandableListView?.apply {
            setOnGroupExpandListener { groupPosition ->
                val expandableListTitle = viewModel.getNearestExpandableListTitle()
                expandedGrouplistView.add(groupPosition)
                //don't allow refresh if cell is expanding
                allowRefresh = false
                viewModel.getGeoLocationBasedOnBusStopName((expandableListTitle as ArrayList<String>)[groupPosition])
                observeBusStopCodeModel(
                    expandableListTitle[groupPosition]
                )
            }
            setOnGroupCollapseListener { groupPosition ->
                expandedGrouplistView.remove(groupPosition)
            }
            setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
                val expandableListTitle = viewModel.getNearestExpandableListTitle()
                activity?.let {
                    Toast.makeText(
                        it.applicationContext,
                        (expandableListTitle as ArrayList<String>)[groupPosition] + " -> "
                                + viewModel.expandableNearestListDetail[expandableListTitle[groupPosition]]!![childPosition],
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
        Log.d(TAG,"Receive callback here")
        viewModel.setUpExpandableListAdapter(FragmentType.NEAREST)
        expandableListAdapter = viewModel.getExpandableNearestListAdapter()
        expandableListView?.setAdapter(expandableListAdapter)
    }

    private fun loadComponents(location: Location?) {
        location?.let {
            (requireActivity() as ILocationCallback).updateOnResult(location, StatusEnum.Success)
            var tempLatitude: Double
            var tempLongitude: Double
            location.let {
                it.latitude.let { latitude ->
                    tempLatitude = latitude
                }
                it.longitude.let { longitude ->
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
    }


    private fun observeNearestBusStopsModel(location: GeoLocation) {
        Log.d(TAG, "Call nearest bus stop API ")
        // Update the list when the data changes
        viewModel.getNearestBusStopsGeoListObservable(location)
            .observe(viewLifecycleOwner,
                Observer<StatusEnum> { status ->
                    if (status != null) {
                        activity?.let {
                            Toast.makeText(
                                it.applicationContext, "Update headers on page",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        when (status){
                            StatusEnum.Success -> createNearestExpandableListAdapter()
                            StatusEnum.ReloadAll -> collapseExpandableGroupViews()
                            else -> showErrorPage()
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
        if (expandedGrouplistView.isEmpty()) {
            if (swipeRefresh) {
                swipeContainer.isRefreshing = false
            }
            return
        }
        val list: HashMap<String, String> = getCurrentExpandedList()
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
        }
    }

    //method that will check for the expanded items and add into hashmap <busStopCode,busStopName>
    private fun getCurrentExpandedList(): HashMap<String, String> {
        val visibleExpandedList: HashMap<String, String> = hashMapOf()
        expandedGrouplistView.forEach {
            val busStopCode = (expandableListAdapter.getChild(
                it, firstIndex
            ) as StoredBusMeta).BusStopCode
            val busStopName = (expandableListAdapter.getGroup(
                it
            )
                    ).toString()
            if (busStopCode.isNotEmpty() && busStopName.isNotEmpty()) {
                visibleExpandedList[busStopCode] = busStopName
            }
        }
        return visibleExpandedList
    }

    //method to collapse all the expanded groups
    private fun collapseExpandableGroupViews(){
        //copy first to prevent concurrent modification
        val iterationList: ArrayList<Int> = arrayListOf()
        iterationList.addAll(expandedGrouplistView)
        for (item in iterationList){
            Log.d(TAG, "Removing position $item}")
            expandableListView?.collapseGroup(item)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Resume app here")
        IntentHelper.apply {
            if ((viewModel.getNearestExpandableListSize() == 0).and(navigateToAppSettings)) {
                setNavigationToExternalIntent()
                handlePermissionResponse(
                    locationServicesHelper.checkForLastLocation(
                        iLocationCallback
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
        if (swipeContainer.isRefreshing) {
            swipeContainer.isRefreshing = false
            allowRefresh = true
        }
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
        AlertDialogView(this.requireContext()).showDialog(
            true,
            dialogCallback
        )
        isPermissionDialogActive = true
    }
}
