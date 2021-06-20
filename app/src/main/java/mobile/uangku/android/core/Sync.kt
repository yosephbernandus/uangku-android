package mobile.uangku.android.core

import android.content.Context
import java.util.*

object Sync {

    // interval in minutes
    fun isNeeded(context: Context, key: String, interval: Int): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = Preferences(context).getLong(key)
        calendar.add(Calendar.MINUTE, interval)

        return if (Date().after(calendar.time)) true else false
    }

    fun setSyncTime(context: Context, key: String) {
        Preferences(context)[key] = Date().time
    }
}