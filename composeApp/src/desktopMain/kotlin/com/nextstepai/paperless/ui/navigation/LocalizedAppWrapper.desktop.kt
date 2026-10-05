package com.nextstepai.paperless.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.nextstepai.paperless.ui.settings.AppLanguage
import java.util.Locale

@Composable
actual fun LocalizedAppWrapper(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    val locale = when (language) {
        AppLanguage.ARABIC -> Locale("ar")
        AppLanguage.FRENCH -> Locale("fr")
        AppLanguage.ENGLISH -> Locale("en")
        AppLanguage.SYSTEM -> Locale.getDefault()
    }

    Locale.setDefault(locale)

    val layoutDirection = when (language) {
        AppLanguage.ARABIC -> LayoutDirection.Rtl
        AppLanguage.ENGLISH, AppLanguage.FRENCH -> LayoutDirection.Ltr
        AppLanguage.SYSTEM -> LocalLayoutDirection.current
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection
    ) {
        content()
    }
}
