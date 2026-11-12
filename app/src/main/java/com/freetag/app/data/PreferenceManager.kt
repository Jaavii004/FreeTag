package com.freetag.app.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class FocusProfile(val id: String, val title: String, val subtitle: String) {
    DETOX("DETOX", "Social Detox", "Bloquea redes sociales, vídeos y ocio"),
    WORK("WORK", "Trabajo Profundo", "Permite productividad, bloquea distracciones"),
    SLEEP("SLEEP", "Modo Sueño", "Bloqueo extremo nocturno (solo llamadas y reloj)")
}

/**
 * Representation of a registered physical NFC key with profile mapping.
 */
data class RegisteredKey(
    val hash: String,
    val maskedUid: String,
    val label: String,
    val registeredAt: Long = System.currentTimeMillis(),
    val assignedProfile: String = FocusProfile.DETOX.id
) {
    fun serialize(): String = "$hash|$maskedUid|$label|$registeredAt|$assignedProfile"

    companion object {
        fun deserialize(raw: String): RegisteredKey? {
            val parts = raw.split("|")
            if (parts.size < 3) return null
            val hash = parts[0]
            val maskedUid = parts[1]
            val label = parts[2]
            val registeredAt = parts.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis()
            val assignedProfile = parts.getOrNull(4) ?: FocusProfile.DETOX.id
            return RegisteredKey(hash, maskedUid, label, registeredAt, assignedProfile)
        }
    }
}

data class DayFocusStat(
    val dayLabel: String,
    val minutes: Int,
    val isToday: Boolean = false
)

/**
 * Thread-safe preferences and local state management for FreeTag.
 * Provides synchronous read methods for AppBlockerService (Accessibility)
 * alongside reactive StateFlows for Jetpack Compose UI.
 */
class PreferenceManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Reactive StateFlows for Compose UI (initialized safely and directly from prefs)
    private val _isLockedFlow = MutableStateFlow(prefs.getBoolean(KEY_IS_LOCKED, false))
    val isLockedFlow: StateFlow<Boolean> = _isLockedFlow.asStateFlow()

    private val _registeredKeysFlow = MutableStateFlow<List<RegisteredKey>>(emptyList())
    val registeredKeysFlow: StateFlow<List<RegisteredKey>> = _registeredKeysFlow.asStateFlow()

    private val _activeProfileIdFlow = MutableStateFlow(prefs.getString(KEY_ACTIVE_PROFILE_ID, FocusProfile.DETOX.id) ?: FocusProfile.DETOX.id)
    val activeProfileIdFlow: StateFlow<String> = _activeProfileIdFlow.asStateFlow()

    private val _boundCardHashFlow = MutableStateFlow<String?>(null)
    val boundCardHashFlow: StateFlow<String?> = _boundCardHashFlow.asStateFlow()

    private val _boundCardLabelFlow = MutableStateFlow<String?>(null)
    val boundCardLabelFlow: StateFlow<String?> = _boundCardLabelFlow.asStateFlow()

    private val _boundCardMaskedFlow = MutableStateFlow<String?>(null)
    val boundCardMaskedFlow: StateFlow<String?> = _boundCardMaskedFlow.asStateFlow()

    private val _blockedPackagesFlow = MutableStateFlow<Set<String>>(prefs.getStringSet(KEY_BLOCKED_PACKAGES, emptySet()) ?: emptySet())
    val blockedPackagesFlow: StateFlow<Set<String>> = _blockedPackagesFlow.asStateFlow()

    private val _emergencyUnlocksLeftFlow = MutableStateFlow(prefs.getInt(KEY_EMERGENCY_UNLOCKS, DEFAULT_EMERGENCY_UNLOCKS))
    val emergencyUnlocksLeftFlow: StateFlow<Int> = _emergencyUnlocksLeftFlow.asStateFlow()

    private val _focusStartTimeFlow = MutableStateFlow(prefs.getLong(KEY_FOCUS_START_TIME, 0L))
    val focusStartTimeFlow: StateFlow<Long> = _focusStartTimeFlow.asStateFlow()

    private val _targetDurationMinutesFlow = MutableStateFlow(prefs.getInt(KEY_TARGET_DURATION_MINUTES, 0))
    val targetDurationMinutesFlow: StateFlow<Int> = _targetDurationMinutesFlow.asStateFlow()

    private val _totalFocusMinutesFlow = MutableStateFlow(prefs.getLong(KEY_TOTAL_FOCUS_MINUTES, 0L))
    val totalFocusMinutesFlow: StateFlow<Long> = _totalFocusMinutesFlow.asStateFlow()

    private val _interceptionsTodayFlow = MutableStateFlow(prefs.getInt(KEY_INTERCEPTIONS_TODAY, 0))
    val interceptionsTodayFlow: StateFlow<Int> = _interceptionsTodayFlow.asStateFlow()

    private val _streakDaysFlow = MutableStateFlow(prefs.getInt(KEY_STREAK_DAYS, 1))
    val streakDaysFlow: StateFlow<Int> = _streakDaysFlow.asStateFlow()

    private val _isStrictModeFlow = MutableStateFlow(prefs.getBoolean(KEY_IS_STRICT_MODE, false))
    val isStrictModeFlow: StateFlow<Boolean> = _isStrictModeFlow.asStateFlow()

    private val _isAntiBypassEnabledFlow = MutableStateFlow(prefs.getBoolean(KEY_IS_ANTI_BYPASS, true))
    val isAntiBypassEnabledFlow: StateFlow<Boolean> = _isAntiBypassEnabledFlow.asStateFlow()

    private val _temporaryPassExpiryFlow = MutableStateFlow(prefs.getLong(KEY_TEMP_PASS_EXPIRY, 0L))
    val temporaryPassExpiryFlow: StateFlow<Long> = _temporaryPassExpiryFlow.asStateFlow()

    private val _hasRecoveryPinFlow = MutableStateFlow(!prefs.getString(KEY_RECOVERY_PIN_HASH, null).isNullOrEmpty())
    val hasRecoveryPinFlow: StateFlow<Boolean> = _hasRecoveryPinFlow.asStateFlow()

    private val _isBunkerModeFlow = MutableStateFlow(prefs.getBoolean(KEY_IS_BUNKER_MODE, false))
    val isBunkerModeFlow: StateFlow<Boolean> = _isBunkerModeFlow.asStateFlow()

    private val _bunkerAllowedPackagesFlow = MutableStateFlow(
        prefs.getStringSet(KEY_BUNKER_ALLOWED_PACKAGES, setOf(
            "com.google.android.dialer",
            "com.android.phone",
            "com.whatsapp",
            "com.google.android.apps.messaging",
            "com.android.mms"
        )) ?: emptySet()
    )
    val bunkerAllowedPackagesFlow: StateFlow<Set<String>> = _bunkerAllowedPackagesFlow.asStateFlow()

    private val _mindfulDelaySecondsFlow = MutableStateFlow(prefs.getInt(KEY_MINDFUL_DELAY_SECONDS, 0))
    val mindfulDelaySecondsFlow: StateFlow<Int> = _mindfulDelaySecondsFlow.asStateFlow()

    init {
        // Populate initial key flows
        _registeredKeysFlow.value = registeredKeys
        _boundCardHashFlow.value = boundCardHash
        _boundCardLabelFlow.value = boundCardLabel
        _boundCardMaskedFlow.value = boundCardMasked

        migrateLegacyKeys()
        checkDayRollover()
    }

    private fun migrateLegacyKeys() {
        val legacyHash = prefs.getString(KEY_BOUND_CARD_HASH, null)
        val legacyMasked = prefs.getString(KEY_BOUND_CARD_MASKED, null) ?: "•••• ••••"
        val legacyLabel = prefs.getString(KEY_BOUND_CARD_LABEL, null) ?: "Llave Principal"

        if (legacyHash != null && registeredKeys.none { it.hash.equals(legacyHash, ignoreCase = true) }) {
            val key = RegisteredKey(legacyHash, legacyMasked, legacyLabel)
            val updated = registeredKeys.toMutableList().apply { add(key) }
            saveKeys(updated)
        }
    }

    private fun checkDayRollover() {
        val todayStr = getTodayDateString()
        val lastDay = prefs.getString(KEY_LAST_RECORDED_DAY, null)

        if (lastDay != todayStr) {
            prefs.edit()
                .putString(KEY_LAST_RECORDED_DAY, todayStr)
                .putInt(KEY_INTERCEPTIONS_TODAY, 0)
                .apply()
            _interceptionsTodayFlow.value = 0
        }
    }

    // Synchronous properties
    var isLocked: Boolean
        get() = prefs.getBoolean(KEY_IS_LOCKED, false)
        set(value) {
            val wasLocked = isLocked
            val now = System.currentTimeMillis()

            val editor = prefs.edit().putBoolean(KEY_IS_LOCKED, value)
            if (value && !wasLocked) {
                // Starting focus session
                editor.putLong(KEY_FOCUS_START_TIME, now)
                _focusStartTimeFlow.value = now
                updateDailyStreak(editor)
            } else if (!value && wasLocked) {
                // Ending focus session, accumulate minutes
                val start = focusStartTime
                if (start > 0) {
                    val minutesSpent = ((now - start) / (1000 * 60)).coerceAtLeast(0)
                    val newTotal = totalFocusMinutes + minutesSpent
                    editor.putLong(KEY_TOTAL_FOCUS_MINUTES, newTotal)
                    _totalFocusMinutesFlow.value = newTotal
                    addMinutesToToday(editor, minutesSpent.toInt())
                }
                editor.putLong(KEY_FOCUS_START_TIME, 0L)
                _focusStartTimeFlow.value = 0L
                editor.putInt(KEY_TARGET_DURATION_MINUTES, 0)
                _targetDurationMinutesFlow.value = 0
                // Cancel temporary pass if any
                editor.putLong(KEY_TEMP_PASS_EXPIRY, 0L)
                _temporaryPassExpiryFlow.value = 0L
            }
            editor.apply()
            _isLockedFlow.value = value

            // Notify widget of state change
            notifyWidgetUpdate()
        }

    var activeProfileId: String
        get() = prefs.getString(KEY_ACTIVE_PROFILE_ID, FocusProfile.DETOX.id) ?: FocusProfile.DETOX.id
        set(value) {
            prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, value).apply()
            _activeProfileIdFlow.value = value
        }

    var targetDurationMinutes: Int
        get() = prefs.getInt(KEY_TARGET_DURATION_MINUTES, 0)
        set(value) {
            prefs.edit().putInt(KEY_TARGET_DURATION_MINUTES, value).apply()
            _targetDurationMinutesFlow.value = value
        }

    val registeredKeys: List<RegisteredKey>
        get() {
            val rawSet = prefs.getStringSet(KEY_REGISTERED_KEYS, emptySet()) ?: emptySet()
            return rawSet.mapNotNull { RegisteredKey.deserialize(it) }
                .sortedBy { it.registeredAt }
        }

    val hasBoundCard: Boolean
        get() = registeredKeys.isNotEmpty() || !boundCardHash.isNullOrEmpty()

    var boundCardHash: String?
        get() = registeredKeys.firstOrNull()?.hash ?: prefs.getString(KEY_BOUND_CARD_HASH, null)
        private set(value) {
            prefs.edit().putString(KEY_BOUND_CARD_HASH, value).apply()
            _boundCardHashFlow.value = value
        }

    var boundCardLabel: String?
        get() = registeredKeys.firstOrNull()?.label ?: prefs.getString(KEY_BOUND_CARD_LABEL, null)
        private set(value) {
            prefs.edit().putString(KEY_BOUND_CARD_LABEL, value).apply()
            _boundCardLabelFlow.value = value
        }

    var boundCardMasked: String?
        get() = registeredKeys.firstOrNull()?.maskedUid ?: prefs.getString(KEY_BOUND_CARD_MASKED, null)
        private set(value) {
            prefs.edit().putString(KEY_BOUND_CARD_MASKED, value).apply()
            _boundCardMaskedFlow.value = value
        }

    var blockedPackages: Set<String>
        get() = prefs.getStringSet(KEY_BLOCKED_PACKAGES, emptySet()) ?: emptySet()
        set(value) {
            prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, value).apply()
            _blockedPackagesFlow.value = value
        }

    var emergencyUnlocksLeft: Int
        get() = prefs.getInt(KEY_EMERGENCY_UNLOCKS, DEFAULT_EMERGENCY_UNLOCKS)
        set(value) {
            prefs.edit().putInt(KEY_EMERGENCY_UNLOCKS, value).apply()
            _emergencyUnlocksLeftFlow.value = value
        }

    var focusStartTime: Long
        get() = prefs.getLong(KEY_FOCUS_START_TIME, 0L)
        private set(value) {
            prefs.edit().putLong(KEY_FOCUS_START_TIME, value).apply()
            _focusStartTimeFlow.value = value
        }

    var totalFocusMinutes: Long
        get() = prefs.getLong(KEY_TOTAL_FOCUS_MINUTES, 0L)
        private set(value) {
            prefs.edit().putLong(KEY_TOTAL_FOCUS_MINUTES, value).apply()
            _totalFocusMinutesFlow.value = value
        }

    var interceptionsToday: Int
        get() = prefs.getInt(KEY_INTERCEPTIONS_TODAY, 0)
        private set(value) {
            prefs.edit().putInt(KEY_INTERCEPTIONS_TODAY, value).apply()
            _interceptionsTodayFlow.value = value
        }

    var streakDays: Int
        get() = prefs.getInt(KEY_STREAK_DAYS, 1)
        private set(value) {
            prefs.edit().putInt(KEY_STREAK_DAYS, value).apply()
            _streakDaysFlow.value = value
        }

    var isStrictMode: Boolean
        get() = prefs.getBoolean(KEY_IS_STRICT_MODE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_STRICT_MODE, value).apply()
            _isStrictModeFlow.value = value
        }

    var isAntiBypassEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_ANTI_BYPASS, true)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_ANTI_BYPASS, value).apply()
            _isAntiBypassEnabledFlow.value = value
        }

    var temporaryPassExpiryTime: Long
        get() = prefs.getLong(KEY_TEMP_PASS_EXPIRY, 0L)
        private set(value) {
            prefs.edit().putLong(KEY_TEMP_PASS_EXPIRY, value).apply()
            _temporaryPassExpiryFlow.value = value
        }

    val isTemporaryPassActive: Boolean
        get() = temporaryPassExpiryTime > System.currentTimeMillis()

    var recoveryPinHash: String?
        get() = prefs.getString(KEY_RECOVERY_PIN_HASH, null)
        private set(value) {
            prefs.edit().putString(KEY_RECOVERY_PIN_HASH, value).apply()
            _hasRecoveryPinFlow.value = !value.isNullOrEmpty()
        }

    var isBunkerMode: Boolean
        get() = prefs.getBoolean(KEY_IS_BUNKER_MODE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_BUNKER_MODE, value).apply()
            _isBunkerModeFlow.value = value
        }

    var bunkerAllowedPackages: Set<String>
        get() = prefs.getStringSet(KEY_BUNKER_ALLOWED_PACKAGES, setOf(
            "com.google.android.dialer",
            "com.android.phone",
            "com.whatsapp",
            "com.google.android.apps.messaging",
            "com.android.mms"
        )) ?: emptySet()
        set(value) {
            prefs.edit().putStringSet(KEY_BUNKER_ALLOWED_PACKAGES, value).apply()
            _bunkerAllowedPackagesFlow.value = value
        }

    var mindfulDelaySeconds: Int
        get() = prefs.getInt(KEY_MINDFUL_DELAY_SECONDS, 0)
        set(value) {
            prefs.edit().putInt(KEY_MINDFUL_DELAY_SECONDS, value).apply()
            _mindfulDelaySecondsFlow.value = value
        }

    val reclaimedMinutesToday: Int
        get() = (interceptionsToday * 15)

    val totalReclaimedMinutes: Long
        get() = totalFocusMinutes + (interceptionsToday * 15L)

    fun toggleBunkerAllowedPackage(pkg: String) {
        if (isLocked) return
        val current = bunkerAllowedPackages.toMutableSet()
        if (current.contains(pkg)) {
            current.remove(pkg)
        } else {
            current.add(pkg)
        }
        bunkerAllowedPackages = current
    }

    /**
     * Activates a 5-minute emergency micro-pass.
     */
    fun startTemporaryPass(durationMinutes: Int = 5): Boolean {
        if (!isLocked) return false
        val expiry = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        temporaryPassExpiryTime = expiry
        return true
    }

    fun cancelTemporaryPass() {
        temporaryPassExpiryTime = 0L
    }

    /**
     * Configures a paper recovery PIN (hashed with SHA-256 for zero-knowledge storage).
     */
    fun setRecoveryPin(pin: String) {
        val hash = NfcHelper.sha256(pin)
        recoveryPinHash = hash
    }

    fun verifyRecoveryPin(pin: String): Boolean {
        val currentHash = recoveryPinHash ?: return false
        val inputHash = NfcHelper.sha256(pin)
        return currentHash.equals(inputHash, ignoreCase = true)
    }

    /**
     * Binds a physical NFC card as a registered key with an assigned profile.
     */
    fun bindCard(
        hash: String,
        maskedUid: String,
        label: String = "Llave Física Maestra",
        assignedProfile: String = FocusProfile.DETOX.id
    ) {
        val current = registeredKeys.toMutableList()
        current.removeAll { it.hash.equals(hash, ignoreCase = true) }
        current.add(RegisteredKey(hash, maskedUid, label, System.currentTimeMillis(), assignedProfile))
        saveKeys(current)

        boundCardHash = current.firstOrNull()?.hash
        boundCardMasked = current.firstOrNull()?.maskedUid
        boundCardLabel = current.firstOrNull()?.label
    }

    fun updateKeyProfile(hash: String, newProfileId: String) {
        val current = registeredKeys.toMutableList()
        val index = current.indexOfFirst { it.hash.equals(hash, ignoreCase = true) }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(assignedProfile = newProfileId)
            saveKeys(current)
        }
    }

    fun getKeyByHash(hash: String): RegisteredKey? {
        return registeredKeys.firstOrNull { it.hash.equals(hash, ignoreCase = true) }
    }

    /**
     * Removes a specific registered key by its hash. Only permitted if unlocked.
     */
    fun removeKey(hash: String): Boolean {
        if (isLocked) return false
        val current = registeredKeys.toMutableList()
        val removed = current.removeAll { it.hash.equals(hash, ignoreCase = true) }
        if (removed) {
            saveKeys(current)
            boundCardHash = current.firstOrNull()?.hash
            boundCardMasked = current.firstOrNull()?.maskedUid
            boundCardLabel = current.firstOrNull()?.label
        }
        return removed
    }

    /**
     * Removes all bound cards. Only permitted if unlocked.
     */
    fun unbindCard(): Boolean {
        if (isLocked) return false
        saveKeys(emptyList())
        prefs.edit()
            .remove(KEY_BOUND_CARD_HASH)
            .remove(KEY_BOUND_CARD_MASKED)
            .remove(KEY_BOUND_CARD_LABEL)
            .apply()

        _boundCardHashFlow.value = null
        _boundCardMaskedFlow.value = null
        _boundCardLabelFlow.value = null
        return true
    }

    private fun saveKeys(keys: List<RegisteredKey>) {
        val rawSet = keys.map { it.serialize() }.toSet()
        prefs.edit().putStringSet(KEY_REGISTERED_KEYS, rawSet).apply()
        _registeredKeysFlow.value = keys
    }

    /**
     * Checks if a scanned NFC card hash matches any of the registered keys.
     */
    fun matchesBoundCard(scannedHash: String): Boolean {
        if (registeredKeys.any { it.hash.equals(scannedHash, ignoreCase = true) }) {
            return true
        }
        val legacy = prefs.getString(KEY_BOUND_CARD_HASH, null) ?: return false
        return legacy.equals(scannedHash, ignoreCase = true)
    }

    /**
     * Increments the count of blocked application openings today.
     */
    fun recordInterception() {
        checkDayRollover()
        val current = prefs.getInt(KEY_INTERCEPTIONS_TODAY, 0)
        interceptionsToday = current + 1
    }

    private fun updateDailyStreak(editor: SharedPreferences.Editor) {
        val todayStr = getTodayDateString()
        val lastStreakDay = prefs.getString(KEY_LAST_STREAK_DAY, "")

        if (lastStreakDay != todayStr) {
            val currentStreak = prefs.getInt(KEY_STREAK_DAYS, 1)
            val newStreak = currentStreak + 1
            editor.putString(KEY_LAST_STREAK_DAY, todayStr)
            editor.putInt(KEY_STREAK_DAYS, newStreak)
            _streakDaysFlow.value = newStreak
        }
    }

    private fun addMinutesToToday(editor: SharedPreferences.Editor, minutes: Int) {
        val todayStr = getTodayDateString()
        val key = KEY_DAILY_MINUTES_PREFIX + todayStr
        val currentMin = prefs.getInt(key, 0)
        editor.putInt(key, currentMin + minutes)
    }

    fun getWeeklyStats(): List<DayFocusStat> {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayNameSdf = SimpleDateFormat("EEE", Locale("es", "ES"))
        val todayStr = sdf.format(cal.time)

        val daysData = mutableListOf<DayFocusStat>()
        for (i in 0 until 7) {
            val dateStr = sdf.format(cal.time)
            val dayName = dayNameSdf.format(cal.time).replace(".", "").replaceFirstChar { it.uppercase() }
            val minutes = prefs.getInt(KEY_DAILY_MINUTES_PREFIX + dateStr, 0)
            daysData.add(0, DayFocusStat(dayLabel = dayName, minutes = minutes, isToday = (dateStr == todayStr)))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return daysData
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun isPackageBlocked(packageName: String): Boolean {
        // If 5-min temporary micro-pass is active, pause blocking
        if (isTemporaryPassActive) return false

        // Bunker Mode: Blocks everything EXCEPT bunkerAllowedPackages
        if (isBunkerMode) {
            return !bunkerAllowedPackages.contains(packageName)
        }

        // Standard Mode: Check if package is blocked in current active profile
        return blockedPackages.contains(packageName)
    }

    /**
     * Exports full configuration into an offline JSON backup string.
     */
    fun exportBackupJson(): String {
        val keysArray = JSONArray()
        for (key in registeredKeys) {
            val obj = JSONObject().apply {
                put("hash", key.hash)
                put("maskedUid", key.maskedUid)
                put("label", key.label)
                put("registeredAt", key.registeredAt)
                put("assignedProfile", key.assignedProfile)
            }
            keysArray.put(obj)
        }

        val blockedArray = JSONArray()
        blockedPackages.forEach { blockedArray.put(it) }

        val bunkerAllowedArray = JSONArray()
        bunkerAllowedPackages.forEach { bunkerAllowedArray.put(it) }

        val root = JSONObject().apply {
            put("version", 2)
            put("exportedAt", System.currentTimeMillis())
            put("keys", keysArray)
            put("blockedPackages", blockedArray)
            put("bunkerAllowedPackages", bunkerAllowedArray)
            put("isBunkerMode", isBunkerMode)
            put("isStrictMode", isStrictMode)
            put("isAntiBypass", isAntiBypassEnabled)
            put("mindfulDelaySeconds", mindfulDelaySeconds)
            put("activeProfileId", activeProfileId)
        }
        return root.toString(2)
    }

    /**
     * Restores configuration from a JSON backup string with integrity checks.
     */
    fun importBackupJson(rawJson: String): Boolean {
        if (isLocked) return false
        return try {
            val root = JSONObject(rawJson)
            if (root.has("keys")) {
                val keysArray = root.getJSONArray("keys")
                val restoredKeys = mutableListOf<RegisteredKey>()
                for (i in 0 until keysArray.length()) {
                    val item = keysArray.getJSONObject(i)
                    val key = RegisteredKey(
                        hash = item.getString("hash"),
                        maskedUid = item.getString("maskedUid"),
                        label = item.getString("label"),
                        registeredAt = item.optLong("registeredAt", System.currentTimeMillis()),
                        assignedProfile = item.optString("assignedProfile", FocusProfile.DETOX.id)
                    )
                    restoredKeys.add(key)
                }
                saveKeys(restoredKeys)
            }

            if (root.has("blockedPackages")) {
                val blockedArray = root.getJSONArray("blockedPackages")
                val set = mutableSetOf<String>()
                for (i in 0 until blockedArray.length()) {
                    set.add(blockedArray.getString(i))
                }
                blockedPackages = set
            }

            if (root.has("bunkerAllowedPackages")) {
                val allowedArray = root.getJSONArray("bunkerAllowedPackages")
                val set = mutableSetOf<String>()
                for (i in 0 until allowedArray.length()) {
                    set.add(allowedArray.getString(i))
                }
                bunkerAllowedPackages = set
            }

            if (root.has("isBunkerMode")) isBunkerMode = root.getBoolean("isBunkerMode")
            if (root.has("isStrictMode")) isStrictMode = root.getBoolean("isStrictMode")
            if (root.has("isAntiBypass")) isAntiBypassEnabled = root.getBoolean("isAntiBypass")
            if (root.has("mindfulDelaySeconds")) mindfulDelaySeconds = root.getInt("mindfulDelaySeconds")
            if (root.has("activeProfileId")) activeProfileId = root.getString("activeProfileId")
            true
        } catch (_: Exception) {
            false
        }
    }

    fun togglePackageBlocked(packageName: String) {
        if (isLocked) return
        val current = blockedPackages.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        blockedPackages = current
    }

    fun addAllPackages(packages: Collection<String>) {
        if (isLocked) return
        val current = blockedPackages.toMutableSet()
        current.addAll(packages)
        blockedPackages = current
    }

    fun clearAllBlockedPackages() {
        if (isLocked) return
        blockedPackages = emptySet()
    }

    fun useEmergencyUnlock(): Boolean {
        if (isStrictMode) return false
        val left = emergencyUnlocksLeft
        if (left > 0) {
            emergencyUnlocksLeft = left - 1
            isLocked = false
            return true
        }
        return false
    }

    private fun notifyWidgetUpdate() {
        val intent = Intent("com.freetag.app.UPDATE_WIDGET")
        context.sendBroadcast(intent)
    }

    companion object {
        private const val PREFS_NAME = "freetag_secure_prefs"
        private const val KEY_IS_LOCKED = "pref_is_locked"
        private const val KEY_REGISTERED_KEYS = "pref_registered_keys_set"
        private const val KEY_ACTIVE_PROFILE_ID = "pref_active_profile_id"
        private const val KEY_BOUND_CARD_HASH = "pref_bound_card_hash"
        private const val KEY_BOUND_CARD_LABEL = "pref_bound_card_label"
        private const val KEY_BOUND_CARD_MASKED = "pref_bound_card_masked"
        private const val KEY_BLOCKED_PACKAGES = "pref_blocked_packages"
        private const val KEY_EMERGENCY_UNLOCKS = "pref_emergency_unlocks"
        private const val KEY_FOCUS_START_TIME = "pref_focus_start_time"
        private const val KEY_TARGET_DURATION_MINUTES = "pref_target_duration_minutes"
        private const val KEY_TOTAL_FOCUS_MINUTES = "pref_total_focus_minutes"
        private const val KEY_INTERCEPTIONS_TODAY = "pref_interceptions_today"
        private const val KEY_LAST_RECORDED_DAY = "pref_last_recorded_day"
        private const val KEY_STREAK_DAYS = "pref_streak_days"
        private const val KEY_LAST_STREAK_DAY = "pref_last_streak_day"
        private const val KEY_IS_STRICT_MODE = "pref_is_strict_mode"
        private const val KEY_IS_ANTI_BYPASS = "pref_is_anti_bypass"
        private const val KEY_TEMP_PASS_EXPIRY = "pref_temp_pass_expiry"
        private const val KEY_RECOVERY_PIN_HASH = "pref_recovery_pin_hash"
        private const val KEY_DAILY_MINUTES_PREFIX = "pref_day_min_"
        private const val KEY_IS_BUNKER_MODE = "pref_is_bunker_mode"
        private const val KEY_BUNKER_ALLOWED_PACKAGES = "pref_bunker_allowed_packages"
        private const val KEY_MINDFUL_DELAY_SECONDS = "pref_mindful_delay_seconds"

        const val DEFAULT_EMERGENCY_UNLOCKS = 3
    }
}
