package com.freetag.app.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freetag.app.FreeTagApp
import com.freetag.app.data.AppCategory
import com.freetag.app.data.AppInfo
import com.freetag.app.data.FocusProfile
import com.freetag.app.data.NfcHelper
import com.freetag.app.data.PreferenceManager
import com.freetag.app.data.ScannedCardInfo
import com.freetag.app.ui.screens.BlockedAppsScreen
import com.freetag.app.ui.screens.DashboardScreen
import com.freetag.app.ui.screens.KeyManagementScreen
import com.freetag.app.ui.theme.EmeraldDark
import com.freetag.app.ui.theme.EmeraldLight
import com.freetag.app.ui.theme.FreeTagTheme
import com.freetag.app.ui.theme.ObsidianBg
import com.freetag.app.ui.theme.SurfaceBorder
import com.freetag.app.ui.theme.SurfaceDark
import com.freetag.app.ui.theme.TextSecondary
import com.freetag.app.util.FocusNotificationManager
import com.freetag.app.util.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class NavigationTab(val index: Int, val title: String, val icon: ImageVector) {
    object Dashboard : NavigationTab(0, "Presencia", Icons.Rounded.Spa)
    object BlockedApps : NavigationTab(1, "Apps", Icons.Rounded.Apps)
    object Keys : NavigationTab(2, "Llaves", Icons.Rounded.CreditCard)
}

class MainActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private var lastScannedCardInfo by mutableStateOf<ScannedCardInfo?>(null)
    private var currentTabIndex by mutableIntStateOf(0)
    private var cachedApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = (application as FreeTagApp).preferenceManager

        // Setup ongoing notification channel
        FocusNotificationManager.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS on Android 13+ if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            FreeTagTheme {
                MainContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startNfcScanning()
        // Refresh persistent notification state
        if (preferenceManager.isLocked && !preferenceManager.isTemporaryPassActive) {
            FocusNotificationManager.showFocusNotification(this)
        } else {
            FocusNotificationManager.cancelFocusNotification(this)
        }
    }

    override fun onPause() {
        super.onPause()
        NfcHelper.stopReaderMode(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
        if (tag != null) {
            val scanned = NfcHelper.processTagToHash(tag)
            if (scanned != null) {
                handleScannedCard(scanned)
            }
        }
    }

    private fun startNfcScanning() {
        NfcHelper.startReaderMode(
            activity = this,
            onTagScanned = { scanned ->
                handleScannedCard(scanned)
            },
            onError = { _ -> }
        )
    }

    private fun handleScannedCard(scanned: ScannedCardInfo) {
        if (currentTabIndex == NavigationTab.Keys.index || !preferenceManager.hasBoundCard) {
            // Key management mode: record for binding
            lastScannedCardInfo = scanned
        } else {
            // Dashboard / Blocker mode: Toggle lock if matches bound card
            if (preferenceManager.matchesBoundCard(scanned.sha256Hash)) {
                val newLockState = !preferenceManager.isLocked
                val matchedKey = preferenceManager.getKeyByHash(scanned.sha256Hash)

                if (newLockState) {
                    val assignedProfileId = matchedKey?.assignedProfile ?: preferenceManager.activeProfileId
                    preferenceManager.activeProfileId = assignedProfileId
                    applyProfileBlocking(assignedProfileId)
                }

                preferenceManager.isLocked = newLockState

                if (newLockState) {
                    NfcHelper.playLockFeedback(this)
                    val profileName = FocusProfile.values().find { it.id == preferenceManager.activeProfileId }?.title ?: "Presencia"
                    FocusNotificationManager.showFocusNotification(this)
                    Toast.makeText(this, "¡Modo Presencia Activado [$profileName]!", Toast.LENGTH_SHORT).show()
                } else {
                    NfcHelper.playUnlockFeedback(this)
                    FocusNotificationManager.cancelFocusNotification(this)
                    Toast.makeText(this, "¡Dispositivo Desbloqueado!", Toast.LENGTH_SHORT).show()
                }
            } else {
                NfcHelper.vibrateError(this)
                Toast.makeText(this, "Tarjeta no autorizada para esta acción.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyProfileBlocking(profileId: String) {
        if (cachedApps.isEmpty()) return
        when (profileId) {
            FocusProfile.WORK.id -> {
                val targets = cachedApps.filter { it.category == AppCategory.SOCIAL || it.category == AppCategory.ENTERTAINMENT || it.category == AppCategory.GAMES }.map { it.packageName }
                preferenceManager.addAllPackages(targets)
            }
            FocusProfile.DETOX.id -> {
                val targets = cachedApps.filter { it.category == AppCategory.SOCIAL || it.category == AppCategory.ENTERTAINMENT }.map { it.packageName }
                preferenceManager.addAllPackages(targets)
            }
            FocusProfile.SLEEP.id -> {
                val targets = cachedApps.map { it.packageName }
                preferenceManager.addAllPackages(targets)
            }
        }
    }

    @Composable
    private fun MainContent() {
        val isLocked by preferenceManager.isLockedFlow.collectAsState()
        val registeredKeys by preferenceManager.registeredKeysFlow.collectAsState()
        val boundLabel by preferenceManager.boundCardLabelFlow.collectAsState()
        val boundMasked by preferenceManager.boundCardMaskedFlow.collectAsState()
        val blockedPackages by preferenceManager.blockedPackagesFlow.collectAsState()
        val emergencyUnlocks by preferenceManager.emergencyUnlocksLeftFlow.collectAsState()
        val focusStartTime by preferenceManager.focusStartTimeFlow.collectAsState()
        val targetDurationMinutes by preferenceManager.targetDurationMinutesFlow.collectAsState()
        val totalFocusMinutes by preferenceManager.totalFocusMinutesFlow.collectAsState()
        val interceptionsToday by preferenceManager.interceptionsTodayFlow.collectAsState()
        val streakDays by preferenceManager.streakDaysFlow.collectAsState()
        val isStrictMode by preferenceManager.isStrictModeFlow.collectAsState()
        val isAntiBypassEnabled by preferenceManager.isAntiBypassEnabledFlow.collectAsState()
        val isBunkerMode by preferenceManager.isBunkerModeFlow.collectAsState()
        val mindfulDelay by preferenceManager.mindfulDelaySecondsFlow.collectAsState()
        val hasRecoveryPin by preferenceManager.hasRecoveryPinFlow.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        var isAccessibilityEnabled by remember {
            mutableStateOf(PermissionUtils.isAccessibilityServiceEnabled(this@MainActivity))
        }
        var isNfcEnabled by remember {
            mutableStateOf(NfcHelper.isNfcEnabled(this@MainActivity))
        }

        // Periodic permission status check
        LaunchedEffect(Unit) {
            isAccessibilityEnabled = PermissionUtils.isAccessibilityServiceEnabled(this@MainActivity)
            isNfcEnabled = NfcHelper.isNfcEnabled(this@MainActivity)
        }

        // Installed apps state
        var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
        var isLoadingApps by remember { mutableStateOf(true) }

        LaunchedEffect(blockedPackages) {
            withContext(Dispatchers.IO) {
                val apps = loadInstalledApps(blockedPackages)
                cachedApps = apps
                withContext(Dispatchers.Main) {
                    installedApps = apps
                    isLoadingApps = false
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = SurfaceDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = SurfaceBorder
                    )
                ) {
                    val tabs = listOf(
                        NavigationTab.Dashboard,
                        NavigationTab.BlockedApps,
                        NavigationTab.Keys
                    )
                    tabs.forEach { tab ->
                        val isSelected = currentTabIndex == tab.index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentTabIndex = tab.index
                                lastScannedCardInfo = null // clear temp scan on tab switch
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldLight,
                                selectedTextColor = EmeraldLight,
                                indicatorColor = EmeraldDark,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg)
                    .padding(paddingValues)
            ) {
                when (currentTabIndex) {
                    NavigationTab.Dashboard.index -> {
                        DashboardScreen(
                            isLocked = isLocked,
                            focusStartTime = focusStartTime,
                            targetDurationMinutes = targetDurationMinutes,
                            totalFocusMinutes = totalFocusMinutes,
                            hasBoundCard = registeredKeys.isNotEmpty(),
                            boundCardLabel = boundLabel,
                            boundCardMasked = boundMasked,
                            blockedAppsCount = blockedPackages.size,
                            interceptionsToday = interceptionsToday,
                            streakDays = streakDays,
                            isStrictMode = isStrictMode,
                            isAntiBypassEnabled = isAntiBypassEnabled,
                            isBunkerMode = isBunkerMode,
                            mindfulDelaySeconds = mindfulDelay,
                            weeklyStats = preferenceManager.getWeeklyStats(),
                            isAccessibilityEnabled = isAccessibilityEnabled,
                            isNfcEnabled = isNfcEnabled,
                            remainingEmergencyUnlocks = emergencyUnlocks,
                            onToggleStrictMode = { preferenceManager.isStrictMode = it },
                            onToggleAntiBypass = { preferenceManager.isAntiBypassEnabled = it },
                            onToggleBunkerMode = { preferenceManager.isBunkerMode = it },
                            onSelectMindfulDelay = { preferenceManager.mindfulDelaySeconds = it },
                            onSelectDurationMinutes = { preferenceManager.targetDurationMinutes = it },
                            onOpenAccessibilitySettings = {
                                PermissionUtils.openAccessibilitySettings(this@MainActivity)
                            },
                            onOpenNfcSettings = {
                                PermissionUtils.openNfcSettings(this@MainActivity)
                            },
                            onNavigateToKeys = {
                                currentTabIndex = NavigationTab.Keys.index
                            },
                            onNavigateToApps = {
                                currentTabIndex = NavigationTab.BlockedApps.index
                            },
                            onEmergencyUnlocked = {
                                preferenceManager.useEmergencyUnlock()
                                FocusNotificationManager.cancelFocusNotification(this@MainActivity)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Desbloqueo de emergencia realizado.")
                                }
                            },
                            onAutoUnlockTimeout = {
                                preferenceManager.isLocked = false
                                NfcHelper.vibrateSuccess(this@MainActivity)
                                FocusNotificationManager.cancelFocusNotification(this@MainActivity)
                                scope.launch {
                                    snackbarHostState.showSnackbar("¡Sesión de enfoque completada con éxito! 🎉")
                                }
                            },
                            onArmLockWithCardPrompt = {
                                if (preferenceManager.hasBoundCard) {
                                    applyProfileBlocking(preferenceManager.activeProfileId)
                                    preferenceManager.isLocked = true
                                    NfcHelper.vibrateSuccess(this@MainActivity)
                                    FocusNotificationManager.showFocusNotification(this@MainActivity)
                                    scope.launch {
                                        val dur = preferenceManager.targetDurationMinutes
                                        val msg = if (dur > 0) "¡Sesión de ${dur}m iniciada!" else "¡Modo Presencia Activado!"
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                } else {
                                    currentTabIndex = NavigationTab.Keys.index
                                }
                            }
                        )
                    }

                    NavigationTab.BlockedApps.index -> {
                        BlockedAppsScreen(
                            isLocked = isLocked,
                            isBunkerMode = isBunkerMode,
                            installedApps = installedApps,
                            isLoading = isLoadingApps,
                            onToggleApp = { pkg ->
                                if (isLocked) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Desbloquea con tu tarjeta para editar la lista.")
                                    }
                                } else {
                                    preferenceManager.togglePackageBlocked(pkg)
                                }
                            },
                            onBlockSocialMediaOnly = {
                                if (isLocked) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Desbloquea con tu tarjeta para editar la lista.")
                                    }
                                } else {
                                    val socialPackages = installedApps
                                        .filter { it.category == AppCategory.SOCIAL }
                                        .map { it.packageName }
                                    preferenceManager.addAllPackages(socialPackages)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("¡${socialPackages.size} redes sociales añadidas al bloqueo!")
                                    }
                                }
                            },
                            onClearAllBlocked = {
                                if (isLocked) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Desbloquea con tu tarjeta para editar la lista.")
                                    }
                                } else {
                                    preferenceManager.clearAllBlockedPackages()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Lista de bloqueo limpiada.")
                                    }
                                }
                            }
                        )
                    }

                    NavigationTab.Keys.index -> {
                        KeyManagementScreen(
                            isLocked = isLocked,
                            registeredKeys = registeredKeys,
                            lastScannedTempCard = lastScannedCardInfo?.maskedDisplay,
                            hasRecoveryPin = hasRecoveryPin,
                            onBindCardConfirmed = { label, profileId ->
                                val card = lastScannedCardInfo
                                if (card != null) {
                                    preferenceManager.bindCard(
                                        hash = card.sha256Hash,
                                        maskedUid = card.maskedDisplay,
                                        label = label,
                                        assignedProfile = profileId
                                    )
                                    lastScannedCardInfo = null
                                    scope.launch {
                                        snackbarHostState.showSnackbar("¡Tarjeta vinculada con éxito!")
                                    }
                                }
                            },
                            onUpdateKeyProfile = { hash, profileId ->
                                preferenceManager.updateKeyProfile(hash, profileId)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Perfil de tarjeta actualizado.")
                                }
                            },
                            onSaveRecoveryPin = { pin ->
                                preferenceManager.setRecoveryPin(pin)
                                scope.launch {
                                    snackbarHostState.showSnackbar("¡PIN de papel configurado correctamente!")
                                }
                            },
                            onExportBackup = {
                                preferenceManager.exportBackupJson()
                            },
                            onImportBackup = { json ->
                                val success = preferenceManager.importBackupJson(json)
                                if (success) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("¡Copia de seguridad restaurada correctamente!")
                                    }
                                }
                                success
                            },
                            onRemoveKey = { hash ->
                                if (preferenceManager.removeKey(hash)) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Llave eliminada del llavero.")
                                    }
                                }
                            },
                            onClearAllKeys = {
                                if (preferenceManager.unbindCard()) {
                                    lastScannedCardInfo = null
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Todas las llaves han sido desvinculadas.")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Loads installed applications that have launcher activities and categorizes them.
     */
    private fun loadInstalledApps(blockedPackages: Set<String>): List<AppInfo> {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val appList = mutableListOf<AppInfo>()
        val seenPackages = mutableSetOf<String>()

        for (resolveInfo in resolveInfos) {
            try {
                val actInfo = resolveInfo.activityInfo ?: continue
                val pkgName = actInfo.packageName ?: continue
                if (pkgName == packageName) continue // skip FreeTag itself
                if (seenPackages.contains(pkgName)) continue
                seenPackages.add(pkgName)

                val label = resolveInfo.loadLabel(pm)?.toString() ?: pkgName
                val icon = resolveInfo.loadIcon(pm)
                val appInfo = actInfo.applicationInfo
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val category = detectAppCategory(pkgName, label, appInfo)

                appList.add(
                    AppInfo(
                        packageName = pkgName,
                        appName = label,
                        icon = icon,
                        isSystemApp = isSystem,
                        isBlocked = blockedPackages.contains(pkgName),
                        category = category
                    )
                )
            } catch (_: Exception) {
                // Ignore any individual package parsing issues
            }
        }

        // Sort alphabetically by application name
        return appList.sortedBy { it.appName.lowercase() }
    }

    private fun detectAppCategory(pkg: String, label: String, appInfo: ApplicationInfo): AppCategory {
        val lowerPkg = pkg.lowercase()

        // 1. Social networks & chat traps
        if (lowerPkg.contains("instagram") || lowerPkg.contains("tiktok") ||
            lowerPkg.contains("facebook") || lowerPkg.contains("twitter") || lowerPkg.contains(".katana") ||
            lowerPkg.contains("snapchat") || lowerPkg.contains("reddit") || lowerPkg.contains("threads") ||
            lowerPkg.contains("bereal") || lowerPkg.contains("mastodon") || lowerPkg.contains("linkedin") ||
            lowerPkg.contains("pinterest") || lowerPkg.contains("tinder") || lowerPkg.contains("bumble") ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo.category == ApplicationInfo.CATEGORY_SOCIAL)
        ) {
            return AppCategory.SOCIAL
        }

        // 2. Video streaming & binge
        if (lowerPkg.contains("youtube") || lowerPkg.contains("netflix") || lowerPkg.contains("twitch") ||
            lowerPkg.contains("primevideo") || lowerPkg.contains("disney") || lowerPkg.contains("hbo") ||
            lowerPkg.contains("spotify") || lowerPkg.contains("crunchyroll") || lowerPkg.contains("plex") ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    (appInfo.category == ApplicationInfo.CATEGORY_VIDEO || appInfo.category == ApplicationInfo.CATEGORY_AUDIO))
        ) {
            return AppCategory.ENTERTAINMENT
        }

        // 3. Mobile games
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo.category == ApplicationInfo.CATEGORY_GAME) ||
            lowerPkg.contains(".game") || lowerPkg.contains("supercell") || lowerPkg.contains("roblox") ||
            lowerPkg.contains("minecraft") || lowerPkg.contains("king.candy") || lowerPkg.contains("genshin")
        ) {
            return AppCategory.GAMES
        }

        // 4. Productivity
        if (lowerPkg.contains("slack") || lowerPkg.contains("notion") || lowerPkg.contains("trello") ||
            lowerPkg.contains("asana") || lowerPkg.contains("gmail") || lowerPkg.contains("outlook") ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo.category == ApplicationInfo.CATEGORY_PRODUCTIVITY)
        ) {
            return AppCategory.PRODUCTIVITY
        }

        return AppCategory.OTHER
    }
}
