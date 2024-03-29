package com.example.wheremybuzz.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wheremybuzz.BusApplication
import com.example.wheremybuzz.R
import com.facebook.shimmer.ShimmerFrameLayout

class NearestBusView(context: Context, view: ViewGroup){
    var mContext:Context = BusApplication.instance.applicationContext
    private val container = view
    lateinit var swipeContainer: SwipeRefreshLayout
    lateinit var shimmeringLayoutView: ShimmerFrameLayout
    //var shimmeringLayoutView: ShimmerFrameLayout? = null
    lateinit var expandableListView: ExpandableListView
    private var parentView: View? = null

    companion object{
        private val TAG = "NearestBusView"
    }

    fun build(): View{
        if (parentView == null){
            onViewInit(mContext,container)
        }
        return parentView!!
    }

    private fun onViewInit(context: Context, container: ViewGroup) {
        val inflater = getInflater(context)
        parentView  = inflater.inflate(R.layout.fragment_tab, container, false)
        shimmeringLayoutView = parentView!!.findViewById(R.id.shimmer_view_container)
        swipeContainer = parentView!!.findViewById(R.id.swipeContainer)
        expandableListView = parentView!!.findViewById(R.id.expandableListView)
    }


    private fun getInflater(context: Context): LayoutInflater {
        return LayoutInflater.from(context)
    }

    fun hideShimmeringLayout(){
        shimmeringLayoutView.visibility = INVISIBLE
    }

    fun hideSwipeContainer(){
        swipeContainer.visibility = INVISIBLE
    }

    fun enableShimmer() {
        Log.d(TAG,"shimmeringLayoutView is $shimmeringLayoutView")
        shimmeringLayoutView.startShimmerAnimation()
        shimmeringLayoutView.visibility = VISIBLE
    }

    fun disableShimmer() {
        shimmeringLayoutView.stopShimmerAnimation()
        shimmeringLayoutView.visibility = INVISIBLE
    }
}