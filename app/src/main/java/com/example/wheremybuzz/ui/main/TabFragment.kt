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
import com.example.wheremybuzz.model.BusStopMeta
import com.example.wheremybuzz.model.BusStopsCodeResponse
import com.example.wheremybuzz.viewModel.NearestBusStopsViewModel


class TabFragment : Fragment() {
    var position = 0
    val TAG: String = "TabFragment"

    var expandableListView: ExpandableListView? = null
    var expandableListAdapter: ExpandableListAdapter? = null
    var expandableListTitle: List<String>? = null
    var expandableListDetail: HashMap<String, List<String>>? = null
    var viewModel: NearestBusStopsViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments!!.getInt("pos")
        //getNearestBusStopsData()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProvider(requireActivity(), ViewModelFactory(activity!!.application)).get(
                NearestBusStopsViewModel::class.java
            )
        observeViewModel()
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
        //textView = view.findViewById<View>(R.id.textView) as TextView
        //textView!!.text = "Fragment " + (position + 1)

        //expandableListView!!.setAdapter(expandableListAdapter)
        expandableListView!!.setOnGroupExpandListener { groupPosition ->
            Toast.makeText(
                activity!!.getApplicationContext(),
                (expandableListTitle as ArrayList<String>).get(groupPosition) + " List Expanded.",
                Toast.LENGTH_SHORT
            ).show()
        }

        expandableListView!!.setOnGroupCollapseListener { groupPosition ->
            Toast.makeText(
                activity!!.getApplicationContext(),
                (expandableListTitle as ArrayList<String>).get(groupPosition) + " List Collapsed.",
                Toast.LENGTH_SHORT
            ).show()
        }

        expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            Toast.makeText(
                activity!!.getApplicationContext(),
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
                    val busStopArrayList: MutableList<String> = ArrayList()
                    viewModel?.setExpandableListDetail(
                        nearestBusStopsList[i]!!.busStopName,
                        busStopArrayList
                    )
                }
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
        }
    }

    private fun observeViewModel() {
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
                            observeBusStopCodeViewModel("test",0.0,0.0)
                            Log.d(
                                TAG,
                                "getNearestBusStopsGeoListObservable API result is $nearestBusStopMeta"
                            )
                        }
                    })
        }
    }

    private fun observeBusStopCodeViewModel(
        busStopName: String,
        latitude: Double,
        longtitude: Double
    ) {
        if (position == 0) {
            Log.d(TAG, "Call nearest bus stop API ")
            // Update the list when the data changes
            viewModel?.getBusStopCodeListObservable(busStopName, latitude, longtitude)
                ?.observe(viewLifecycleOwner,
                    Observer<BusStopsCodeResponse> { nearestBusStopMeta ->
                        if (nearestBusStopMeta != null) {
                            Toast.makeText(
                                activity!!.applicationContext, "Retrieved bus stop code",
                                Toast.LENGTH_SHORT
                            ).show()
                            //createBusStopNameHeader(nearestBusStopMeta)
                            Log.d(TAG, "API result is $nearestBusStopMeta")
                        }
                    })
        }
    }


    companion object {
        fun getInstance(position: Int): Fragment {
            val bundle = Bundle()
            bundle.putInt("pos", position)
            val tabFragment = TabFragment()
            tabFragment.setArguments(bundle)
            return tabFragment
        }

        private val location: String = "1.380308, 103.741256"
    }
}