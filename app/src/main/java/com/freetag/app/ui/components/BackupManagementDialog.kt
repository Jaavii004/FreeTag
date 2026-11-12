package com.freetag.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.freetag.app.ui.theme.AlertRed
import com.freetag.app.ui.theme.EmeraldDark
import com.freetag.app.ui.theme.EmeraldLight
import com.freetag.app.ui.theme.EmeraldPrimary
import com.freetag.app.ui.theme.PureWhite
import com.freetag.app.ui.theme.SurfaceBorder
import com.freetag.app.ui.theme.SurfaceDark
import com.freetag.app.ui.theme.SurfaceElevated
import com.freetag.app.ui.theme.TextPrimary
import com.freetag.app.ui.theme.TextSecondary

@Composable
fun BackupManagementDialog(
    exportedJson: String,
    onDismissRequest: () -> Unit,
    onImportJson: (String) -> Boolean
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var importInputText by remember { mutableStateOf("") }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }

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
                        .size(46.dp)
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Copia de Seguridad Offline",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Guarda o restaura tus llaves físicas y apps sin conexión a internet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs: Exportar / Importar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceElevated,
                    contentColor = EmeraldLight,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = EmeraldPrimary
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Exportar JSON",
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedTab == 0) EmeraldLight else TextSecondary
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Rounded.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 0) EmeraldLight else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Restaurar JSON",
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedTab == 1) EmeraldLight else TextSecondary
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Rounded.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 1) EmeraldLight else TextSecondary
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Export Tab
                    Text(
                        text = "Tu configuración encriptada localmente lista para respaldar:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = exportedJson,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = EmeraldLight
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("FreeTag Backup", exportedJson)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "¡Copia de seguridad copiada al portapapeles!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp), tint = PureWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copiar al Portapapeles", fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                } else {
                    // Import Tab
                    Text(
                        text = "Pega aquí el texto JSON de tu copia de seguridad anterior:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = {
                            importInputText = it
                            importErrorMessage = null
                        },
                        placeholder = { Text("Pega aquí el código JSON...", color = TextSecondary, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )

                    if (!importErrorMessage.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = importErrorMessage ?: "", color = AlertRed, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (importInputText.isBlank()) {
                                importErrorMessage = "El texto de la copia de seguridad está vacío."
                                return@Button
                            }
                            val success = onImportJson(importInputText.trim())
                            if (success) {
                                Toast.makeText(context, "¡Configuración restaurada con éxito!", Toast.LENGTH_SHORT).show()
                                onDismissRequest()
                            } else {
                                importErrorMessage = "El formato JSON es inválido o está dañado."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp), tint = PureWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restaurar Configuración", fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = PureWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Cerrar", color = PureWhite, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

