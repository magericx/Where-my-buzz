package com.example.wheremybuzz.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.wheremybuzz.R
import com.example.wheremybuzz.model.StatusEnum


class Alert(
    context: Context?
) : Dialog(context!!) {
    var message: String? = null
    var title: String? = null
    private var btYesText: String? = null
    private var btNoText: String? = null
    var icon = 0
    private var btYesListener: View.OnClickListener? = null
    private var btNoListener: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog)
        val tv = findViewById(R.id.malertTitle) as TextView
        tv.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
        tv.text = title
        val tvmessage = findViewById(R.id.aleartMessage) as TextView
        tvmessage.text = message
        val btYes: Button = findViewById(R.id.aleartYes) as Button
        val btNo: Button = findViewById(R.id.aleartNo) as Button
        btYes.setText(btYesText)
        btNo.setText(btNoText)
        btYes.setOnClickListener(btYesListener)
        btNo.setOnClickListener(btNoListener)
    }


    fun setPositveButton(yes: String?, onClickListener: View.OnClickListener?) {
        dismiss()
        btYesText = yes
        btYesListener = onClickListener
    }

    fun setNegativeButton(no: String?, onClickListener: View.OnClickListener?) {
        dismiss()
        btNoText = no
        btNoListener = onClickListener
    }
}

interface DialogCallback {
    fun updateOnResult(status: StatusEnum)
}