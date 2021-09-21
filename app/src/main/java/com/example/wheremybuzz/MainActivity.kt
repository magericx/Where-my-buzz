package com.example.wheremybuzz

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.example.wheremybuzz.ui.main.ViewPagerAdapter
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        setupToolBar()
        val adapter = ViewPagerAdapter(supportFragmentManager)
        viewPager!!.adapter = adapter
        tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)
    }

    private fun setupToolBar(){
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
    }

}