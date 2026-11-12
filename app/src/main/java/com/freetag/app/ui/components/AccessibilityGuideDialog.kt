package com.freetag.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.freetag.app.ui.theme.EmeraldDark
import com.freetag.app.ui.theme.EmeraldLight
import com.freetag.app.ui.theme.EmeraldPrimary
import com.freetag.app.ui.theme.PureWhite
import com.freetag.app.ui.theme.SurfaceBorder
import com.freetag.app.ui.theme.SurfaceDark
import com.freetag.app.ui.theme.SurfaceElevated
import com.freetag.app.ui.theme.TextPrimary
import com.freetag.app.ui.theme.TextSecondary
import com.freetag.app.ui.theme.WarningAmber

@Composable
fun AccessibilityGuideDialog(
    onDismissRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EmeraldDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Cómo Activar la Accesibilidad",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "FreeTag necesita este permiso únicamente para detectar en tiempo real cuándo se abre una app que hayas decidido bloquear.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Step 1
                GuideStepItem(
                    stepNumber = "1",
                    title = "Abre Accesibilidad",
                    description = "Pulsa el botón de abajo para ir a Ajustes de Accesibilidad de tu móvil."
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Step 2
                GuideStepItem(
                    stepNumber = "2",
                    title = "Busca 'FreeTag' y Actívalo",
                    description = "Entra en 'Apps instaladas' o 'Servicios descargados', selecciona 'FreeTag' y activa el interruptor."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Android 13/14 notice banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(WarningAmber.copy(alpha = 0.12f))
                        .border(1.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "¿Aparece 'Ajuste restringido'?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "En Android 13 y 14:\n1. Ve a Ajustes > Aplicaciones > FreeTag.\n2. Toca los 3 puntos (⋮) arriba a la derecha.\n3. Selecciona 'Permitir ajustes restringidos' y confirma con tu huella/PIN.\n4. ¡Listo! Vuelve a Accesibilidad y ya podrás activarlo.",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onDismissRequest()
                        onOpenSettings()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Ir a Ajustes de Accesibilidad",
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = PureWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Cerrar Guía", color = PureWhite, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun GuideStepItem(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(EmeraldDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = EmeraldLight,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PureWhite
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

