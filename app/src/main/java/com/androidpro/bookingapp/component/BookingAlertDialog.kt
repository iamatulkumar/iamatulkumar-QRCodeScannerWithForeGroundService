package com.androidpro.bookingapp.component

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

object BookingAlertDialog {
    fun showDialogOK(title: String? = null,
                             message: String? = null,
                             context: Context?,
                             okListener: DialogInterface.OnClickListener) {
        context?.let {
            val alertDialog = AlertDialog.Builder(context)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
            title?.let { alertDialog.setTitle(it) }
            message?.let { alertDialog.setMessage(it) }
            alertDialog
                .create()
                .show()
        }
    }
}