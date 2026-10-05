package com.nextstepai.paperless.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun dynamicPaperlessColorScheme(darkTheme: Boolean): ColorScheme {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
        else dynamicLightColorScheme(LocalContext.current)
    }
    return if (darkTheme) PaperlessDarkColors else PaperlessLightColors
}
