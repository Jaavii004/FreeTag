package com.freetag.app.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.freetag.app.service.AppBlockerService

object PermissionUtils {

    /**
     * Checks whether FreeTag's AccessibilityService is actively enabled in System Settings.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedServiceName = "${context.packageName}/${AppBlockerService::class.java.canonicalName}"

        for (service in enabledServices) {
            val serviceId = service.id
            if (serviceId.equals(expectedServiceName, ignoreCase = true) ||
                serviceId.contains(context.packageName) && serviceId.contains(AppBlockerService::class.java.simpleName)
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Opens Android Accessibility Settings page.
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Opens Android NFC Settings page.
     */
    fun openNfcSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    /**
     * Checks if SYSTEM_ALERT_WINDOW permission is granted (recommended for instant interception on newer Androids).
     */
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Opens System Overlay settings for the app.
     */
    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}
