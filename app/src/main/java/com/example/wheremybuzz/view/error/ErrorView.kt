package com.example.wheremybuzz.view.error

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wheremybuzz.BusApplication
import com.example.wheremybuzz.R
import com.google.android.material.button.MaterialButton

class ErrorView(view: ViewGroup){

    //var mActivity: WeakReference<FragmentActivity>? = null
    var mContext:Context = BusApplication.instance.applicationContext
    private val container : ViewGroup = view
    lateinit var errorButton: MaterialButton
    private var parentView: View? = null

    companion object{
        private val TAG = "ErrorView"
    }

    init{
        //mActivity = WeakReference(activity)
    }

    private fun getInflater(): LayoutInflater {
        return LayoutInflater.from(mContext)
    }

    fun build(): View {
        if (parentView == null){
            onViewInit()
        }
        return parentView!!
    }

    private fun onViewInit(){
        val inflater = getInflater()
        parentView  = inflater.inflate(R.layout.error_placeholder_layout, container, false)
        setupSubViews(parentView)
    }

    private fun setupSubViews(view:View?){
        errorButton = view!!.findViewById(R.id.restartApp)
    }

    fun setupErrorListeners(listener: (()->Unit)){
        errorButton.setOnClickListener {
            listener()
        }
    }

}
