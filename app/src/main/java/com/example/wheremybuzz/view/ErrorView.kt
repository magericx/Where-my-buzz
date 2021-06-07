package com.example.wheremybuzz.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wheremybuzz.R
import com.google.android.material.button.MaterialButton

//import java.lang.ref.WeakReference

class ErrorView(/*activity: TabFragment,*/ context: Context, view: ViewGroup){

    //var mActivity: WeakReference<TabFragment> = activity as WeakReference<TabFragment>
    private val context: Context = context
    private val container : ViewGroup = view
    lateinit var errorButton: MaterialButton
    lateinit var parentView: View

    companion object{
        private val TAG = "ErrorView"
    }

    private fun getInflater(): LayoutInflater {
        return LayoutInflater.from(context)
    }

    fun build(): View {
        val inflater = getInflater()
        parentView  = inflater.inflate(R.layout.error_placeholder_layout, container, false)
        setupSubViews(parentView)
        return parentView
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
