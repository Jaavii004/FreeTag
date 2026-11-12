package com.freetag.app.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.freetag.app.FreeTagApp
import com.freetag.app.R
import com.freetag.app.ui.MainActivity

class FreeTagWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = (context.applicationContext as? FreeTagApp)?.preferenceManager
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, prefs?.isLocked == true, prefs?.streakDays ?: 1)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.freetag.app.UPDATE_WIDGET" || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, FreeTagWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            val prefs = (context.applicationContext as? FreeTagApp)?.preferenceManager
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id, prefs?.isLocked == true, prefs?.streakDays ?: 1)
            }
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            isLocked: Boolean,
            streakDays: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_freetag)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            if (isLocked) {
                views.setTextViewText(R.id.widget_title, "Modo Presencia Activo")
                views.setTextViewText(R.id.widget_subtitle, "🛡️ Bloqueo NFC • Racha ${streakDays}d")
                views.setTextColor(R.id.widget_subtitle, 0xFF34D399.toInt())
            } else {
                views.setTextViewText(R.id.widget_title, "FreeTag: Modo Libre")
                views.setTextViewText(R.id.widget_subtitle, "Toca para activar enfoque")
                views.setTextColor(R.id.widget_subtitle, 0xFF9CA3AF.toInt())
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

