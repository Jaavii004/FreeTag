package com.freetag.app.ui.screens

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import com.freetag.app.ui.components.BoxBreathingDialog
import com.freetag.app.ui.components.EmergencyUnlockDialog
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
import com.freetag.app.ui.theme.WarningAmber

private val STOIC_REFLECTIONS = listOf(
    "«Tu atención es tu activo más valioso. No la regales por impulso.»",
    "«La disciplina es elegir entre lo que quieres ahora y lo que más quieres.»",
    "«El impulso por distraerte dura apenas 90 segundos. Respira y mantente presente.»",
    "«¿Realmente necesitas abrir esto ahora mismo o es solo un reflejo automático?»",
    "«Quien vence sus deseos es más fuerte que quien vence a sus enemigos.» — Aristóteles"
)

@Composable
fun LockOverlayScreen(
    blockedAppName: String,
    blockedAppIcon: Drawable?,
    boundCardMasked: String?,
    remainingEmergencyUnlocks: Int,
    interceptionsToday: Int,
    isStrictMode: Boolean,
    hasRecoveryPin: Boolean,
    mindfulDelaySeconds: Int = 0,
    errorMessage: String?,
    onGoHome: () -> Unit,
    onEmergencyUnlocked: () -> Unit,
    onActivateTemporaryPass: () -> Unit,
    onVerifyRecoveryPin: (String) -> Boolean
) {
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showBreathingDialog by remember { mutableStateOf(false) }
    var showPaperRecoveryDialog by remember { mutableStateOf(false) }

    var delayRemaining by remember(mindfulDelaySeconds) {
        mutableIntStateOf(mindfulDelaySeconds)
    }

    LaunchedEffect(mindfulDelaySeconds) {
        if (mindfulDelaySeconds > 0) {
            while (delayRemaining > 0) {
                delay(1000L)
                delayRemaining -= 1
            }
        }
    }

    val randomReflection = remember {
        STOIC_REFLECTIONS.random()
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

    if (showBreathingDialog) {
        BoxBreathingDialog(
            onDismissRequest = { showBreathingDialog = false },
            onRecoveredControl = {
                showBreathingDialog = false
                onGoHome()
            },
            onProceedToUnlock = { showBreathingDialog = false }
        )
    }

    if (showPaperRecoveryDialog) {
        PaperRecoveryDialog(
            isSetupMode = false,
            onDismissRequest = { showPaperRecoveryDialog = false },
            onPinVerifiedOrSaved = { pin ->
                val ok = onVerifyRecoveryPin(pin)
                if (ok) {
                    showPaperRecoveryDialog = false
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Blocked App Icon + Lock Badge
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark)
                        .border(1.5.dp, SurfaceBorder, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (blockedAppIcon != null) {
                        AndroidView(
                            factory = { ctx ->
                                ImageView(ctx).apply {
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    setImageDrawable(blockedAppIcon)
                                }
                            },
                            modifier = Modifier.size(56.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(AlertRed)
                        .border(2.dp, ObsidianBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$blockedAppName está Bloqueada",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (interceptionsToday > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Llevas $interceptionsToday tentaciones evitadas hoy",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Reflection Quote Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = randomReflection,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    color = EmeraldLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Box Breathing Quick Button
            Button(
                onClick = { showBreathingDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceElevated,
                    contentColor = EmeraldLight
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Rounded.Spa, contentDescription = null, modifier = Modifier.size(18.dp), tint = EmeraldLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Fricción Consciente: Respirar 12s", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // NFC Radar Animation
            NfcRadarAnimation(
                size = 170.dp,
                isActive = true,
                centerIconColor = EmeraldPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Acerca tu Llave Física NFC",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pega tu tarjeta contactless (${boundCardMasked ?: "Llave"}) al móvil para desbloquear.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (!errorMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AlertRed.copy(alpha = 0.2f))
                        .border(1.dp, AlertRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = AlertRed,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Go to Home Button
            Button(
                onClick = onGoHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceElevated,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null,
                    tint = EmeraldLight,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Volver a la Pantalla de Inicio",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5-Minute Micro-Pass & Emergency Options Row
            if (delayRemaining > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏳ Pausa consciente activa: espera ${delayRemaining}s...",
                        fontSize = 12.sp,
                        color = EmeraldLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onActivateTemporaryPass,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceElevated,
                            contentColor = EmeraldLight
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldDark)
                    ) {
                        Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldLight)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Pase 5 min", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EmeraldLight)
                    }

                    if (hasRecoveryPin) {
                        Button(
                            onClick = { showPaperRecoveryDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceElevated,
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Icon(Icons.Rounded.Password, contentDescription = null, modifier = Modifier.size(16.dp), tint = PureWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "PIN de Papel", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PureWhite)
                        }
                    } else {
                        Button(
                            onClick = { showEmergencyDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceElevated,
                                contentColor = if (isStrictMode) TextSecondary else WarningAmber
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isStrictMode) SurfaceBorder else WarningAmber.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isStrictMode) TextSecondary else WarningAmber
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isStrictMode) "Estricto" else "Emergencia",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isStrictMode) TextSecondary else WarningAmber
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
