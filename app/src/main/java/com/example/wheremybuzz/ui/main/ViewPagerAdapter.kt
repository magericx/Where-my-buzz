package com.example.wheremybuzz.ui.main


import android.location.Location
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.wheremybuzz.model.StatusEnum
import com.example.wheremybuzz.utils.helper.permission.ILocationCallback
import com.example.wheremybuzz.utils.helper.permission.LocationListener


internal class ViewPagerAdapter(manager: FragmentManager?) :
    FragmentPagerAdapter(manager!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT), LocationListener {
    private val title = arrayOf("Nearest stops", "Favourite stops")
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> TabFragment.getInstance(position)
            1 -> FavouriteFragment.getInstance(position)
            else -> {
                TabFragment.getInstance(position)
            }
        }
    }

    override fun getCount(): Int {
        return title.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return title[position]
    }

    override fun updateOnResult(location: com.example.wheremybuzz.model.Location?) {
        (TabFragment.getInstance(0) as TabFragment).updateOnResult(location)
    }


}