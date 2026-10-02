package com.nextstepai.paperless.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalPaperlessSemanticColors = staticCompositionLocalOf { LightSemanticColors }

@Composable
fun PaperlessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = if (dynamicColor) {
        dynamicPaperlessColorScheme(darkTheme)
    } else if (darkTheme) {
        PaperlessDarkColors
    } else {
        PaperlessLightColors
    }

    val semantic = if (darkTheme) DarkSemanticColors else LightSemanticColors

    CompositionLocalProvider(LocalPaperlessSemanticColors provides semantic) {
        MaterialTheme(
            colorScheme = colors,
            typography = PaperlessTypography,
            shapes = PaperlessShapes,
            content = content,
        )
    }
}
