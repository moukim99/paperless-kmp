package com.nextstepai.paperless.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val PaperlessLightColors = lightColorScheme(
    primary = Color(0xFF3559A8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF001944),
    secondary = Color(0xFF5A5F71),
    secondaryContainer = Color(0xFFDEE2F9),
    tertiary = Color(0xFF76546F),
    tertiaryContainer = Color(0xFFFFD8F3),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
)

val PaperlessDarkColors = darkColorScheme(
    primary = Color(0xFFB0C6FF),
    onPrimary = Color(0xFF002D6C),
    primaryContainer = Color(0xFF1B438F),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = Color(0xFFC2C6DD),
    secondaryContainer = Color(0xFF424659),
    tertiary = Color(0xFFE6BADB),
    tertiaryContainer = Color(0xFF5B3D55),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
)

data class PaperlessSemanticColors(
    val success: Color,
    val warning: Color,
    val critical: Color,
)

val LightSemanticColors = PaperlessSemanticColors(
    success = Color(0xFF176B3A),
    warning = Color(0xFF8A5A00),
    critical = Color(0xFFBA1A1A),
)

val DarkSemanticColors = PaperlessSemanticColors(
    success = Color(0xFF72D69B),
    warning = Color(0xFFFFC66D),
    critical = Color(0xFFFFB4AB),
)
