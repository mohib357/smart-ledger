package com.example.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.first

class ExpiryCheckWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("expiry_notif_enabled", true)) return Result.success()

        val db = AppDatabase.getDatabase(applicationContext)
        val sevenDaysFromNow = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000
        val expiringList = db.productDao().getExpiringProducts(sevenDaysFromNow).first()

        if (expiringList.isEmpty()) return Result.success()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_tab", 8)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = String.format(
            applicationContext.getString(R.string.notification_expiry_text),
            expiringList.size
        )
        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_EXPIRY_WARNING)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.notification_expiry_title))
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(2001, notification)

        return Result.success()
    }
}
