package bilal.com.fintrack.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import bilal.com.fintrack.MainActivity
import bilal.com.fintrack.R
import java.util.concurrent.TimeUnit

object NotificationHelper {
    const val CHANNEL_ID_BUDGET = "budget_channel"
    const val CHANNEL_ID_REMINDER = "reminder_channel"
    const val CHANNEL_ID_SUMMARY = "summary_channel"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val budgetChannel = NotificationChannel(
                CHANNEL_ID_BUDGET,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when budget is exceeded"
            }
            
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDER,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to add transactions"
            }
            
            val summaryChannel = NotificationChannel(
                CHANNEL_ID_SUMMARY,
                "Weekly Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly financial summary"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(summaryChannel)
        }
    }

    fun showBudgetExceededNotification(context: Context, categoryName: String, amount: Double) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BUDGET)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with actual icon
            .setContentTitle("⚠️ Budget Dépassé")
            .setContentText("Vous avez dépassé votre budget $categoryName de $amount MAD")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(1001, builder.build())
    }
    
    fun scheduleDailyReminder(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    private fun calculateInitialDelay(): Long {
        val calendar = java.util.Calendar.getInstance()
        val now = System.currentTimeMillis()
        
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 20) // 8 PM
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        
        if (calendar.timeInMillis < now) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis - now
    }
}
