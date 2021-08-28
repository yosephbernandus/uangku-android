package mobile.uangku.android.core

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object Utils {
    fun addThousandSeparator(value: Double): String {
        val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
        val symbols = formatter.decimalFormatSymbols

        symbols.groupingSeparator = '.'
        formatter.decimalFormatSymbols = symbols
        return formatter.format(value)
    }

    fun getDifferenceTime(differenceTime: Long, getTime: Int): String {
        val seconds = differenceTime / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        var setDifferenceTime = seconds.toString()
        if (getTime == Constants.MINUTE) {
            setDifferenceTime = minutes.toString()
        } else if (getTime == Constants.HOURS) {
            setDifferenceTime = hours.toString()
        } else if (getTime == Constants.DAYS) {
            setDifferenceTime = days.toString()
        }
        return setDifferenceTime
    }
}