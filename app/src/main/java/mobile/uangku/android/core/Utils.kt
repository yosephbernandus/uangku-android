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

}