package com.freetag.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.freetag.app.R
import com.freetag.app.ui.MainActivity

object FocusNotificationManager {

    private const val CHANNEL_ID = "freetag_presence_channel"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Estado de Presencia FreeTag",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra el estado activo del bloqueo físico NFC y tiempo transcurrido."
                setShowBadge(false)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showFocusNotification(context: Context, remainingTimeFormatted: String? = null) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = if (remainingTimeFormatted != null) {
            "Tiempo restante de sesión: $remainingTimeFormatted • Distracciones bloqueadas"
        } else {
            "Modo Presencia Activo • Acerca tu tarjeta física NFC para desbloquear"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_freetag_logo)
            .setContentTitle("🛡️ FreeTag: Modo Presencia Activo")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build()

        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS may not be granted on Android 13+
        }
    }

    fun cancelFocusNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(NOTIFICATION_ID)
    }
}

