package com.example.wheremybuzz.view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.widget.Button
import com.example.wheremybuzz.model.StatusEnum
import java.lang.ref.WeakReference


class AlertDialogView(
    context: Context
) {
    private var mContextRef: WeakReference<Context>? = null

    init {
        mContextRef = WeakReference(context)
    }

    fun showDialog(cancellable: Boolean, dialogListener: DialogListener) {
        val dialog = AlertDialog.Builder(mContextRef?.get())
            .setTitle("Location Permission Needed")
            .setMessage("This app needs the Location permission, please accept to use location functionality")
            .setPositiveButton(
                "OK"
            ) { _, _ ->
                dialogListener.onClick(StatusEnum.Success)
            }
            .setCancelable(cancellable)
            .setNegativeButton("Cancel") { _, _ ->
                dialogListener.onCancel(StatusEnum.Success)

            }
            .create()
        dialog.show()
        val button: Button = dialog.getButton(BUTTON_NEGATIVE)
        button.isEnabled = cancellable

    }
}

interface DialogListener {
    fun onClick(status: StatusEnum)
    fun onCancel(status: StatusEnum)
}