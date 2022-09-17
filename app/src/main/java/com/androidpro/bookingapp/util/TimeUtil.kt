package com.androidpro.bookingapp.util

import java.util.concurrent.TimeUnit

object TimerUtil {
    fun getFormattedTime(timeInMillis: Long): String {
        var milliseconds = timeInMillis
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return "${if (hours < 10) "0" else ""}$hours:" +
            "${if (minutes < 10) "0" else ""}$minutes:" +
            "${if (seconds < 10) "0" else ""}$seconds"
    }

    fun getTotalMinutes(startTime: Long, endTime:Long): Int {
        return TimeUnit.MILLISECONDS.toMinutes(endTime-startTime).toInt()
    }

    fun getCurrentTimeInMillis() = System.currentTimeMillis()
}