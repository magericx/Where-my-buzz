package com.example.wheremybuzz.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment

class SecondFragment : Fragment() {

    companion object {
        val TAG = "SecondFragment"
        fun getInstance(position: Int): Fragment {
            Log.d(TAG,"Loaded second fragment here")
            val bundle = Bundle()
            bundle.putInt("pos", position)
            val tabFragment = SecondFragment()
            tabFragment.arguments = bundle
            return tabFragment
        }
    }
}