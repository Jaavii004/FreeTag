package com.freetag.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.view.accessibility.AccessibilityEvent
import com.freetag.app.FreeTagApp
import com.freetag.app.data.PreferenceManager
import com.freetag.app.ui.LockOverlayActivity

/**
 * High-performance Accessibility Service that intercepts blocked applications in real-time.
 * Listens strictly to TYPE_WINDOW_STATE_CHANGED events without inspecting window content (privacy-first).
 */
class AppBlockerService : AccessibilityService() {

    private lateinit var preferenceManager: PreferenceManager
    private var lastInterceptedPackage: String? = null
    private var lastInterceptedTime: Long = 0L

    // Cache of packages that should NEVER be blocked (system UI, launchers, settings, phone, keyboards)
    private val whitelistPackages = mutableSetOf(
        "com.android.systemui",
        "android",
        // System Settings & Accessibility config (never trap user outside settings)
        "com.android.settings",
        "com.google.android.settings.intelligence",
        // Keyboards
        "com.google.android.inputmethod.latin",
        "com.touchtype.swiftkey",
        "com.samsung.android.honeyboard",
        "com.sec.android.inputmethod",
        // Emergency & Telephony
        "com.android.emergency",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.phone",
        "com.android.server.telecom",
        "com.android.incallui",
        "com.android.contacts",
        // Known manufacturer & custom launchers
        "com.sec.android.app.launcher",
        "com.miui.home",
        "com.mi.android.globallauncher",
        "com.google.android.apps.nexuslauncher",
        "com.huawei.android.launcher",
        "com.hihonor.android.launcher",
        "com.oppo.launcher",
        "com.coloros.launcher",
        "net.oneplus.launcher",
        "com.motorola.launcher3",
        "com.transsion.hilauncher",
        "com.teslacoilsw.launcher",
        "com.microsoft.launcher",
        "app.lawnchair",
        "com.actionlauncher.playstore"
    )

    // Bypass & Uninstaller package identifiers
    private val installerPackages = setOf(
        "com.google.android.packageinstaller",
        "com.android.packageinstaller"
    )

    override fun onCreate() {
        super.onCreate()
        preferenceManager = (application as FreeTagApp).preferenceManager
        whitelistPackages.add(packageName) // Always whitelist FreeTag itself
        refreshLauncherPackages()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val foregroundPackage = event.packageName?.toString() ?: return

        // Fast-path checks
        if (foregroundPackage.isEmpty()) return
        if (foregroundPackage == packageName) return
        if (whitelistPackages.contains(foregroundPackage)) return

        // Check if lock mode is active
        if (!preferenceManager.isLocked) return

        // Anti-Bypass protection: intercept attempts to open package uninstaller during active lock
        if (preferenceManager.isAntiBypassEnabled && installerPackages.contains(foregroundPackage)) {
            interceptBlockedApp(foregroundPackage)
            return
        }

        // Check if this package is in the user's blocked list
        if (preferenceManager.isPackageBlocked(foregroundPackage)) {
            val now = System.currentTimeMillis()
            // Avoid spamming if the event fires multiple times in a short burst
            if (foregroundPackage == lastInterceptedPackage && (now - lastInterceptedTime) < 1200L) {
                return
            }
            lastInterceptedPackage = foregroundPackage
            lastInterceptedTime = now

            // Record interception count for daily stats
            preferenceManager.recordInterception()

            interceptBlockedApp(foregroundPackage)
        }
    }

    private fun interceptBlockedApp(blockedPackage: String) {
        // Direct display of full-screen Lock Interception UI
        LockOverlayActivity.start(this, blockedPackage)
    }

    /**
     * Resolves all current and installed launchers to guarantee the home screen is never locked.
     */
    private fun refreshLauncherPackages() {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            // 1. Check default launcher
            val defaultResolve = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            defaultResolve?.activityInfo?.packageName?.let {
                whitelistPackages.add(it)
            }

            // 2. Query all activities supporting CATEGORY_HOME
            val allLaunchers: List<ResolveInfo> = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_ALL)
            } else {
                packageManager.queryIntentActivities(homeIntent, 0)
            }
            for (info in allLaunchers) {
                info.activityInfo?.packageName?.let {
                    whitelistPackages.add(it)
                }
            }
        } catch (_: Exception) {
            // Keep existing defaults
        }
    }

    override fun onInterrupt() {
        // Service interrupted by system
    }
}
