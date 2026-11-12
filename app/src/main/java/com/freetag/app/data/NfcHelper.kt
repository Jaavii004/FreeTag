package com.freetag.app.data

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.security.MessageDigest
import java.util.Locale

/**
 * Universal NFC Helper.
 * Reads ONLY the hardware chip UID (tag.id) via low-level Reader Mode.
 * NEVER reads financial, bank account, card numbers, or private cryptographic applets.
 * Hashes the UID using SHA-256 for zero-knowledge storage.
 */
object NfcHelper {

    // Reader mode flags to support all standard contactless cards (ISO-A, ISO-B, Felica, ISO-V)
    const val READER_FLAGS: Int =
        NfcAdapter.FLAG_READER_NFC_A or
        NfcAdapter.FLAG_READER_NFC_B or
        NfcAdapter.FLAG_READER_NFC_F or
        NfcAdapter.FLAG_READER_NFC_V or
        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

    /**
     * Checks if NFC hardware is present on the device.
     */
    fun hasNfc(context: Context): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return adapter != null
    }

    /**
     * Checks if NFC is currently enabled in system settings.
     */
    fun isNfcEnabled(context: Context): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return adapter?.isEnabled == true
    }

    /**
     * Extracts tag.id, formats it as Hexadecimal, and returns its SHA-256 hash.
     */
    fun processTagToHash(tag: Tag): ScannedCardInfo? {
        val rawId = tag.id ?: return null
        if (rawId.isEmpty()) return null

        val hexUid = bytesToHex(rawId)
        val sha256Hash = sha256(hexUid)
        val maskedDisplay = maskUid(hexUid)

        return ScannedCardInfo(
            rawHexUid = hexUid,
            sha256Hash = sha256Hash,
            maskedDisplay = maskedDisplay,
            techList = tag.techList?.toList() ?: emptyList()
        )
    }

    /**
     * Enables Reader Mode with optimized flags.
     */
    fun startReaderMode(
        activity: Activity,
        onTagScanned: (ScannedCardInfo) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter == null) {
            onError?.invoke("Este dispositivo no cuenta con hardware NFC.")
            return
        }
        if (!adapter.isEnabled) {
            onError?.invoke("El sensor NFC está desactivado en los Ajustes.")
            return
        }

        val callback = NfcAdapter.ReaderCallback { tag ->
            val scannedInfo = processTagToHash(tag)
            activity.runOnUiThread {
                if (scannedInfo != null) {
                    vibrateSuccess(activity)
                    onTagScanned(scannedInfo)
                } else {
                    vibrateError(activity)
                    onError?.invoke("No se pudo leer el identificador del tag.")
                }
            }
        }

        val options = Bundle()
        // Reduce presence check delay for snappier tag detection
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 100)

        adapter.enableReaderMode(activity, callback, READER_FLAGS, options)
    }

    /**
     * Disables Reader Mode when activity pauses.
     */
    fun stopReaderMode(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        adapter?.disableReaderMode(activity)
    }

    /**
     * Converts a byte array to an uppercase Hex string.
     */
    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format(Locale.US, "%02X", b))
        }
        return sb.toString()
    }

    /**
     * Generates a SHA-256 hash string for the provided input string.
     */
    fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytesToHex(digest)
    }

    /**
     * Formats UID for safe UI display (e.g. "•••• •••• A3F2").
     */
    private fun maskUid(hexUid: String): String {
        return if (hexUid.length > 4) {
            "•••• •••• " + hexUid.takeLast(4)
        } else {
            "•••• $hexUid"
        }
    }

    /**
     * Haptic feedback on successful tag detection.
     */
    fun vibrateSuccess(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 40, 60, 50)
            val amplitudes = intArrayOf(0, 180, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(120)
        }
    }

    /**
     * Heavy mechanical safe-lock haptic sensation when arming the physical lock.
     */
    fun vibrateMechanicalLock(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 25, 40, 30, 40, 90)
            val amplitudes = intArrayOf(0, 90, 0, 160, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(180)
        }
    }

    /**
     * Combined mechanical haptics and subtle click tone for arming the lock.
     */
    fun playLockFeedback(context: Context) {
        vibrateMechanicalLock(context)
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
            toneGen.release()
        } catch (_: Exception) {}
    }

    /**
     * Satisfying dual crisp click when disarming the lock.
     */
    fun vibrateMechanicalUnlock(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 35, 70, 45)
            val amplitudes = intArrayOf(0, 220, 0, 170)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    /**
     * Combined mechanical haptics and crisp chime tone for unlocking.
     */
    fun playUnlockFeedback(context: Context) {
        vibrateMechanicalUnlock(context)
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 120)
            toneGen.release()
        } catch (_: Exception) {}
    }

    /**
     * Haptic feedback on error or invalid tag.
     */
    fun vibrateError(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 80, 50, 80)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

/**
 * Data class representing a safely scanned NFC Tag.
 */
data class ScannedCardInfo(
    val rawHexUid: String,
    val sha256Hash: String,
    val maskedDisplay: String,
    val techList: List<String>
)
