package bilal.com.fintrack.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import bilal.com.fintrack.MainActivity
import bilal.com.fintrack.R

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Utiliser le nouveau NotificationService
        NotificationService.showDailyReminder(applicationContext)
        return Result.success()
    }
}
