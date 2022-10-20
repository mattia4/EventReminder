package com.example.eventreminder.utils;

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    fun getDateString(seconds: Long, outputPattern: String): String {
        try {
            val dateFormat = SimpleDateFormat(outputPattern, Locale.ENGLISH)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = seconds * 1000
            val date = calendar.time
            return dateFormat.format(date)
        } catch (e: Exception) {
            Log.e("utils", "Date format", e)
            return ""
        }
    }
}
