package com.example.wheremybuzz.ui.main


import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction
import com.example.wheremybuzz.R


internal class ViewPagerAdapter(manager: FragmentManager?) :
    FragmentPagerAdapter(manager!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val TAG = "ViewPagerAdapter"
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

}