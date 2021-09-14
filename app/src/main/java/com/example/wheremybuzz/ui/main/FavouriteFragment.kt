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
import com.example.wheremybuzz.model.callback.StatusCallBack
import com.example.wheremybuzz.utils.helper.network.NetworkUtil
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceHelper
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceManager
import com.example.wheremybuzz.view.ErrorView
import com.example.wheremybuzz.viewModel.NearestBusStopsViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import enum.FragmentType

class FavouriteFragment : Fragment() {

    companion object {
        const val TAG = "SecondFragment"
        fun getInstance(position: Int): Fragment {
            Log.d(TAG, "Loaded second fragment here")
            val bundle = Bundle()
            bundle.putInt("pos", position)
            val tabFragment = FavouriteFragment()
            tabFragment.arguments = bundle
            return tabFragment
        }
    }

    private var enabledNetwork: Boolean = false
    lateinit var sharedPreference: SharedPreferenceHelper
    private lateinit var viewModel: NearestBusStopsViewModel
    var shimmeringLayoutView: ShimmerFrameLayout? = null
    private lateinit var expandableListView: ExpandableListView
    private lateinit var swipeContainer: SwipeRefreshLayout
    lateinit var expandableListTitle: List<String>
    lateinit var expandableListAdapter: ExpandableListAdapter
    private var errorView: ErrorView? = null

    private var allowRefresh = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enabledNetwork = NetworkUtil.haveNetworkConnection()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sharedPreference = SharedPreferenceManager.getFavouriteSharedPreferenceHelper
        viewModel =
            ViewModelProvider(requireActivity(), ViewModelFactory(activity!!.application)).get(
                NearestBusStopsViewModel::class.java
            )
        if (enabledNetwork) {
            observeFavouriteBusStopsModel()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        enabledNetwork = NetworkUtil.haveNetworkConnection()
        val view: View?
        if (enabledNetwork) {
            view = inflater.inflate(R.layout.fragment_tab, container, false)
            shimmeringLayoutView = view.findViewById(R.id.shimmer_view_container)
            swipeContainer = view.findViewById(R.id.swipeContainer)
            enableShimmer()
            expandableListView = view.findViewById(R.id.expandableListView)
            Log.d(TAG, "debug expendable $expandableListView")
        } else {
            errorView = ErrorView(container!!)
            view = errorView!!.build()
        }
        return view as View
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (enabledNetwork) {
            setListenersForOriginalView()
        } else {
            setListenersForErrorView(errorView!!)
        }
    }

    private fun setListenersForErrorView(errorView: ErrorView) {
        errorView.let {
            it.setupErrorListeners {
                (view as ViewGroup).let {
                    Log.d(TAG, "Number of child views ${it.childCount}")
                    it.removeAllViews()
                    parentFragmentManager.beginTransaction()
                        .detach(this)
                        .attach(this)
                        .commit()
                }
            }
        }
    }

    private fun showErrorPage() {
        (view as ViewGroup).let {
            if (errorView == null) {
                errorView = ErrorView(it)
            }
            it.removeAllViews()
            it.addView(errorView!!.build())
            setListenersForErrorView(errorView!!)
        }
    }

    private fun setListeners() {
        swipeContainer.setOnRefreshListener { refreshExpandedList(swipeRefresh = true) }
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
            }, FragmentType.FAVOURITE)
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
            val expanded = expandableListView.isGroupExpanded(i) ?: false
            Log.d(TAG, "group position number $i is $expanded")
            if (expanded) {
                val busStopCode = (expandableListAdapter.getChild(
                    i, 1
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

    private fun enableShimmer() {
        shimmeringLayoutView?.startShimmerAnimation()
        shimmeringLayoutView?.visibility = View.VISIBLE
    }

    private fun disableShimmer() {
        shimmeringLayoutView?.stopShimmerAnimation()
        shimmeringLayoutView?.visibility = View.INVISIBLE
    }

    private fun setListenersForOriginalView() {
        expandableListView.setOnGroupExpandListener { groupPosition ->
            Toast.makeText(
                activity!!.applicationContext,
                (expandableListTitle as ArrayList<String>)[groupPosition] + " List Expanded.",
                Toast.LENGTH_SHORT
            ).show()
            //don't allow refresh if cell is expanding
            allowRefresh = false
//            val geoLocation =
//                viewModel.getGeoLocationBasedOnBusStopName((expandableListTitle as ArrayList<String>)[groupPosition])
            observeBusStopCodeModel(
                (expandableListTitle as ArrayList<String>)[groupPosition]
            )
        }

        expandableListView.setOnGroupCollapseListener { groupPosition ->
            Toast.makeText(
                activity!!.applicationContext,
                (expandableListTitle as ArrayList<String>)[groupPosition] + " List Collapsed.",
                Toast.LENGTH_SHORT
            ).show()
        }

        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            Toast.makeText(
                activity!!.applicationContext,
                (expandableListTitle as ArrayList<String>)[groupPosition] + " -> "
                        + viewModel.expandableFavouriteListDetail[(expandableListTitle as ArrayList<String>)[groupPosition]]!![childPosition],
                Toast.LENGTH_SHORT
            ).show()
            false
        }
//        swipeContainer.setOnRefreshListener {
//            refreshExpandedList(true)
//        }
//        swipeContainer.setColorSchemeResources(
//            android.R.color.holo_blue_bright,
//            android.R.color.holo_green_light,
//            android.R.color.holo_orange_light,
//            android.R.color.holo_red_light
//        )
        setListeners()
    }

    private fun observeBusStopCodeModel(
        expandableListTitle: String
    ) {
        Log.d(TAG, "Call bus Stop code list API ")
        val busStopCode =
            viewModel.getExpandableFavouriteListBusStopCode(expandableListTitle) ?: return
        viewModel.getBusScheduleListObservable(
            busStopCode.busStopCode.toLong(),
            expandableListTitle, FragmentType.FAVOURITE
        )
        allowRefresh = true

    }

    private fun observeFavouriteBusStopsModel() {
        // Update the list when the data changes
        if (sharedPreference.checkIfListIsEmpty()) {
            disableShimmer()
            showErrorPage()
            return
        }
        val listOfBusStopCodes = sharedPreference.getSharedPreferenceAsMap() ?: return
        viewModel.getFavouriteBusStopsGeoListObservable(listOfBusStopCodes)
            ?.observe(viewLifecycleOwner,
                Observer<StatusEnum> { status ->
                    if (status != null) {
                        Toast.makeText(
                            activity!!.applicationContext, "Update headers on page",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (status == StatusEnum.Success) createFavouriteExpandableListAdapter()
                        disableShimmer()
                    }
                })
    }

    //adapter for 2nd screen
    private fun createFavouriteExpandableListAdapter() {
        viewModel.setUpExpandableListAdapter(FragmentType.FAVOURITE)
        expandableListAdapter = viewModel.getexpandableFavouriteListAdapter()
        expandableListTitle = viewModel.getFavouriteExpandableListTitle()
        expandableListView.setAdapter(expandableListAdapter)
    }

    override fun onPause() {
        Log.d(TAG, "onPause is called")
        viewModel.destroyDisposable()
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy is called")
        errorView = null
        viewModel.destroyDisposable()
        viewModel.destroyRepositories()
        activity?.viewModelStore?.clear()
        super.onDestroy()
    }


}