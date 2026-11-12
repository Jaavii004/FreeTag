package com.freetag.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freetag.app.data.DayFocusStat
import com.freetag.app.ui.components.AccessibilityGuideDialog
import com.freetag.app.ui.components.EmergencyUnlockDialog
import com.freetag.app.ui.components.NfcRadarAnimation
import com.freetag.app.ui.components.WeeklyFocusChart
import com.freetag.app.ui.theme.AlertRed
import com.freetag.app.ui.theme.EmeraldDark
import com.freetag.app.ui.theme.EmeraldLight
import com.freetag.app.ui.theme.EmeraldPrimary
import com.freetag.app.ui.theme.ObsidianBg
import com.freetag.app.ui.theme.PureWhite
import com.freetag.app.ui.theme.SurfaceBorder
import com.freetag.app.ui.theme.SurfaceDark
import com.freetag.app.ui.theme.SurfaceElevated
import com.freetag.app.ui.theme.TextPrimary
import com.freetag.app.ui.theme.TextSecondary
import com.freetag.app.ui.theme.TextTertiary
import com.freetag.app.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun DashboardScreen(
    isLocked: Boolean,
    focusStartTime: Long,
    targetDurationMinutes: Int,
    totalFocusMinutes: Long,
    hasBoundCard: Boolean,
    boundCardLabel: String?,
    boundCardMasked: String?,
    blockedAppsCount: Int,
    interceptionsToday: Int,
    streakDays: Int,
    isStrictMode: Boolean,
    isAntiBypassEnabled: Boolean,
    isBunkerMode: Boolean,
    mindfulDelaySeconds: Int,
    weeklyStats: List<DayFocusStat>,
    isAccessibilityEnabled: Boolean,
    isNfcEnabled: Boolean,
    remainingEmergencyUnlocks: Int,
    onToggleStrictMode: (Boolean) -> Unit,
    onToggleAntiBypass: (Boolean) -> Unit,
    onToggleBunkerMode: (Boolean) -> Unit,
    onSelectMindfulDelay: (Int) -> Unit,
    onSelectDurationMinutes: (Int) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenNfcSettings: () -> Unit,
    onNavigateToKeys: () -> Unit,
    onNavigateToApps: () -> Unit,
    onEmergencyUnlocked: () -> Unit,
    onAutoUnlockTimeout: () -> Unit,
    onArmLockWithCardPrompt: () -> Unit
) {
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showAccessibilityGuide by remember { mutableStateOf(false) }
    var currentSessionSeconds by remember { mutableLongStateOf(0L) }

    // Live focus timer counter & auto-timeout check
    LaunchedEffect(isLocked, focusStartTime, targetDurationMinutes) {
        if (isLocked && focusStartTime > 0L) {
            while (true) {
                val elapsed = ((System.currentTimeMillis() - focusStartTime) / 1000L).coerceAtLeast(0L)
                currentSessionSeconds = elapsed

                // Check timed session expiration
                if (targetDurationMinutes > 0) {
                    val targetSeconds = targetDurationMinutes * 60L
                    if (elapsed >= targetSeconds) {
                        onAutoUnlockTimeout()
                        break
                    }
                }
                delay(1000L)
            }
        } else {
            currentSessionSeconds = 0L
        }
    }

    if (showEmergencyDialog) {
        EmergencyUnlockDialog(
            remainingUnlocks = remainingEmergencyUnlocks,
            isStrictMode = isStrictMode,
            onDismissRequest = { showEmergencyDialog = false },
            onUnlocked = {
                showEmergencyDialog = false
                onEmergencyUnlocked()
            }
        )
    }

    if (showAccessibilityGuide) {
        AccessibilityGuideDialog(
            onDismissRequest = { showAccessibilityGuide = false },
            onOpenSettings = { onOpenAccessibilitySettings() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Accessibility Warning Banner
        AnimatedVisibility(visible = !isAccessibilityEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .border(1.5.dp, WarningAmber, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(WarningAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Permiso de Accesibilidad Requerido",
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Necesario para detectar e interceptar las apps que decidas bloquear.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onOpenAccessibilitySettings() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarningAmber,
                                contentColor = ObsidianBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Activar Permiso",
                                color = ObsidianBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = { showAccessibilityGuide = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceElevated,
                                contentColor = PureWhite
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Instrucciones",
                                color = PureWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // NFC Disabled Warning Banner
        AnimatedVisibility(visible = !isNfcEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .border(1.5.dp, AlertRed, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AlertRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Nfc,
                                contentDescription = null,
                                tint = AlertRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sensor NFC Desactivado",
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Activa el NFC en Ajustes para poder leer tus tarjetas físicas.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onOpenNfcSettings() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertRed,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Activar Sensor NFC",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // State Pill Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(if (isLocked) EmeraldDark else SurfaceElevated)
                .border(
                    width = 1.dp,
                    color = if (isLocked) EmeraldPrimary else SurfaceBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(if (isLocked) EmeraldLight else EmeraldPrimary.copy(alpha = 0.6f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLocked) {
                        if (targetDurationMinutes > 0) "SESIÓN PROGRAMADA (${targetDurationMinutes}M)" else "MODO PRESENCIA ACTIVO"
                    } else "DISPOSITIVO DESBLOQUEADO",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isLocked) EmeraldLight else PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Central Radar / Lock Visual
        NfcRadarAnimation(
            size = 200.dp,
            isActive = isLocked,
            centerIconColor = if (isLocked) EmeraldPrimary else TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLocked) {
            // Timer Display: Countdown if timed, Count-up if indefinite
            val displaySeconds = if (targetDurationMinutes > 0) {
                ((targetDurationMinutes * 60L) - currentSessionSeconds).coerceAtLeast(0L)
            } else {
                currentSessionSeconds
            }

            val hours = displaySeconds / 3600
            val minutes = (displaySeconds % 3600) / 60
            val seconds = displaySeconds % 60
            val timeString = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge,
                fontFamily = FontFamily.Monospace,
                color = PureWhite
            )

            Text(
                text = if (targetDurationMinutes > 0) "Tiempo restante de concentración" else "Tiempo en estado de presencia",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Acerca tu tarjeta física (${boundCardMasked ?: "Llave"}) a la parte trasera del móvil para desbloquear.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Emergency Unlock Option
            Button(
                onClick = { showEmergencyDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceElevated,
                    contentColor = if (isStrictMode) TextSecondary else WarningAmber
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isStrictMode) SurfaceBorder else WarningAmber.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isStrictMode) "Modo Estricto (Sin Emergencia)" else "Desbloqueo de Emergencia ($remainingEmergencyUnlocks/3)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        } else {
            // Unlocked State: Preset Duration Selector
            Text(
                text = "Duración de la Sesión",
                style = MaterialTheme.typography.titleSmall,
                color = PureWhite,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val durations = listOf(
                    0 to "Indefinido",
                    25 to "Pomodoro (25m)",
                    45 to "45 min",
                    60 to "1 hora",
                    120 to "2 horas"
                )
                durations.forEach { (mins, label) ->
                    val isSelected = targetDurationMinutes == mins
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectDurationMinutes(mins) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PureWhite else TextPrimary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceElevated,
                            labelColor = TextPrimary,
                            selectedContainerColor = EmeraldDark,
                            selectedLabelColor = PureWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SurfaceBorder,
                            selectedBorderColor = EmeraldPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (hasBoundCard) {
                    "Llave vinculada: ${boundCardLabel ?: "Tarjeta"} ($boundCardMasked)\nAcerca tu tarjeta o pulsa el botón para bloquear."
                } else {
                    "Vincula una tarjeta contactless física antes de activar el bloqueo."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Lock Action Button
            Button(
                onClick = {
                    if (hasBoundCard) {
                        onArmLockWithCardPrompt()
                    } else {
                        onNavigateToKeys()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = if (hasBoundCard) Icons.Rounded.Lock else Icons.Rounded.CreditCard,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (hasBoundCard) {
                        if (targetDurationMinutes > 0) "INICIAR SESIÓN (${targetDurationMinutes}M)" else "ACTIVAR MODO PRESENCIA"
                    } else "VINCULAR MI PRIMERA TARJETA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PureWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Status 3 Quick Cards (Apps, Interceptions avoided today, Streak)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DashboardMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Apps,
                label = "Bloqueadas",
                value = "$blockedAppsCount",
                onClick = onNavigateToApps
            )

            DashboardMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Shield,
                label = "Evitadas hoy",
                value = "$interceptionsToday",
                onClick = {}
            )

            DashboardMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.LocalFireDepartment,
                label = "Racha",
                value = "${streakDays}d",
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reclaimed Life Time Psychological Card
        if (interceptionsToday > 0) {
            val reclaimedMins = interceptionsToday * 15
            val hours = reclaimedMins / 60
            val mins = reclaimedMins % 60
            val timeReclaimedText = if (hours > 0) "${hours}h ${mins}m" else "${mins} min"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(EmeraldDark.copy(alpha = 0.5f), SurfaceDark)
                        )
                    )
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.HourglassBottom,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "~$timeReclaimedText de Vida Recuperada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            text = "Has esquivado $interceptionsToday aperturas compulsivas de scroll (~15 min c/u).",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Weekly Focus Chart
        WeeklyFocusChart(stats = weeklyStats)

        Spacer(modifier = Modifier.height(24.dp))

        // Strict Mode Toggle Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Modo Estricto Absoluto",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Deshabilita el botón de emergencia de 60s. Solo tu tarjeta física podrá desbloquear.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isStrictMode,
                    onCheckedChange = { if (!isLocked) onToggleStrictMode(it) },
                    enabled = !isLocked,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureWhite,
                        checkedTrackColor = EmeraldPrimary,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = SurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Anti-Bypass Protection Toggle Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Escudo Anti-Desinstalación",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Impide acceder al desinstalador del sistema mientras el bloqueo esté activo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isAntiBypassEnabled,
                    onCheckedChange = { if (!isLocked) onToggleAntiBypass(it) },
                    enabled = !isLocked,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureWhite,
                        checkedTrackColor = EmeraldPrimary,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = SurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bunker Mode (Whitelist Only) Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(
                    1.dp,
                    if (isBunkerMode) EmeraldPrimary.copy(alpha = 0.6f) else SurfaceBorder,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Modo Búnker (Kiosko Exclusivo)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isBunkerMode) EmeraldLight else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Bloquea TODO el dispositivo excepto apps autorizadas esenciales. Máxima concentración.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isBunkerMode,
                    onCheckedChange = { if (!isLocked) onToggleBunkerMode(it) },
                    enabled = !isLocked,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureWhite,
                        checkedTrackColor = EmeraldPrimary,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = SurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mindful Delay (One-Sec Friction) Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Pausa Consciente ('One-Sec Friction')",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Obliga a esperar unos segundos al abrir una app antes de permitir pases o desbloqueos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val delayOptions = listOf(0 to "0s (Off)", 5 to "5s", 10 to "10s", 15 to "15s")
                    delayOptions.forEach { (sec, label) ->
                        val isSelected = mindfulDelaySeconds == sec
                        FilterChip(
                            selected = isSelected,
                            onClick = { if (!isLocked) onSelectMindfulDelay(sec) },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PureWhite else TextPrimary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceElevated,
                                labelColor = TextPrimary,
                                selectedContainerColor = EmeraldDark,
                                selectedLabelColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = SurfaceBorder,
                                selectedBorderColor = EmeraldPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EmeraldLight,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 18.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}
