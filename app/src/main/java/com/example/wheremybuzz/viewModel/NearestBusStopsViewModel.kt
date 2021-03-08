package com.example.wheremybuzz.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.example.wheremybuzz.model.NearestBusStopsResponse
import com.example.wheremybuzz.repository.NearestBusRepository


class NearestBusStopsViewModel(application: Application) : AndroidViewModel(application) {
    private var projectListObservable: LiveData<List<NearestBusStopsResponse>>? = null
    private val TAG ="NearestBusStopsView"

    var mRepository: NearestBusRepository? = null

    init {
        // If any transformation is needed, this can be simply done by Transformations class ...
//            projectListObservable = NearestBusRepository
        mRepository = NearestBusRepository()
    }

    fun setProjectListObservable(){
        projectListObservable = mRepository!!.getNearestBusStops()
        //Log.d(TAG,"print projectListObservable" + mRepository!!.getNearestBusStops()?.value?.get(0)?.results)
    }
    /**
     * Expose the LiveData Projects query so the UI can observe it.
     */
    fun getProjectListObservable(): LiveData<List<NearestBusStopsResponse>>? {
        Log.d(TAG,"print projectListObservable")
        return projectListObservable
    }
}