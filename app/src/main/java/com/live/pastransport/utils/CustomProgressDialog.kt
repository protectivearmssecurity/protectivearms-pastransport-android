package com.live.pastransport.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import com.live.pastransport.R


class CustomProgressDialog(var dialog: Dialog) {

    fun show(context: Context): Dialog? {
        val inflater = (context as Activity).layoutInflater
        val view = inflater.inflate(R.layout.loader_layout, null)

        // Create a dialog if it's null
        if (dialog == null) {
            dialog = Dialog(context)
        }

        dialog?.setContentView(view)
        dialog?.setCancelable(false)

        // Set transparent background for the dialog window
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        if (!dialog?.isShowing!!) {
            dialog?.show()
        }
            return dialog

        }

//    fun show(): KProgressHUD {
//        dialog.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
//        dialog.setCancellable(false)
//        dialog.setAnimationSpeed(2)
//        dialog.setDimAmount(0.1f)
//        dialog.show()
//        return dialog
//    }

    fun hide() {
        if (dialog.isShowing){
        dialog.dismiss()
    }}
}