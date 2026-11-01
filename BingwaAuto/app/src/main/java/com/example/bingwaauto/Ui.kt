package com.example.bingwaauto

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Ui {
    const val GREEN = 0xFF5CD68A.toInt()
    const val RED = 0xFFFF6B6B.toInt()
    const val AMBER = 0xFFFFC857.toInt()

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    fun toast(context: Context, message: String) =
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    fun money(value: Double): String = String.format(Locale.US, "KSh %,.2f", value)

    fun startOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private val timeFormat = SimpleDateFormat("d MMM HH:mm", Locale.getDefault())
    fun time(millis: Long): String = timeFormat.format(Date(millis))
}
