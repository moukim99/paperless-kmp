package com.nextstepai.paperless.ui.navigation

enum class AppDestination(
    val label: String,
    val symbol: String,
) {
    Documents("Documents", "▤"),
    Inbox("Inbox", "↓"),
    Expiring("Expiring", "◷"),
    Tags("Tags", "#"),
    Settings("Settings", "⚙"),
}
