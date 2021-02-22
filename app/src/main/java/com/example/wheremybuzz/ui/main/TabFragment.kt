package com.example.wheremybuzz.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.example.wheremybuzz.R
import com.example.wheremybuzz.adapter.CustomExpandableListAdapter
import com.example.wheremybuzz.data.ExpandableListDataPump.data
import kotlinx.android.synthetic.main.fragment_tab.expandableListView


class TabFragment : Fragment() {
    var position = 0

    var expandableListView: ExpandableListView? = null
    var expandableListAdapter: ExpandableListAdapter? = null
    var expandableListTitle: List<String>? = null
    var expandableListDetail: HashMap<String, List<String>>? = null
    private var textView: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = getArguments()!!.getInt("pos")


        expandableListDetail = data
        expandableListTitle = ArrayList<String>(expandableListDetail!!.keys)
        expandableListAdapter =
            CustomExpandableListAdapter(
                activity!!.applicationContext,
                expandableListTitle!!,
                expandableListDetail!!
            )
        println(expandableListAdapter)
        println(expandableListView)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view:View =  inflater.inflate(R.layout.fragment_tab, container, false)
        expandableListView = view.findViewById(R.id.expandableListView)
        return view
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //textView = view.findViewById<View>(R.id.textView) as TextView
        //textView!!.text = "Fragment " + (position + 1)

        expandableListView!!.setAdapter(expandableListAdapter)
//        expandableListView!!.setOnGroupExpandListener { groupPosition ->
//            Toast.makeText(
//                activity!!.getApplicationContext(),
//                (expandableListTitle as ArrayList<String>).get(groupPosition) + " List Expanded.",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//
//        expandableListView!!.setOnGroupCollapseListener { groupPosition ->
//            Toast.makeText(
//                activity!!.getApplicationContext(),
//                (expandableListTitle as ArrayList<String>).get(groupPosition) + " List Collapsed.",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//
//        expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
//            Toast.makeText(
//                activity!!.getApplicationContext(),
//                (expandableListTitle as ArrayList<String>).get(groupPosition) + " -> "
//                        + expandableListDetail!![(expandableListTitle as ArrayList<String>).get(
//                    groupPosition
//                )]!![childPosition],
//                Toast.LENGTH_SHORT
//            ).show()
//            false
//        }
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