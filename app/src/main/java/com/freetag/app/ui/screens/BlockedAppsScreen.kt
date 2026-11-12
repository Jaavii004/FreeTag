package com.freetag.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freetag.app.data.AppCategory
import com.freetag.app.data.AppInfo
import com.freetag.app.ui.components.AppItemRow
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

enum class FilterMode(val label: String) {
    ALL("Todas"),
    BLOCKED("Bloqueadas"),
    SOCIAL("Redes Sociales"),
    ENTERTAINMENT("Vídeo"),
    GAMES("Juegos")
}

@Composable
fun BlockedAppsScreen(
    isLocked: Boolean,
    isBunkerMode: Boolean = false,
    installedApps: List<AppInfo>,
    isLoading: Boolean,
    onToggleApp: (String) -> Unit,
    onBlockSocialMediaOnly: () -> Unit,
    onClearAllBlocked: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FilterMode.ALL) }

    val filteredApps = remember(installedApps, searchQuery, selectedFilter) {
        installedApps.filter { app ->
            val matchesQuery = app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                FilterMode.ALL -> true
                FilterMode.BLOCKED -> app.isBlocked
                FilterMode.SOCIAL -> app.category == AppCategory.SOCIAL
                FilterMode.ENTERTAINMENT -> app.category == AppCategory.ENTERTAINMENT
                FilterMode.GAMES -> app.category == AppCategory.GAMES
            }
            matchesQuery && matchesFilter
        }
    }

    val blockedCount = remember(installedApps) {
        installedApps.count { it.isBlocked }
    }
    val socialCount = remember(installedApps) {
        installedApps.count { it.category == AppCategory.SOCIAL }
    }
    val entertainmentCount = remember(installedApps) {
        installedApps.count { it.category == AppCategory.ENTERTAINMENT }
    }
    val gamesCount = remember(installedApps) {
        installedApps.count { it.category == AppCategory.GAMES }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isBunkerMode) "Modo Búnker: Apps Permitidas" else "Apps a Bloquear",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = if (isBunkerMode) {
                        "$blockedCount apps autorizadas (todo lo demás estará bloqueado)"
                    } else {
                        "$blockedCount de ${installedApps.size} apps seleccionadas"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isBunkerMode) EmeraldLight else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLocked) {
            // Anti-tamper banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AlertRed.copy(alpha = 0.15f))
                    .border(1.dp, AlertRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Modo Presencia Activo. La lista está sellada contra impulsos hasta que acerques tu llave NFC.",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // Quick preset action row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onBlockSocialMediaOnly,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = EmeraldLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldDark)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = EmeraldLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Bloquear Redes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EmeraldLight)
                }

                if (blockedCount > 0) {
                    Button(
                        onClick = onClearAllBlocked,
                        modifier = Modifier.height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = SurfaceElevated,
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text(text = "Desmarcar todo", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PureWhite)
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar aplicación...", color = TextSecondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = TextSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Limpiar",
                            tint = TextSecondary
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = SurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterMode.values().forEach { mode ->
                val isSelected = selectedFilter == mode
                val count = when (mode) {
                    FilterMode.ALL -> installedApps.size
                    FilterMode.BLOCKED -> blockedCount
                    FilterMode.SOCIAL -> socialCount
                    FilterMode.ENTERTAINMENT -> entertainmentCount
                    FilterMode.GAMES -> gamesCount
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = mode },
                    label = {
                        Text(
                            text = "${mode.label} ($count)",
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

        Spacer(modifier = Modifier.height(12.dp))

        // Application List
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Escaneando aplicaciones instaladas...",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "No se encontraron coincidencias para \"$searchQuery\"" else "No hay aplicaciones en esta categoría",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredApps,
                    key = { it.packageName }
                ) { app ->
                    AppItemRow(
                        app = app,
                        isAppLockedMode = isLocked,
                        onToggle = onToggleApp
                    )
                }
            }
        }
    }
}
