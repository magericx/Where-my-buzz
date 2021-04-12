package com.example.wheremybuzz.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.wheremybuzz.R
import com.example.wheremybuzz.model.FinalBusMeta
import com.example.wheremybuzz.model.StoredBusMeta
import com.example.wheremybuzz.utils.TimeUtil
import java.util.*


class CustomExpandableListAdapter(
    private val context: Context, private val expandableListTitle: List<String>,
    private val expandableListDetail: HashMap<String, MutableList<StoredBusMeta>>
) : BaseExpandableListAdapter() {
    val TAG = "CustomExpendableListAdapter"
    override fun getChild(listPosition: Int, expandedListPosition: Int): StoredBusMeta? {
        return expandableListDetail[expandableListTitle[listPosition]]
            ?.get(expandedListPosition)
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    @SuppressLint("LongLogTag")
    override fun getChildView(
        listPosition: Int, expandedListPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View? {

        var convertView = convertView
        val expandedListText =
            getChild(listPosition, expandedListPosition)
        if (convertView == null) {
            val layoutInflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_item, null)
        }
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
        if (expandedListText?.Services != null) {
            busNumber.text = expandedListText.Services!!.ServiceNo
            setArriveTime(expandedListText.Services!!.NextBus.EstimatedArrival, firstArriveTime)
            setBusType(expandedListText.Services!!.NextBus.Type, firstBusIcon)
            setArriveTime(expandedListText.Services!!.NextBus2.EstimatedArrival, secondArriveTime)
            setBusType(expandedListText.Services!!.NextBus2.Type, secondBusIcon)
            setArriveTime(expandedListText.Services!!.NextBus3.EstimatedArrival, thirdArriveTime)
            setBusType(expandedListText.Services!!.NextBus3.Type, thirdBusIcon)
//            Log.d(
//                TAG,
//                "Returned date is ${TimeUtil.retrieveDifferenceFromNow(expandedListText.Services!!.NextBus.EstimatedArrival)}"
//            )
        } else {
            busNumber.text = context.getString(R.string.not_available)
        }


        //change to include fields to show
        return convertView
    }

    private fun setArriveTime(arriveTime: String, arriveTimeLabel: TextView) {
        if (arriveTime.isNotBlank()) {
            val calculateFirstArriveTime = TimeUtil.retrieveDifferenceFromNow(arriveTime)
            if (calculateFirstArriveTime == "0" || calculateFirstArriveTime == "ARR") {
                arriveTimeLabel.text = context.getString(R.string.arrive)
            } else if (calculateFirstArriveTime.isEmpty()) {
                arriveTimeLabel.text = context.getString(R.string.not_applicable)
            } else {
                arriveTimeLabel.text = calculateFirstArriveTime
            }
        }
    }

    private fun setBusType(busType: String, busIcon: ImageView) {
        if (!busType.isBlank()) {
            if (busType == context.getString(R.string.single_deck)) {
                busIcon.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.single_deck,
                        null
                    )
                )
            } else if (busType == context.getString(R.string.double_deck)) {
                busIcon.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.double_deck,
                        null
                    )
                )
            } else if (busType == context.getString(R.string.bendy_deck)) {
                busIcon.setImageDrawable(
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

    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView
        val listTitle = getGroup(listPosition) as String
        if (convertView == null) {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_group, null)
        }
        val listTitleTextView = convertView
            ?.findViewById<View>(R.id.listTitle) as TextView
        listTitleTextView.setTypeface(null, Typeface.BOLD)
        listTitleTextView.text = listTitle
        return convertView
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