package mobile.uangku.android.core

import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView

object Validator {

    fun validateMinLength(view: View, length: Int): Boolean {
        return if (view is EditText)
            view.text.trim().length >= length
        else
            (view as TextView).text.trim().length >= length
    }

    fun validateEmail(editText: EditText): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(editText.text).matches()
    }
}