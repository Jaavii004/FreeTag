package com.freetag.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.freetag.app.ui.theme.EmeraldDark
import com.freetag.app.ui.theme.EmeraldLight
import com.freetag.app.ui.theme.EmeraldPrimary
import com.freetag.app.ui.theme.SurfaceDark

/**
 * High-tech minimalist radar pulse animation indicating that NFC reader is active.
 */
@Composable
fun NfcRadarAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    isActive: Boolean = true,
    centerIconColor: Color = EmeraldLight
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_radar_anim")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, delayMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, delayMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse3"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val maxRadius = size.toPx() / 2f

                val pulses = listOf(pulse1, pulse2, pulse3)
                for (p in pulses) {
                    val radius = maxRadius * (0.35f + (p * 0.65f))
                    val alpha = (1f - p).coerceIn(0f, 1f) * 0.5f
                    val strokeWidth = 3f * (1f - (p * 0.5f))

                    drawCircle(
                        color = EmeraldPrimary.copy(alpha = alpha),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                }
            }
        }

        // Central NFC Orb
        Box(
            modifier = Modifier
                .size(size * 0.42f)
                .clip(CircleShape)
                .background(SurfaceDark)
                .border(
                    width = 2.dp,
                    color = if (isActive) EmeraldPrimary else EmeraldDark,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Nfc,
                contentDescription = "Sensor NFC Activo",
                tint = centerIconColor,
                modifier = Modifier.size(size * 0.22f)
            )
        }
    }
}
