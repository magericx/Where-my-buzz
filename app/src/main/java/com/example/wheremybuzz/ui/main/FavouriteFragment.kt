package com.example.wheremybuzz.ui.main

import android.os.Build
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wheremybuzz.R
//import com.example.wheremybuzz.ViewModelFactory
import com.example.wheremybuzz.model.StatusEnum
import com.example.wheremybuzz.model.StoredBusMeta
import com.example.wheremybuzz.model.callback.StatusCallBack
import com.example.wheremybuzz.utils.helper.network.NetworkUtil
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceHelper
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceManager
import com.example.wheremybuzz.view.ErrorView
import com.example.wheremybuzz.viewModel.BusStopsViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.scopes.FragmentScoped
import com.example.wheremybuzz.enum.FragmentType
import com.example.wheremybuzz.utils.helper.intent.IntentHelper
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@FragmentScoped
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

    var sharedPreference: SharedPreferenceHelper =
        SharedPreferenceManager.getFavouriteSharedPreferenceHelper
    private val viewModel: BusStopsViewModel by activityViewModels()
    var shimmeringLayoutView: ShimmerFrameLayout? = null
    private lateinit var expandableListView: ExpandableListView
    private lateinit var swipeContainer: SwipeRefreshLayout
    lateinit var expandableListTitle: List<String>
    lateinit var expandableListAdapter: ExpandableListAdapter
    lateinit var parentView: View
    private var errorView: ErrorView? = null

    private var allowRefresh = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (viewModel.getNetworkConnection().and(sharedPreference.checkIfListIsEmpty().not())) {
            observeFavouriteBusStopsModel()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Invoke here again")
        if (viewModel.getNetworkConnection(cache = false)
                .and(sharedPreference.checkIfListIsEmpty().not())
        ) {
            parentView = inflater.inflate(R.layout.fragment_tab, container, false)
            shimmeringLayoutView = parentView.findViewById(R.id.shimmer_view_container)
            swipeContainer = parentView.findViewById(R.id.swipeContainer)
            enableShimmer()
            expandableListView = parentView.findViewById(R.id.expandableListView)
        } else {
            errorView = activity?.let {
                container?.let {
                    ErrorView(it)
                }
            }
            errorView?.build()?.let {
                parentView = it
            }
        }
        return parentView
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.getNetworkConnection().and(sharedPreference.checkIfListIsEmpty().not())) {
            setListenersForOriginalView()
        } else {
            setListenersForErrorView(errorView!!)
        }
    }

    private fun setListenersForErrorView(errorView: ErrorView) {
        errorView.let { it ->
            it.setupErrorListeners {
                if (viewModel.getNetworkConnection(cache = false).not()) {
                    Toast.makeText(requireContext(), R.string.disable_network, Toast.LENGTH_SHORT)
                        .show()
                    return@setupErrorListeners
                } else if (sharedPreference.checkIfListIsEmpty()) {
                    Toast.makeText(requireContext(), R.string.empty_preferences, Toast.LENGTH_SHORT)
                        .show()
                    return@setupErrorListeners
                }
                (view as ViewGroup).let {
                    Log.d(TAG, "Number of child views ${it.childCount}")
                    it.removeAllViews()
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

    private fun setListeners() {
        swipeContainer.setOnRefreshListener { refreshExpandedList(swipeRefresh = true) }
    }

    private fun refreshExpandedList(swipeRefresh: Boolean) {
        val list: HashMap<String, String>? = getCurrentExpandedList()
        if (!list.isNullOrEmpty()) {
            Log.d(TAG, "Refresh for favourite fragment here $list")
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
            val expanded = expandableListView.isGroupExpanded(i)
            Log.d(TAG, "group position number $i is $expanded")
            if (expanded) {
                val busStopCode = (expandableListAdapter.getChild(
                    i, 0
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
                requireActivity().applicationContext,
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
                requireActivity().applicationContext,
                (expandableListTitle as ArrayList<String>)[groupPosition] + " List Collapsed.",
                Toast.LENGTH_SHORT
            ).show()
        }

        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            Toast.makeText(
                requireActivity().applicationContext,
                (expandableListTitle as ArrayList<String>)[groupPosition] + " -> "
                        + viewModel.expandableFavouriteListDetail[(expandableListTitle as ArrayList<String>)[groupPosition]]!![childPosition],
                Toast.LENGTH_SHORT
            ).show()
            false
        }
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
                            requireActivity().applicationContext, "Update headers on page",
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


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Resume app here")
        if (allowRefresh) {
            allowRefresh = false
            refreshExpandedList(swipeRefresh = false)
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause is called")
        if (::swipeContainer.isInitialized){
            if (swipeContainer.isRefreshing){
                swipeContainer.isRefreshing = false
                allowRefresh = true
            }
        }
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