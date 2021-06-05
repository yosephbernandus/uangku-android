package mobile.uangku.android.core

import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager

class DismissKeyboardOnTouch(activity: Activity) : View.OnTouchListener {
    private val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN)
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        return false
    }
}