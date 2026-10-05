package com.nextstepai.paperless.ui.settings

enum class AppLanguage(val code: String, val displayName: String) {
    SYSTEM("system", "System Default"),
    ENGLISH("en", "English"),
    ARABIC("ar", "العربية"),
    FRENCH("fr", "Français");
}

enum class SyncInterval(val label: String) {
    MINUTES_15("15m"),
    MINUTES_30("30m"),
    HOURS_1("1h"),
    MANUAL("Manual")
}

sealed interface ConnectionStatus {
    data object Idle : ConnectionStatus
    data object Testing : ConnectionStatus
    data class Success(val latencyMs: Long) : ConnectionStatus
    data class Error(val message: String) : ConnectionStatus
}

data class SettingsUiState(
    val serverUrl: String = "https://sync.docvault.app/v1",
    val authToken: String = "dv_sec_live_9204bf893e1a0b77c",
    val isTokenVisible: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Idle,
    val syncOnWifiOnly: Boolean = true,
    val syncInterval: SyncInterval = SyncInterval.HOURS_1,
    val selectedLanguage: AppLanguage = AppLanguage.SYSTEM,
    val cacheUsedMb: Float = 48.2f,
    val cacheMaxMb: Float = 500f,
    val isConfirmingClearCache: Boolean = false,
    val installedLanguages: List<String> = listOf("English (US)", "Arabic (Standard)", "French"),
    val offlineOcrEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false
)

sealed interface SettingsUiIntent {
    data class UpdateServerUrl(val url: String) : SettingsUiIntent
    data class UpdateAuthToken(val token: String) : SettingsUiIntent
    data object ToggleTokenVisibility : SettingsUiIntent
    data object TestConnection : SettingsUiIntent
    data object SaveSettings : SettingsUiIntent
    data class ToggleSyncOnWifi(val enabled: Boolean) : SettingsUiIntent
    data class SelectSyncInterval(val interval: SyncInterval) : SettingsUiIntent
    data class SelectLanguage(val language: AppLanguage) : SettingsUiIntent
    data object RequestClearCache : SettingsUiIntent
    data object ConfirmClearCache : SettingsUiIntent
    data class ToggleOfflineOcr(val enabled: Boolean) : SettingsUiIntent
}
