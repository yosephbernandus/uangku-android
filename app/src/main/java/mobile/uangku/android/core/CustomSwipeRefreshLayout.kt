package mobile.uangku.android.core

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.util.AttributeSet
import mobile.uangku.android.R

class CustomSwipeRefreshLayout : SwipeRefreshLayout {

    val primaryColorID = ContextCompat.getColor(context, R.color.primary)

    constructor(context: Context) : super(context) {
        setColorSchemeColors(primaryColorID, primaryColorID, primaryColorID)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setColorSchemeColors(primaryColorID, primaryColorID, primaryColorID)
    }
}
