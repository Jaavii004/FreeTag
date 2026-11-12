package com.freetag.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.freetag.app.ui.theme.EmeraldDark
import com.freetag.app.ui.theme.EmeraldLight
import com.freetag.app.ui.theme.EmeraldPrimary
import com.freetag.app.ui.theme.PureWhite
import com.freetag.app.ui.theme.SurfaceBorder
import com.freetag.app.ui.theme.SurfaceDark
import com.freetag.app.ui.theme.SurfaceElevated
import com.freetag.app.ui.theme.TextPrimary
import com.freetag.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun BoxBreathingDialog(
    onDismissRequest: () -> Unit,
    onRecoveredControl: () -> Unit,
    onProceedToUnlock: () -> Unit
) {
    var phaseSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            phaseSeconds = (phaseSeconds + 1) % 12
        }
    }

    val phaseText = when (phaseSeconds) {
        in 0..3 -> "Inhala profundamente..."
        in 4..7 -> "Mantén el aire con calma..."
        else -> "Exhala despacio y relaja..."
    }

    val phaseSubtext = when (phaseSeconds) {
        in 0..3 -> "Llena tus pulmones de oxígeno"
        in 4..7 -> "Observa el impulso sin juzgarlo"
        else -> "Suelta la necesidad de estimulación"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Spa,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fricción Consciente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "El 80% de aperturas de apps son un tic reflejo. Respira 12 segundos antes de decidir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Pulsing Breathing Circle
                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        EmeraldPrimary.copy(alpha = 0.35f),
                                        EmeraldDark.copy(alpha = 0.1f)
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .border(1.5.dp, EmeraldLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Spa,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(phaseSeconds % 4) + 1}s",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = phaseText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite
                )

                Text(
                    text = phaseSubtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Choice: I regained control
                Button(
                    onClick = onRecoveredControl,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = PureWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "He recuperado el control", fontWeight = FontWeight.Bold, color = PureWhite)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onProceedToUnlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = PureWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(text = "Aún necesito usar la tarjeta NFC", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

