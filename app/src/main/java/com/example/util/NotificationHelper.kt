package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val CHANNEL_DAILY_REMINDER = "daily_reminder"
    const val CHANNEL_MONTHLY_SUMMARY = "monthly_summary"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDER,
                "দৈনিক স্মরণ",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "আজকের হিসাব যোগ করতে দৈনিক অনুস্মারক"
            }
            manager.createNotificationChannel(dailyChannel)

            val monthlyChannel = NotificationChannel(
                CHANNEL_MONTHLY_SUMMARY,
                "মাসিক সারসংক্ষেপ",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "প্রতি মাসে আয়-ব্যয়ের সারসংক্ষেপ"
            }
            manager.createNotificationChannel(monthlyChannel)
        }
    }
}
