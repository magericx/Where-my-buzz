package com.example.wheremybuzz

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wheremybuzz.viewModel.NearestBusStopsViewModel

class ViewModelFactory(val application: Application)  : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NearestBusStopsViewModel::class.java)) {
            return NearestBusStopsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}