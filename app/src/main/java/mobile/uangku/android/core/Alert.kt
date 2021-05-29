package mobile.uangku.android.core

import android.content.Context
import androidx.appcompat.app.AlertDialog

object Alert {

    fun dialog(context: Context, error: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(error)
        builder.setPositiveButton("OK") { dialog, which -> null }
        val alert = builder.create()
        alert.show()
    }
}