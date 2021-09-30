package com.example.wheremybuzz

import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.example.wheremybuzz.model.StatusEnum
import com.example.wheremybuzz.ui.main.ViewPagerAdapter
import com.example.wheremybuzz.utils.helper.permission.ILocationCallback
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ILocationCallback {
    private var toolbar: Toolbar? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var viewPageAdapter: ViewPagerAdapter? = null

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        setupToolBar()
        viewPageAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPager!!.adapter = viewPageAdapter
        tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)

        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        val newMapFragment = MapsFragment()
        fragmentTransaction.add(R.id.fragment_map_container, newMapFragment)
        fragmentTransaction.commit()
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
    }

    override fun updateOnResult(location: Location?, statusEnum: StatusEnum) {
        val mapFragment: MapsFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_map_container) as MapsFragment
        mapFragment.updateOnResult(location = location, statusEnum = statusEnum)
    }



}