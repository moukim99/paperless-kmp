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

// Terracotta / Warm Editorial Paper Tokens (M3 Expressive)
val TerracottaPrimary = Color(0xFF9F3C16)
val TerracottaOnPrimary = Color(0xFFFFFFFF)
val TerracottaPrimaryContainer = Color(0xFFBF542C)
val TerracottaOnPrimaryContainer = Color(0xFFFFFBFF)

val TerracottaSecondary = Color(0xFF58624C)
val TerracottaOnSecondary = Color(0xFFFFFFFF)
val TerracottaSecondaryContainer = Color(0xFFD9E4C8)
val TerracottaOnSecondaryContainer = Color(0xFF5C6650)
val OnSecondaryFixed = Color(0xFF161E0D)

val AmberTertiary = Color(0xFF7F5300)
val TerracottaTertiaryContainer = Color(0xFF9E6A0D)
val TerracottaOnTertiaryContainer = Color(0xFFFFFBFF)

val SurfaceBackground = Color(0xFFFFF8F6)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFFFF1EC)
val SurfaceContainer = Color(0xFFFDEAE3)
val SurfaceContainerHigh = Color(0xFFF7E4DD)
val SurfaceContainerHighest = Color(0xFFF2DFD7)

val OnSurface = Color(0xFF231915)
val OnSurfaceVariant = Color(0xFF57423B)
val OutlineVariant = Color(0xFFDEC0B7)

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
