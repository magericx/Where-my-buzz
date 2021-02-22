package com.example.wheremybuzz

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.example.wheremybuzz.ui.main.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        val adapter = ViewPagerAdapter(supportFragmentManager)
        viewPager!!.adapter = adapter
        tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)
    }
}