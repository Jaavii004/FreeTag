package com.freetag.app.data

import android.graphics.drawable.Drawable

enum class AppCategory(val title: String) {
    ALL("Todas"),
    SOCIAL("Redes Sociales"),
    ENTERTAINMENT("Vídeo y Streaming"),
    GAMES("Juegos"),
    PRODUCTIVITY("Trabajo"),
    OTHER("Otras")
}

/**
 * Represents an installed Android application selectable for blocking.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean = false,
    val isBlocked: Boolean = false,
    val category: AppCategory = AppCategory.OTHER
)
