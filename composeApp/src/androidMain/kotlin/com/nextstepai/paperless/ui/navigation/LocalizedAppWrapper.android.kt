package com.nextstepai.paperless.ui.navigation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.nextstepai.paperless.ui.settings.AppLanguage
import java.util.Locale

@Composable
actual fun LocalizedAppWrapper(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentConfig = LocalConfiguration.current

    val locale = when (language) {
        AppLanguage.ARABIC -> Locale("ar")
        AppLanguage.FRENCH -> Locale("fr")
        AppLanguage.ENGLISH -> Locale("en")
        AppLanguage.SYSTEM -> Locale.getDefault()
    }

    Locale.setDefault(locale)

    val updatedConfig = remember(language, currentConfig) {
        val config = Configuration(currentConfig)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        config
    }

    val layoutDirection = when (language) {
        AppLanguage.ARABIC -> LayoutDirection.Rtl
        AppLanguage.ENGLISH, AppLanguage.FRENCH -> LayoutDirection.Ltr
        AppLanguage.SYSTEM -> LocalLayoutDirection.current
    }

    CompositionLocalProvider(
        LocalConfiguration provides updatedConfig,
        LocalLayoutDirection provides layoutDirection
    ) {
        content()
    }
}
