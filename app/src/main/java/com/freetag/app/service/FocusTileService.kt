package com.freetag.app.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.freetag.app.FreeTagApp
import com.freetag.app.data.NfcHelper
import com.freetag.app.data.PreferenceManager
import com.freetag.app.ui.MainActivity
import com.freetag.app.util.FocusNotificationManager

@RequiresApi(Build.VERSION_CODES.N)
class FocusTileService : TileService() {

    private val preferenceManager: PreferenceManager by lazy {
        (application as FreeTagApp).preferenceManager
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val isLocked = preferenceManager.isLocked

        if (isLocked) {
            // Already locked: must tap NFC card to unlock
            NfcHelper.vibrateError(this)
            Toast.makeText(this, "Acerca tu tarjeta física NFC para desbloquear", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ pending intent background launch
                val pendingIntent = android.app.PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    android.app.PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        } else {
            // Not locked: check if card is bound
            if (!preferenceManager.hasBoundCard) {
                Toast.makeText(this, "Vincula primero una tarjeta física en FreeTag", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
                return
            }

            // Arm the lock
            preferenceManager.isLocked = true
            NfcHelper.vibrateSuccess(this)
            FocusNotificationManager.showFocusNotification(this)
            Toast.makeText(this, "¡Modo Presencia Activado!", Toast.LENGTH_SHORT).show()
            updateTileState()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isLocked = preferenceManager.isLocked

        if (isLocked) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Presencia Activa"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Bloqueado con NFC"
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Modo Presencia"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Toca para activar"
            }
        }
        tile.updateTile()
    }
}

