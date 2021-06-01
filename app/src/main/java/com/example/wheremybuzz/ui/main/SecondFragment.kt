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
import com.example.wheremybuzz.utils.helper.network.NetworkUtil
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceHelper
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceManager
import com.example.wheremybuzz.viewModel.NearestBusStopsViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton

class SecondFragment : Fragment() {

    companion object {
        const val TAG = "SecondFragment"
        fun getInstance(position: Int): Fragment {
            Log.d(TAG, "Loaded second fragment here")
            val bundle = Bundle()
            bundle.putInt("pos", position)
            val tabFragment = SecondFragment()
            tabFragment.arguments = bundle
            return tabFragment
        }
    }

    private var enabledNetwork: Boolean = false
    lateinit var sharedPreference: SharedPreferenceHelper
    private lateinit var viewModel: NearestBusStopsViewModel
    lateinit var swipeContainer: SwipeRefreshLayout
    var shimmeringLayoutView: ShimmerFrameLayout? = null
    private lateinit var expandableListView: ExpandableListView
    lateinit var errorButton: MaterialButton
    lateinit var expandableListTitle: List<String>
    lateinit var expandableListAdapter: ExpandableListAdapter

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
            swipeContainer = view.findViewById(R.id.swipeContainer)!!
            enableShimmer()
            expandableListView = view.findViewById(R.id.expandableListView)
            Log.d(TAG, "debug expendable $expandableListView")
        } else {
            view = inflateErrorPage(inflater, container)
        }
        return view as View
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (enabledNetwork) {
            setListenersForOriginalView()
        } else {
            setListenersForErrorView()
        }
    }

    private fun inflateErrorPage(inflater: LayoutInflater, container: ViewGroup?): View? {
        val view = inflater.inflate(R.layout.error_placeholder_layout, container, false)
        Log.d(TAG, "Inflate errorview here $view")
        errorButton = view.findViewById(R.id.restartApp)
        return view
    }

    private fun setListenersForErrorView() {
        errorButton.setOnClickListener {
            //casting is very expensive, do it once here
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

    private fun showErrorPage() {
        (view as ViewGroup).let {
            it.removeAllViews()
            val li = LayoutInflater.from(context)
            it.addView(inflateErrorPage(li, it))
            setListenersForErrorView()
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

    private fun setListenersForOriginalView() {
        expandableListView.setOnGroupExpandListener { groupPosition ->
            Toast.makeText(
                activity!!.applicationContext,
                (expandableListTitle as ArrayList<String>)[groupPosition] + " List Expanded.",
                Toast.LENGTH_SHORT
            ).show()
            //don't allow refresh if cell is expanding
            allowRefresh = false
            val geoLocation =
                viewModel.getGeoLocationBasedOnBusStopName((expandableListTitle as ArrayList<String>)[groupPosition])
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
                        + viewModel.getExpandableNearestListDetail()[(expandableListTitle as ArrayList<String>)[groupPosition]]!![childPosition],
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
    }

    private fun observeBusStopCodeModel(
        expandableListTitle: String
    ) {
        Log.d(TAG, "Call bus Stop code list API ")
        // Update the list when the data changes
        //viewModel?.getBusStopCodeListObservable(expandableListTitle, latitude, longtitude)
        val busStopCode = viewModel.getExpandableListBusStopCode(expandableListTitle)
        viewModel.getBusScheduleListObservable(
            busStopCode.busStopCode.toLong(),
            expandableListTitle
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
        expandableListAdapter = viewModel.setUpExpandableListAdapter(1)
        expandableListTitle = viewModel.getFavouriteExpandableListTitle()
        expandableListView.setAdapter(expandableListAdapter)
    }


}