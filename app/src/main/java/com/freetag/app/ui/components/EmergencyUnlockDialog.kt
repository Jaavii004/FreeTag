package com.freetag.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.freetag.app.ui.theme.AlertRed
import com.freetag.app.ui.theme.EmeraldPrimary
import com.freetag.app.ui.theme.ObsidianBg
import com.freetag.app.ui.theme.PureWhite
import com.freetag.app.ui.theme.SurfaceBorder
import com.freetag.app.ui.theme.SurfaceDark
import com.freetag.app.ui.theme.SurfaceElevated
import com.freetag.app.ui.theme.TextPrimary
import com.freetag.app.ui.theme.TextSecondary
import com.freetag.app.ui.theme.WarningAmber
import kotlinx.coroutines.delay

private const val TARGET_SECONDS = 60

/**
 * Emergency Unlock with intentional psychological friction.
 * Requires pressing and holding for 60 seconds without letting go.
 */
@Composable
fun EmergencyUnlockDialog(
    remainingUnlocks: Int,
    isStrictMode: Boolean = false,
    onDismissRequest: () -> Unit,
    onUnlocked: () -> Unit
) {
    var isPressing by remember { mutableStateOf(false) }
    var secondsHeld by remember { mutableIntStateOf(0) }

    LaunchedEffect(isPressing) {
        if (isPressing) {
            while (secondsHeld < TARGET_SECONDS && isPressing) {
                delay(1000L)
                if (isPressing) {
                    secondsHeld += 1
                }
            }
            if (secondsHeld >= TARGET_SECONDS) {
                onUnlocked()
            }
        } else {
            // Reset if user released their finger
            secondsHeld = 0
        }
    }

    val progress = (secondsHeld.toFloat() / TARGET_SECONDS.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "hold_progress"
    )

    Dialog(
        onDismissRequest = {
            if (!isPressing) onDismissRequest()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isPressing,
            dismissOnClickOutside = !isPressing
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isStrictMode) Icons.Rounded.Lock else Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = if (isStrictMode) AlertRed else WarningAmber,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isStrictMode) "Modo Estricto Activo" else "Desbloqueo de Emergencia",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isStrictMode) {
                    Text(
                        text = "El modo estricto está habilitado. Los desbloqueos de emergencia están completamente desactivados para asegurar máximo compromiso.\n\nSolo tu tarjeta física NFC puede desbloquear el dispositivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    Text(
                        text = "Para romper el bloqueo sin tu tarjeta física, debes mantener pulsado el botón durante 60 segundos ininterrumpidos para frenar el impulso.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remaining attempts badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (remainingUnlocks > 1) SurfaceElevated else AlertRed.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Intentos restantes: $remainingUnlocks de 3",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (remainingUnlocks > 1) WarningAmber else AlertRed,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    if (remainingUnlocks <= 0) {
                        Text(
                            text = "Has agotado los desbloqueos de emergencia.\nSolo puedes desbloquear con tu tarjeta NFC.",
                            color = AlertRed,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Hold Button Container with Progress Ring
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isPressing = true
                                            tryAwaitRelease()
                                            isPressing = false
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(150.dp),
                                color = EmeraldPrimary,
                                trackColor = SurfaceElevated,
                                strokeWidth = 6.dp,
                                strokeCap = StrokeCap.Round
                            )

                            // Central Hold Zone
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(if (isPressing) ObsidianBg else SurfaceElevated)
                                    .border(
                                        width = 1.dp,
                                        color = if (isPressing) EmeraldPrimary else SurfaceBorder,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LockOpen,
                                        contentDescription = null,
                                        tint = if (isPressing) EmeraldPrimary else TextSecondary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isPressing) "${TARGET_SECONDS - secondsHeld}s" else "MANTÉN",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isPressing) EmeraldPrimary else TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isPressing) {
                                "Mantén firme... ¡no sueltes!"
                            } else {
                                "Coloca y mantén tu dedo en el círculo"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isPressing) EmeraldPrimary else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismissRequest,
                    enabled = !isPressing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = PureWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Cerrar",
                        color = PureWhite,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
