package com.example.wheremybuzz.ui.main

import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.example.wheremybuzz.R
import com.example.wheremybuzz.model.StatusEnum
import com.example.wheremybuzz.utils.helper.permission.ILocationCallback
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ILocationCallback {
    private var toolbar: Toolbar? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var viewPageAdapter: ViewPagerAdapter? = null
    private var newMapFragment: MapsFragment? = null

    companion object {
        const val TAG = "MainActivity"
    }

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            val rootView = window.decorView.rootView
            val view = rootView.findViewById<FrameLayout>(R.id.fragment_map_container) ?: return
            when (position) {
                //hide mapView according to the fragment type
                0 -> {
                    if (view.visibility == View.GONE) view.visibility = View.VISIBLE
                }
                1 -> {
                    if (view.visibility == View.VISIBLE) view.visibility = View.GONE
                }
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        viewPageAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPager?.apply{
            addOnPageChangeListener(onPageChangeListener)
            adapter = viewPageAdapter
        }
        setupToolBar()
        tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout?.apply{
            setupWithViewPager(viewPager)
        }
        newMapFragment = MapsFragment()
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_map_container, newMapFragment!!)
            commit()
        }
    }

    private fun setupToolBar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        toolbar!!.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayout = null
        viewPager = null
        toolbar = null
        viewPageAdapter = null
        newMapFragment = null
    }

    override fun updateOnResult(location: Location?, statusEnum: StatusEnum) {
        val mapFragment: MapsFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_map_container) as MapsFragment
        mapFragment.updateOnResult(location = location, statusEnum = statusEnum)
    }


}