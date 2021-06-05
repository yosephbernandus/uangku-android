package mobile.uangku.android.core

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {

    companion object {

        @JvmStatic
        fun fromUnixTime(unixTime: Long): Date {
            return Date(unixTime * 1000L)
        }

        @JvmStatic
        fun fromDateString(dateTimeString: String): Date? {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            try {
                return simpleDateFormat.parse(dateTimeString)
            } catch (e: ParseException) {
                return null
            }
        }

        @JvmStatic
        fun toISOString(date: Date): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
        }

        @JvmStatic
        fun toDisplayString(date: Date): String {
            return SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
        }

        @JvmStatic
        fun toDisplayStringMonth(date: Date): String {
            return SimpleDateFormat("MMM yyyy", Locale.US).format(date)
        }

        @JvmStatic
        fun toDisplayStringTime(date: Date): String {
            return SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.US).format(date)
        }

        @JvmStatic
        fun convertStringToDate(date: String): Date {
            return SimpleDateFormat("yyyy-MM-dd").parse(date)
        }
    }
}
