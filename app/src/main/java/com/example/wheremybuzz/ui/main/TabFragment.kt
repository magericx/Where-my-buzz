package com.example.wheremybuzz.ui.main

import android.content.pm.PackageManager
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
import com.example.wheremybuzz.api.NearestBusStopApiService
import com.example.wheremybuzz.data.ExpandableListDataPump.data
import com.example.wheremybuzz.model.NearestBusStopsResponse
import com.example.wheremybuzz.viewModel.NearestBusStopsViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


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
        if (position == 0) {
            expandableListDetail = data
            expandableListTitle = ArrayList<String>(expandableListDetail!!.keys)
            expandableListAdapter =
                CustomExpandableListAdapter(
                    activity!!.applicationContext,
                    expandableListTitle!!,
                    expandableListDetail!!
                )
        }

        //getNearestBusStopsData()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(),ViewModelFactory(activity!!.application)).get(
            NearestBusStopsViewModel::class.java
        )
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

        expandableListView!!.setAdapter(expandableListAdapter)
        expandableListView!!.setOnGroupExpandListener { groupPosition ->
//            Toast.makeText(
//                activity!!.getApplicationContext(),
//                (expandableListTitle as ArrayList<String>).get(groupPosition) + " List Expanded.",
//                Toast.LENGTH_SHORT
//            ).show()
            observeViewModel(viewModel!!)
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
                (expandableListTitle as ArrayList<String>).get(groupPosition) + " -> "
                        + expandableListDetail!![(expandableListTitle as ArrayList<String>).get(
                    groupPosition
                )]!![childPosition],
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

//    private fun observeViewModel(viewModel: NearestBusStopsViewModel) {
//        // Update the list when the data changes
//        viewModel.getNearestBusStopsListObservable()
//            ?.observe(viewLifecycleOwner,
//                Observer<List<NearestBusStopsResponse>> { projects ->
//                    if (projects != null) {
//                        Toast.makeText(
//                            activity!!.applicationContext, "API result is ${projects}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        Log.d(TAG,"API result is " + projects)
//                    }
//                })
//    }

    private fun observeViewModel(viewModel: NearestBusStopsViewModel) {
        // Update the list when the data changes
        viewModel.getNearestBusStopsGeoListObservable()
            ?.observe(viewLifecycleOwner,
                Observer<String> { geoLocation ->
                    if (geoLocation != null) {
                        Toast.makeText(
                            activity!!.applicationContext, "API result is $geoLocation",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "API result is $geoLocation")
                    }
                })
    }

    companion object {
        fun getInstance(position: Int): Fragment {
            val bundle = Bundle()
            bundle.putInt("pos", position)
            val tabFragment = TabFragment()
            tabFragment.setArguments(bundle)
            return tabFragment
        }
    }
}