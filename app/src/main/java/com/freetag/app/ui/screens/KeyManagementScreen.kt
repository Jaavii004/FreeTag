package com.freetag.app.ui.screens

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freetag.app.data.FocusProfile
import com.freetag.app.data.RegisteredKey
import com.freetag.app.ui.components.BackupManagementDialog
import com.freetag.app.ui.components.NfcRadarAnimation
import com.freetag.app.ui.components.PaperRecoveryDialog
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

@Composable
fun KeyManagementScreen(
    isLocked: Boolean,
    registeredKeys: List<RegisteredKey>,
    lastScannedTempCard: String?,
    hasRecoveryPin: Boolean,
    onBindCardConfirmed: (label: String, profileId: String) -> Unit,
    onUpdateKeyProfile: (hash: String, profileId: String) -> Unit,
    onSaveRecoveryPin: (pin: String) -> Unit,
    onExportBackup: () -> String,
    onImportBackup: (String) -> Boolean,
    onRemoveKey: (hash: String) -> Unit,
    onClearAllKeys: () -> Unit
) {
    var newKeyLabel by remember { mutableStateOf("Tarjeta de Respaldo") }
    var selectedProfileId by remember { mutableStateOf(FocusProfile.DETOX.id) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }

    if (showBackupDialog) {
        BackupManagementDialog(
            exportedJson = onExportBackup(),
            onDismissRequest = { showBackupDialog = false },
            onImportJson = { json ->
                val ok = onImportBackup(json)
                if (ok) showBackupDialog = false
                ok
            }
        )
    }

    if (showRecoveryDialog) {
        PaperRecoveryDialog(
            isSetupMode = true,
            onDismissRequest = { showRecoveryDialog = false },
            onPinVerifiedOrSaved = { pin ->
                onSaveRecoveryPin(pin)
                showRecoveryDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Llavero Físico & Perfiles",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Vincula tarjetas a perfiles específicos: tu tarjeta bancaria puede activar 'Trabajo' y tu abono 'Social Detox'.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLocked) {
            // Anti-tamper banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDark)
                    .border(1.dp, AlertRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "El modo bloqueo está activo. Por seguridad, no puedes alterar o desvincular llaves hasta desbloquear la app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // New Card Scanned Confirmation Box
        if (lastScannedTempCard != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(EmeraldDark.copy(alpha = 0.35f))
                    .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(EmeraldDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¡Tarjeta NFC Detectada!",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite
                    )

                    Text(
                        text = "Identificador: $lastScannedTempCard",
                        fontFamily = FontFamily.Monospace,
                        color = EmeraldLight,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newKeyLabel,
                        onValueChange = { newKeyLabel = it },
                        label = { Text("Nombre para esta llave") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Profile assignment
                    Text(
                        text = "Perfil que activará esta tarjeta:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FocusProfile.values().forEach { profile ->
                            val isSelected = selectedProfileId == profile.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedProfileId = profile.id },
                                label = {
                                    Text(
                                        text = profile.title,
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

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { onBindCardConfirmed(newKeyLabel, selectedProfileId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vincular Tarjeta con este Perfil", fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // List of registered cards
        if (registeredKeys.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Llaves Registradas (${registeredKeys.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            registeredKeys.forEachIndexed { index, key ->
                val profile = FocusProfile.values().find { it.id == key.assignedProfile } ?: FocusProfile.DETOX
                PhysicalCardVisual(
                    cardLabel = key.label,
                    maskedUid = key.maskedUid,
                    profileTitle = profile.title,
                    isMaster = index == 0,
                    canDelete = !isLocked,
                    onDelete = { onRemoveKey(key.hash) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else if (lastScannedTempCard == null) {
            // No card bound yet: Animated scan prompt
            NfcRadarAnimation(
                size = 180.dp,
                isActive = true,
                centerIconColor = EmeraldPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Acerca cualquier tarjeta física contactless",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Pégala a la parte trasera de tu teléfono para detectarla y registrarla como tu llave de bloqueo.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Paper Backup PIN Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .clickable(enabled = !isLocked) { showRecoveryDialog = true }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Rounded.Password,
                        contentDescription = null,
                        tint = if (hasRecoveryPin) EmeraldLight else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (hasRecoveryPin) "PIN Físico de Papel Configurado" else "Configurar PIN de Papel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (hasRecoveryPin) "PIN de emergencia de respaldo activo." else "Crea un código offline por si viajas sin tarjeta.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasRecoveryPin) EmeraldDark else SurfaceElevated)
                        .border(
                            1.dp,
                            if (hasRecoveryPin) EmeraldPrimary else SurfaceBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (hasRecoveryPin) "Editar" else "Configurar",
                        color = if (hasRecoveryPin) EmeraldLight else PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Backup & Restore Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .clickable(enabled = !isLocked) { showBackupDialog = true }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDownload,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Copia de Seguridad Offline",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Exporta o restaura tus llaves y apps en formato JSON.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Gestionar",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = EmeraldLight,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Privacidad de Conocimiento Cero",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "FreeTag solo lee el UID público del chip contactless y lo transforma inmediatamente en un hash SHA-256 irreversible. Nunca lee cuentas bancarias ni datos personales.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PhysicalCardVisual(
    cardLabel: String,
    maskedUid: String,
    profileTitle: String,
    isMaster: Boolean = false,
    canDelete: Boolean = false,
    onDelete: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = if (isMaster) {
                        listOf(EmeraldDark, SurfaceDark)
                    } else {
                        listOf(SurfaceElevated, SurfaceDark)
                    }
                )
            )
            .border(
                width = 1.dp,
                color = if (isMaster) EmeraldPrimary.copy(alpha = 0.6f) else SurfaceBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CreditCard,
                        contentDescription = null,
                        tint = if (isMaster) EmeraldLight else TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMaster) "LLAVE MAESTRA" else "LLAVE DE RESPALDO",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMaster) EmeraldLight else TextSecondary
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Eliminar",
                            tint = AlertRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Nfc,
                        contentDescription = null,
                        tint = PureWhite.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = maskedUid,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite,
                letterSpacing = 2.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cardLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceDark.copy(alpha = 0.7f))
                        .border(1.dp, EmeraldDark, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = profileTitle,
                        fontSize = 11.sp,
                        color = EmeraldLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
