package com.nextstepai.paperless.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun dynamicPaperlessColorScheme(darkTheme: Boolean): ColorScheme
