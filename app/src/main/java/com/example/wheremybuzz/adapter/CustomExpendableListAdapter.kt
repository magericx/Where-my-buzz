package com.example.wheremybuzz.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.wheremybuzz.BusApplication
import com.example.wheremybuzz.R
import com.example.wheremybuzz.model.StoredBusMeta
import com.example.wheremybuzz.utils.helper.sharedpreference.SharedPreferenceManager
import com.example.wheremybuzz.utils.helper.time.TimeUtil
import com.facebook.shimmer.ShimmerFrameLayout
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import javax.inject.Inject


class CustomExpandableListAdapter @Inject constructor(
    private val context: Context, private var expandableListTitle: List<String>,
    private var expandableListDetail: ConcurrentHashMap<String, MutableList<StoredBusMeta>>,
    private var sharedPreferenceManager: SharedPreferenceManager
) : BaseExpandableListAdapter() {

    companion object {
        const val TAG = "CustomExpendableListAdapter"
        const val shimmer = "shouldShimmer"
        const val shimmer2 = "noShimmer"
    }

    private val poolThread: ExecutorService = BusApplication.poolThread
    private val mainThread: Handler = BusApplication.mainThreadHandler


    fun refreshExpandableList(
        listDetail: ConcurrentHashMap<String, MutableList<StoredBusMeta>>,
        listTitle: List<String>
    ) {
        this.expandableListDetail = listDetail
        this.expandableListTitle = listTitle
        notifyDataSetChanged()
    }

    override fun getChild(listPosition: Int, expandedListPosition: Int): StoredBusMeta? {
        return expandableListDetail[expandableListTitle[listPosition]]
            ?.get(expandedListPosition)
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    private fun getLayoutInflator(): LayoutInflater {
        return context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    @SuppressLint("LongLogTag")
    override fun getChildView(
        listPosition: Int, expandedListPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View? {

        var convertView: View? = convertView
        val shimmeringLayoutView: ShimmerFrameLayout?
        val shouldShowShimmer: Boolean
        val expandedListText =
            getChild(listPosition, expandedListPosition)

        //check if there are values to be shown
        shouldShowShimmer = expandedListText?.Services?.ServiceNo == null
        Log.d(TAG, "Current convertView is $convertView")

        if (convertView == null) {
            convertView = if (shouldShowShimmer) getLayoutInflator().inflate(
                R.layout.list_item_placeholder,
                null
            ) else getLayoutInflator().inflate(R.layout.list_item, null)
            shimmeringLayoutView = convertView.findViewById(R.id.shimmer_list_view_container)
            shimmeringLayoutView?.startShimmerAnimation()
        } else {
            //use getTag and setTag to solve reassignment issue
            //need to change logic to if convertView != null & instanceOf Shimmering
            if (convertView.tag == shimmer) {
                if (!shouldShowShimmer) {
                    convertView = getLayoutInflator().inflate(R.layout.list_item, null)
                }
            } else if (convertView.tag == shimmer2) {
                if (shouldShowShimmer) {
                    convertView = getLayoutInflator().inflate(R.layout.list_item_placeholder, null)
                }
            }
        }
        //tag will be used for next iteration
        if (shouldShowShimmer) convertView?.tag = shimmer else convertView?.tag = shimmer2
        if (expandedListText?.Services?.ServiceNo != null) {
            val busNumber = convertView
                ?.findViewById<View>(R.id.busNumber) as TextView
            val firstArriveTime = convertView
                .findViewById<View>(R.id.firstArriveTime) as TextView
            val firstBusIcon = convertView
                .findViewById<View>(R.id.firstBusIcon) as ImageView
            val secondArriveTime = convertView
                .findViewById<View>(R.id.secondArriveTime) as TextView
            val secondBusIcon = convertView
                .findViewById<View>(R.id.secondBusIcon) as ImageView
            val thirdArriveTime = convertView
                .findViewById<View>(R.id.thirdArriveTime) as TextView
            val thirdBusIcon = convertView
                .findViewById<View>(R.id.thirdBusIcon) as ImageView
            //busNumber.text = expandedListText.toString()
            //Log.d(TAG, "Retrieved expanddedListText is : $expandedListText")
            if (expandedListText.Services != null) {
                busNumber.text = expandedListText.Services!!.ServiceNo
                setArriveTime(expandedListText.Services!!.NextBus.EstimatedArrival, firstArriveTime)
                setBusType(expandedListText.Services!!.NextBus.Type, firstBusIcon)
                setArriveTime(
                    expandedListText.Services!!.NextBus2.EstimatedArrival,
                    secondArriveTime
                )
                setBusType(expandedListText.Services!!.NextBus2.Type, secondBusIcon)
                setArriveTime(
                    expandedListText.Services!!.NextBus3.EstimatedArrival,
                    thirdArriveTime
                )
                setBusType(expandedListText.Services!!.NextBus3.Type, thirdBusIcon)
            } else {
                busNumber.text = context.getString(R.string.not_available)
            }
        }

        //change to include fields to show
        return convertView
    }

    private fun setArriveTime(arriveTime: String, arriveTimeLabel: TextView) {
        if (arriveTime.isNotBlank()) {
            val calculateFirstArriveTime = TimeUtil.retrieveDifferenceFromNow(arriveTime)
            if (calculateFirstArriveTime.isEmpty()) {
                arriveTimeLabel.text = context.getString(R.string.not_applicable)
            } else {
                when (calculateFirstArriveTime) {
                    "0", "ARR" -> arriveTimeLabel.text = context.getString(R.string.arrive)
                    //"ARR" -> arriveTimeLabel.text = context.getString(R.string.arrive)
                    else -> {
                        arriveTimeLabel.text = calculateFirstArriveTime
                    }
                }
            }
        }
    }

    private fun setBusType(busType: String, busIcon: ImageView) {
        if (!busType.isBlank()) {
            when (busType) {
                "SD" -> busIcon.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.single_deck,
                        null
                    )
                )
                "DD" -> busIcon.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.double_deck,
                        null
                    )
                )
                "BD" -> busIcon.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.bendy_bus,
                        null
                    )
                )
            }
        }
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return expandableListDetail[expandableListTitle[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return expandableListTitle[listPosition]
    }

    override fun getGroupCount(): Int {
        return expandableListTitle.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView
        val listTitle = getGroup(listPosition) as String
        val busStopCode = getChild(listPosition, 0)?.BusStopCode
        if (convertView == null) {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_group, null)
        }
        val listTitleTextView = convertView
            ?.findViewById<View>(R.id.listTitle) as TextView
        listTitleTextView.setTypeface(null, Typeface.BOLD)
        listTitleTextView.text = listTitle
        val listAddress = convertView.findViewById<View>(R.id.listTitleAddress) as TextView
        listAddress.text = busStopCode
        val starButton = convertView
            .findViewById<View>(R.id.starButton) as ImageButton
        starButton.isFocusable = false
        Log.d(TAG, "Retrieved busStopCode is $busStopCode")
        if (busStopCode != null) {
            setListenerForStar(starButton, listTitle, busStopCode)
        }
        return convertView
    }

    private fun setListenerForStar(
        starButton: ImageButton,
        busStopName: String,
        busStopCode: String
    ) {
        val sharedPreference = sharedPreferenceManager.getFavouriteSharedPreferenceHelper
        poolThread.execute {
            if (sharedPreference.checkIfExistsInList(busStopCode)) {
                mainThread.post {
                    starButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            android.R.drawable.btn_star_big_on
                        )
                    )
                    starButton.setTag(R.string.button_view_tag, true)
                }
            }
        }
        starButton.setOnClickListener {
            val previousTag = starButton.getTag(R.string.button_view_tag) ?: false
            if (previousTag as Boolean) {
                poolThread.execute {
                    sharedPreference.removeSharedPreferenceFromList(busStopCode)
                }
                starButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        android.R.drawable.btn_star_big_off
                    )
                )
            } else {
                poolThread.execute {
                    sharedPreference.appendSharedPreferenceIntoList(busStopName, busStopCode)
                }
                starButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        android.R.drawable.btn_star_big_on
                    )
                )
            }
            starButton.setTag(R.string.button_view_tag, !previousTag)
        }
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(
        listPosition: Int,
        expandedListPosition: Int
    ): Boolean {
        return true
    }

}