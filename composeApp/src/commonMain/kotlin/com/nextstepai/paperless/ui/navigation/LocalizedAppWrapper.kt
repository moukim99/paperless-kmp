package com.nextstepai.paperless.ui.navigation

import androidx.compose.runtime.Composable
import com.nextstepai.paperless.ui.settings.AppLanguage

@Composable
expect fun LocalizedAppWrapper(
    language: AppLanguage,
    content: @Composable () -> Unit
)
