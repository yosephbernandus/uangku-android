package mobile.uangku.android.core

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Window
import mobile.uangku.android.R

class LoadingDialog(context: Context) : Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loading_view)
        window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        setCancelable(false)
    }

    fun dismissIfNeeded() {
        if (isShowing) dismiss()
    }
}
