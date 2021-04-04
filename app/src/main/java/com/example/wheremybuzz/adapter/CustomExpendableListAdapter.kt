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
import java.util.*


class CustomExpandableListAdapter(
    private val context: Context, private val expandableListTitle: List<String>,
    private val expandableListDetail: HashMap<String, List<FinalBusMeta>>
) : BaseExpandableListAdapter() {
    val TAG = "CustomExpendableListAdapter"
    override fun getChild(listPosition: Int, expandedListPosition: Int): FinalBusMeta? {
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
        Log.d(TAG, "Retrieved expanddedListText is : $expandedListText")
        if (expandedListText?.Services?.isNotEmpty()!!) {
            busNumber.text = expandedListText.Services[0].ServiceNo
            if (!expandedListText.Services[0].NextBus.EstimatedArrival.isBlank()) {
                firstArriveTime.text = expandedListText.Services[0].NextBus.EstimatedArrival
            }
            val firstBusType = expandedListText.Services[0].NextBus.Type
            if (!firstBusType.isBlank()) {
                if (firstBusType == context.getString(R.string.single_deck)) {
                    firstBusIcon.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.single_deck,
                            null
                        )
                    )
                } else if (firstBusType == context.getString(R.string.double_deck)) {
                    firstBusIcon.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.double_deck,
                            null
                        )
                    )
                }
            }
        } else {
            busNumber.text = context.getString(R.string.not_available)
        }


        //change to include fields to show
        return convertView
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