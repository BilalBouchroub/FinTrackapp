package bilal.com.fintrack.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    fun formatMonthYear(month: Int, year: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.MONTH, month)
        calendar.set(java.util.Calendar.YEAR, year)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}
