package com.example.wheremybuzz

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wheremybuzz.viewModel.BusStopsViewModel

class ViewModelFactory(val application: Application)  : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusStopsViewModel::class.java)) {
            return BusStopsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}