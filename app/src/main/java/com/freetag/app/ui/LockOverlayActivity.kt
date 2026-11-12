package com.freetag.app.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.freetag.app.FreeTagApp
import com.freetag.app.data.NfcHelper
import com.freetag.app.data.PreferenceManager
import com.freetag.app.ui.screens.LockOverlayScreen
import com.freetag.app.ui.theme.FreeTagTheme

/**
 * Fullscreen interception activity presented when a blocked application is launched.
 * Enforces hardware NFC card scanning to dismiss.
 */
class LockOverlayActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private var blockedPackage: String = ""
    private var appName: String = "Aplicación"
    private var appIcon: Drawable? = null
    private var errorMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = (application as FreeTagApp).preferenceManager

        extractBlockedAppDetails(intent)

        // Intercept back button: send user to Home launcher rather than returning to blocked app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHomeScreen()
            }
        })

        setContent {
            FreeTagTheme {
                val boundCardMasked by preferenceManager.boundCardMaskedFlow.collectAsState()
                val remainingUnlocks by preferenceManager.emergencyUnlocksLeftFlow.collectAsState()
                val interceptionsToday by preferenceManager.interceptionsTodayFlow.collectAsState()
                val isStrictMode by preferenceManager.isStrictModeFlow.collectAsState()
                val hasRecoveryPin by preferenceManager.hasRecoveryPinFlow.collectAsState()
                val mindfulDelay by preferenceManager.mindfulDelaySecondsFlow.collectAsState()

                LockOverlayScreen(
                    blockedAppName = appName,
                    blockedAppIcon = appIcon,
                    boundCardMasked = boundCardMasked,
                    remainingEmergencyUnlocks = remainingUnlocks,
                    interceptionsToday = interceptionsToday,
                    isStrictMode = isStrictMode,
                    hasRecoveryPin = hasRecoveryPin,
                    mindfulDelaySeconds = mindfulDelay,
                    errorMessage = errorMessage,
                    onGoHome = { navigateToHomeScreen() },
                    onEmergencyUnlocked = { handleEmergencyUnlock() },
                    onActivateTemporaryPass = {
                        preferenceManager.startTemporaryPass(5)
                        Toast.makeText(this@LockOverlayActivity, "Pase de 5 minutos activado. ¡Aprovecha el tiempo!", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onVerifyRecoveryPin = { pin ->
                        val ok = preferenceManager.verifyRecoveryPin(pin)
                        if (ok) {
                            preferenceManager.isLocked = false
                            NfcHelper.vibrateSuccess(this@LockOverlayActivity)
                            Toast.makeText(this@LockOverlayActivity, "¡Desbloqueado con PIN de recuperación!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            NfcHelper.vibrateError(this@LockOverlayActivity)
                            errorMessage = "PIN de recuperación incorrecto."
                        }
                        ok
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractBlockedAppDetails(intent)

        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
        if (tag != null) {
            handleNfcTag(tag)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!preferenceManager.isLocked || preferenceManager.isTemporaryPassActive) {
            finish()
            return
        }

        NfcHelper.startReaderMode(
            activity = this,
            onTagScanned = { scanned ->
                verifyAndUnlock(scanned.sha256Hash)
            },
            onError = { msg ->
                errorMessage = msg
            }
        )
    }

    override fun onPause() {
        super.onPause()
        NfcHelper.stopReaderMode(this)
    }

    private fun extractBlockedAppDetails(intent: Intent?) {
        blockedPackage = intent?.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        if (blockedPackage.isNotEmpty()) {
            try {
                val pm = packageManager
                val appInfo = pm.getApplicationInfo(blockedPackage, 0)
                appName = pm.getApplicationLabel(appInfo).toString()
                appIcon = pm.getApplicationIcon(appInfo)
            } catch (_: PackageManager.NameNotFoundException) {
                appName = blockedPackage
            }
        }
    }

    private fun verifyAndUnlock(scannedHash: String) {
        if (preferenceManager.matchesBoundCard(scannedHash)) {
            preferenceManager.isLocked = false
            NfcHelper.playUnlockFeedback(this)
            Toast.makeText(this, "¡Modo Presencia desbloqueado!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            NfcHelper.vibrateError(this)
            errorMessage = "Tarjeta no autorizada. Usa una de tus llaves vinculadas."
        }
    }

    private fun handleNfcTag(tag: Tag) {
        val scanned = NfcHelper.processTagToHash(tag)
        if (scanned != null) {
            verifyAndUnlock(scanned.sha256Hash)
        } else {
            errorMessage = "No se pudo leer el tag NFC."
        }
    }

    private fun handleEmergencyUnlock() {
        val success = preferenceManager.useEmergencyUnlock()
        if (success) {
            Toast.makeText(this, "Desbloqueo de emergencia activado", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "No es posible desbloquear en este momento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"

        fun start(context: Context, blockedPackage: String) {
            val intent = Intent(context, LockOverlayActivity::class.java).apply {
                putExtra(EXTRA_BLOCKED_PACKAGE, blockedPackage)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }
    }
}
