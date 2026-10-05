package com.nextstepai.paperless.ui.catalog.tag

import androidx.compose.ui.graphics.Color

val CuratedTagPalette = listOf(
    Color(0xFFC85A32), // Terracotta Primary
    Color(0xFFD97746), // Burnt Amber
    Color(0xFFE3A054), // Warm Honey Ochre
    Color(0xFF7A9A6B), // Editorial Sage
    Color(0xFF4E7D82), // Deep Muted Teal
    Color(0xFF627797), // Vellum Slate Blue
    Color(0xFFB86D79), // Dusty Rose
    Color(0xFF8C6B5B)  // Sepia Brown
)

data class AddTagUiState(
    val tagName: String = "Property Deeds",
    val selectedColor: Color = CuratedTagPalette.first(),
    val hexCode: String = "#C85A32",
    val isInboxTag: Boolean = false,
    val parentTagId: String? = null,
    val parentTagName: String = "None (Top Level)",
    val isAutoMatchingExpanded: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddTagIntent {
    data class TagNameChanged(val name: String) : AddTagIntent
    data class ColorSelected(val color: Color) : AddTagIntent
    data class HexCodeChanged(val hex: String) : AddTagIntent
    data class ToggleInboxTag(val isInbox: Boolean) : AddTagIntent
    data object SelectParentTagClicked : AddTagIntent
    data object ToggleAutoMatchingExpanded : AddTagIntent
    data object Dismiss : AddTagIntent
    data object SaveTag : AddTagIntent
}
