package edu.muj.studentportal.ui

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.muj.studentportal.R
import java.text.SimpleDateFormat
import java.util.*

fun View.dp(value: Int) = (value * resources.displayMetrics.density).toInt()
fun View.systemInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
        view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, keyboard.bottom))
        WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(this)
}
fun dateKey() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
fun dateLabel(date: String): String = runCatching {
    SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date)!!)
}.getOrDefault(date)
fun timestamp(time: Long) = SimpleDateFormat("d MMM · HH:mm", Locale.getDefault()).format(Date(time))
fun weekdayLabel(day: Int) = java.text.DateFormatSymbols.getInstance().weekdays[day]
fun attendanceSummary(present: Int, total: Int): String = if (total == 0) "No sessions recorded" else
    "${java.text.NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }.format(present * 100.0 / total)}% · $present of $total sessions"
fun attendanceAtRisk(present: Int, total: Int) = total > 0 && present * 4L < total * 3L
