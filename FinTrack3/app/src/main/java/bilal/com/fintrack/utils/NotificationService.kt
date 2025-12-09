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
import bilal.com.fintrack.MainActivity
import bilal.com.fintrack.R

/**
 * Service de notification pour gérer toutes les notifications de l'application
 * Similaire à NotificationHelper mais avec des fonctionnalités étendues
 */
object NotificationService {
    const val CHANNEL_ID_BUDGET = "budget_channel"
    const val CHANNEL_ID_BALANCE = "balance_channel"
    const val CHANNEL_ID_REMINDER = "reminder_channel"
    const val CHANNEL_ID_SUMMARY = "summary_channel"
    
    /**
     * Créer tous les canaux de notification
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val budgetChannel = NotificationChannel(
                CHANNEL_ID_BUDGET,
                "Alertes Budget",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications quand le budget est dépassé"
            }
            
            val balanceChannel = NotificationChannel(
                CHANNEL_ID_BALANCE,
                "Alertes Solde",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications quand le solde devient négatif"
            }
            
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDER,
                "Rappels quotidiens",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Rappels quotidiens pour ajouter des transactions"
            }
            
            val summaryChannel = NotificationChannel(
                CHANNEL_ID_SUMMARY,
                "Résumé hebdomadaire",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Résumé financier hebdomadaire"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(balanceChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(summaryChannel)
        }
    }

    /**
     * Afficher une notification quand le budget est dépassé
     */
    fun showBudgetExceededNotification(context: Context, categoryName: String, amount: Double) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BUDGET)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚠️ Budget Dépassé")
            .setContentText("Vous avez dépassé votre budget $categoryName de ${amount.toInt()} MAD")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(1001, builder.build())
    }
    
    /**
     * Afficher une notification quand le solde devient négatif
     * Exemple: Si le solde est -44 MAD
     */
    fun showNegativeBalanceNotification(context: Context, balance: Double) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        // Ne notifier que si le solde est négatif
        if (balance >= 0) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BALANCE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 Solde Négatif!")
            .setContentText("Attention! Votre solde est de ${balance.toInt()} MAD. Vos dépenses dépassent vos revenus.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Attention! Votre solde est de ${balance.toInt()} MAD. Vos dépenses dépassent vos revenus. Veuillez vérifier vos finances."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(1002, builder.build())
    }
    
    /**
     * Afficher une notification de rappel quotidien
     */
    fun showDailyReminder(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💰 N'oubliez pas!")
            .setContentText("Avez-vous enregistré toutes vos transactions aujourd'hui?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(1003, builder.build())
    }
    
    /**
     * Afficher un résumé hebdomadaire
     */
    fun showWeeklySummary(context: Context, totalIncome: Double, totalExpense: Double, balance: Double) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_SUMMARY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📊 Résumé de la semaine")
            .setContentText("Revenus: ${totalIncome.toInt()} MAD | Dépenses: ${totalExpense.toInt()} MAD")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Revenus: ${totalIncome.toInt()} MAD\nDépenses: ${totalExpense.toInt()} MAD\nSolde: ${balance.toInt()} MAD"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(1004, builder.build())
    }
}
