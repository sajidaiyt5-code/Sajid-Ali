package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AlarmEntity
import com.example.data.db.AppDatabase
import com.example.data.db.CalendarEventEntity
import com.example.data.db.ContactEntity
import com.example.data.db.GalleryItemEntity
import com.example.data.db.NoteEntity
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.SandboxFileItem
import com.example.data.repository.VirtualPhoneRepository
import com.example.data.security.BiometricCapability
import com.example.data.security.SecurityManager
import com.example.data.system.DeviceTelemetryManager
import com.example.data.system.NetworkDiagnosticResult
import com.example.data.system.RealBatteryInfo
import com.example.data.system.RealDeviceLocationInfo
import com.example.data.system.RealNetworkInfo
import com.example.data.system.RealStorageInfo
import com.example.model.DateFormatStyle
import com.example.model.TemperatureUnit
import com.example.model.ThemeMode
import com.example.model.UkCity
import com.example.model.VirtualAppId
import com.example.model.VirtualNotification
import com.example.model.VirtualPhoneSettings
import com.example.model.WallpaperStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

data class ShellUiState(
    val activeApp: VirtualAppId? = null,
    val recentApps: List<VirtualAppId> = emptyList(),
    val isAppDrawerOpen: Boolean = false,
    val isShadeOpen: Boolean = false,
    val isRecentsOpen: Boolean = false,
    val isLocked: Boolean = false,
    val isHomeCustomizeMode: Boolean = false,
    val nowLondon: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London")),
    val notifications: List<VirtualNotification> = defaultNotifications(),
    val batteryInfo: RealBatteryInfo = RealBatteryInfo(100, false, "Reading", null),
    val storageInfo: RealStorageInfo = RealStorageInfo(64.0, 48.0, 16.0, 0.25f),
    val networkInfo: RealNetworkInfo = RealNetworkInfo(false, "Checking", false, false, "none", emptyList(), 0, 0),
    val deviceLocationInfo: RealDeviceLocationInfo = RealDeviceLocationInfo(false, null, null, null, null, "Not queried"),
    val pendingDialNumber: String? = null,
    val pendingSmsPeer: Pair<String, String>? = null,
    val statusToastMessage: String? = null
)

private fun defaultNotifications(): List<VirtualNotification> = listOf(
    VirtualNotification(
        id = "uk_env_ready",
        title = "UK Regional Profile Active",
        body = "Country: United Kingdom • Time Zone: Europe/London • Currency: GBP (£)",
        sourceApp = VirtualAppId.SETTINGS
    ),
    VirtualNotification(
        id = "uk_location_note",
        title = "Virtual Location Ready",
        body = "Selected region: London (+44 020). Tap to configure UK cities.",
        sourceApp = VirtualAppId.MAPS
    )
)

class VirtualPhoneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val settingsStore = SettingsDataStore(application)
    val securityManager = SecurityManager(application)
    val telemetryManager = DeviceTelemetryManager(application)
    val repository = VirtualPhoneRepository(application, db, settingsStore, securityManager)

    val settings: StateFlow<VirtualPhoneSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VirtualPhoneSettings())

    val contacts = repository.contactsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val callLogs = repository.callLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val smsMessages = repository.smsMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notes = repository.notesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val calendarEvents = repository.calendarEventsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val alarms = repository.alarmsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val galleryItems = repository.galleryItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _shellState = MutableStateFlow(ShellUiState())
    val shellState: StateFlow<ShellUiState> = _shellState.asStateFlow()

    private val _sandboxFiles = MutableStateFlow<List<SandboxFileItem>>(emptyList())
    val sandboxFiles: StateFlow<List<SandboxFileItem>> = _sandboxFiles.asStateFlow()

    private val _currentSandboxDir = MutableStateFlow(repository.sandboxRootDir)
    val currentSandboxDir: StateFlow<File> = _currentSandboxDir.asStateFlow()

    private val _networkProbeResult = MutableStateFlow<NetworkDiagnosticResult?>(null)
    val networkProbeResult: StateFlow<NetworkDiagnosticResult?> = _networkProbeResult.asStateFlow()

    private val _isProbingNetwork = MutableStateFlow(false)
    val isProbingNetwork: StateFlow<Boolean> = _isProbingNetwork.asStateFlow()

    init {
        viewModelScope.launch {
            val initialSettings = repository.settingsFlow.first()
            val shouldLock = initialSettings.pinLockEnabled && securityManager.hasPinConfigured()
            _shellState.update { it.copy(isLocked = shouldLock) }
            refreshTelemetry()
            refreshSandboxFiles(repository.sandboxRootDir)
        }
        viewModelScope.launch {
            while (true) {
                val zoneId = runCatching { ZoneId.of(settings.value.timeZoneId) }
                    .getOrDefault(ZoneId.of("Europe/London"))
                _shellState.update {
                    it.copy(nowLondon = ZonedDateTime.now(zoneId))
                }
                delay(1_000L)
            }
        }
        viewModelScope.launch {
            while (true) {
                refreshTelemetry()
                delay(15_000L)
            }
        }
    }

    fun refreshTelemetry() {
        val battery = telemetryManager.getRealBatteryInfo()
        val storage = telemetryManager.getRealStorageInfo()
        val network = telemetryManager.getRealNetworkInfo()
        val location = telemetryManager.getRealDeviceLocation()
        _shellState.update {
            it.copy(
                batteryInfo = battery,
                storageInfo = storage,
                networkInfo = network,
                deviceLocationInfo = location
            )
        }
    }

    // Shell Navigation & Controls
    fun openApp(appId: VirtualAppId) {
        if (appId in settings.value.disabledApps) {
            postToast("${appId.title} module is disabled in Settings")
            return
        }
        _shellState.update { state ->
            val updatedRecents = (listOf(appId) + state.recentApps.filter { it != appId }).take(8)
            state.copy(
                activeApp = appId,
                recentApps = updatedRecents,
                isAppDrawerOpen = false,
                isShadeOpen = false,
                isRecentsOpen = false,
                isHomeCustomizeMode = false
            )
        }
    }

    fun navigateHome() {
        _shellState.update {
            it.copy(
                activeApp = null,
                isAppDrawerOpen = false,
                isShadeOpen = false,
                isRecentsOpen = false,
                isHomeCustomizeMode = false
            )
        }
    }

    fun navigateBack(): Boolean {
        val state = _shellState.value
        return when {
            state.isShadeOpen -> {
                _shellState.update { it.copy(isShadeOpen = false) }
                true
            }
            state.isRecentsOpen -> {
                _shellState.update { it.copy(isRecentsOpen = false) }
                true
            }
            state.isAppDrawerOpen -> {
                _shellState.update { it.copy(isAppDrawerOpen = false) }
                true
            }
            state.isHomeCustomizeMode -> {
                _shellState.update { it.copy(isHomeCustomizeMode = false) }
                true
            }
            state.activeApp != null -> {
                _shellState.update { it.copy(activeApp = null) }
                true
            }
            else -> false
        }
    }

    fun toggleAppDrawer(open: Boolean? = null) {
        _shellState.update {
            val next = open ?: !it.isAppDrawerOpen
            it.copy(isAppDrawerOpen = next, isShadeOpen = false, isRecentsOpen = false)
        }
    }

    fun toggleShade(open: Boolean? = null) {
        if (open == true) refreshTelemetry()
        _shellState.update {
            val next = open ?: !it.isShadeOpen
            it.copy(isShadeOpen = next, isAppDrawerOpen = false, isRecentsOpen = false)
        }
    }

    fun toggleRecents(open: Boolean? = null) {
        _shellState.update {
            val next = open ?: !it.isRecentsOpen
            it.copy(isRecentsOpen = next, isShadeOpen = false, isAppDrawerOpen = false)
        }
    }

    fun closeRecentApp(appId: VirtualAppId) {
        _shellState.update { state ->
            val remaining = state.recentApps.filter { it != appId }
            val nextActive = if (state.activeApp == appId) null else state.activeApp
            state.copy(recentApps = remaining, activeApp = nextActive)
        }
    }

    fun clearAllRecents() {
        _shellState.update {
            it.copy(recentApps = emptyList(), activeApp = null, isRecentsOpen = false)
        }
    }

    fun setHomeCustomizeMode(enabled: Boolean) {
        _shellState.update { it.copy(isHomeCustomizeMode = enabled) }
    }

    fun pushNotification(title: String, body: String, source: VirtualAppId) {
        if (!settings.value.notificationsEnabled) return
        val note = VirtualNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            body = body,
            sourceApp = source
        )
        _shellState.update {
            it.copy(notifications = (listOf(note) + it.notifications).take(20))
        }
    }

    fun dismissNotification(id: String) {
        _shellState.update { state ->
            state.copy(notifications = state.notifications.filterNot { it.id == id })
        }
    }

    fun clearAllNotifications() {
        _shellState.update { it.copy(notifications = emptyList()) }
    }

    fun postToast(message: String) {
        _shellState.update { it.copy(statusToastMessage = message) }
    }

    fun clearToast() {
        _shellState.update { it.copy(statusToastMessage = null) }
    }

    // Cross-App Intents within Virtual OS
    fun dialNumberInPhoneApp(number: String) {
        _shellState.update { it.copy(pendingDialNumber = number) }
        openApp(VirtualAppId.PHONE)
    }

    fun consumePendingDialNumber() {
        _shellState.update { it.copy(pendingDialNumber = null) }
    }

    fun openSmsConversation(address: String, name: String) {
        _shellState.update { it.copy(pendingSmsPeer = address to name) }
        openApp(VirtualAppId.MESSAGES)
    }

    fun consumePendingSmsPeer() {
        _shellState.update { it.copy(pendingSmsPeer = null) }
    }

    // Security & Lock Screen
    fun lockDeviceNow() {
        if (settings.value.pinLockEnabled && securityManager.hasPinConfigured()) {
            _shellState.update { it.copy(isLocked = true, isShadeOpen = false) }
        } else {
            postToast("Set a 4-digit PIN in Settings > Security to lock")
        }
    }

    fun unlockWithPin(pin: String): Boolean {
        val ok = securityManager.verifyPin(pin)
        if (ok) {
            _shellState.update { it.copy(isLocked = false) }
        }
        return ok
    }

    fun unlockWithBiometricSuccess() {
        _shellState.update { it.copy(isLocked = false) }
    }

    fun getBiometricCapability(): BiometricCapability = securityManager.getBiometricCapability()

    fun setPinCode(pin: String) {
        viewModelScope.launch {
            if (securityManager.savePin(pin)) {
                repository.updateSettings { it.copy(pinLockEnabled = true) }
                postToast("PIN saved securely in Android Keystore vault")
            }
        }
    }

    fun disablePinLock() {
        viewModelScope.launch {
            securityManager.clearPinAndVault()
            repository.updateSettings { it.copy(pinLockEnabled = false, biometricEnabled = false) }
            postToast("App lock disabled")
        }
    }

    // UK Regional & Device Settings Mutations
    fun selectUkCity(city: UkCity) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(selectedCity = city) }
            pushNotification(
                "UK Location Updated",
                "Virtual location set to ${city.displayName} (${city.regionName}, ${city.postcodePrefix})",
                VirtualAppId.MAPS
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.updateSettings { it.copy(themeMode = mode) } }
    }

    fun setWallpaperStyle(style: WallpaperStyle) {
        viewModelScope.launch { repository.updateSettings { it.copy(wallpaperStyle = style) } }
    }

    fun updateDeviceName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            viewModelScope.launch { repository.updateSettings { it.copy(virtualDeviceName = trimmed) } }
        }
    }

    fun setUse24Hour(use24Hour: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.copy(use24HourClock = use24Hour) } }
    }

    fun setDateFormat(style: DateFormatStyle) {
        viewModelScope.launch { repository.updateSettings { it.copy(dateFormatStyle = style) } }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch { repository.updateSettings { it.copy(temperatureUnit = unit) } }
    }

    fun setRegionProfile(region: String, languageLabel: String) {
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(regionLabel = region, languageLabel = languageLabel)
            }
        }
    }

    fun togglePinnedHomeApp(appId: VirtualAppId) {
        viewModelScope.launch {
            repository.updateSettings { current ->
                val next = if (appId in current.pinnedHomeApps) {
                    current.pinnedHomeApps - appId
                } else {
                    current.pinnedHomeApps + appId
                }
                current.copy(pinnedHomeApps = next)
            }
        }
    }

    fun toggleAppEnabled(appId: VirtualAppId) {
        if (appId == VirtualAppId.SETTINGS) return // Never allow disabling Settings
        viewModelScope.launch {
            repository.updateSettings { current ->
                val next = if (appId in current.disabledApps) {
                    current.disabledApps - appId
                } else {
                    current.disabledApps + appId
                }
                current.copy(disabledApps = next)
            }
        }
    }

    fun updateSetting(transform: (VirtualPhoneSettings) -> VirtualPhoneSettings) {
        viewModelScope.launch { repository.updateSettings(transform) }
    }

    // Room Data Actions
    fun saveContact(contact: ContactEntity) = viewModelScope.launch { repository.saveContact(contact) }
    fun deleteContact(id: Int) = viewModelScope.launch { repository.deleteContact(id) }

    fun logCall(name: String, number: String, type: String, durationSec: Int = 0) =
        viewModelScope.launch { repository.logCall(name, number, type, durationSec) }

    fun deleteCallLog(id: Int) = viewModelScope.launch { repository.deleteCallLog(id) }
    fun clearCallLogs() = viewModelScope.launch { repository.clearCallLogs() }

    fun sendSms(peerAddress: String, peerName: String, body: String, deliveryNote: String) =
        viewModelScope.launch { repository.sendLocalSms(peerAddress, peerName, body, deliveryNote) }

    fun deleteSmsThread(peerAddress: String) = viewModelScope.launch { repository.deleteSmsThread(peerAddress) }

    fun saveNote(note: NoteEntity) = viewModelScope.launch { repository.saveNote(note) }
    fun deleteNote(id: Int) = viewModelScope.launch { repository.deleteNote(id) }

    fun saveCalendarEvent(event: CalendarEventEntity) = viewModelScope.launch { repository.saveCalendarEvent(event) }
    fun deleteCalendarEvent(id: Int) = viewModelScope.launch { repository.deleteCalendarEvent(id) }

    fun saveAlarm(alarm: AlarmEntity) = viewModelScope.launch { repository.saveAlarm(alarm) }
    fun deleteAlarm(id: Int) = viewModelScope.launch { repository.deleteAlarm(id) }

    fun addGalleryPhotoFromCamera(file: File) {
        viewModelScope.launch {
            repository.addGalleryItem(
                GalleryItemEntity(
                    title = file.nameWithoutExtension,
                    uriString = Uri.fromFile(file).toString(),
                    sourceType = "CAMERA"
                )
            )
            refreshSandboxFiles(_currentSandboxDir.value)
        }
    }

    fun importPhotoToGallery(uri: Uri, title: String) {
        viewModelScope.launch {
            val saved = repository.importPhotoToSandbox(uri, title)
            if (saved != null) {
                postToast("Photo imported to UK Virtual Gallery")
                refreshSandboxFiles(_currentSandboxDir.value)
            } else {
                postToast("Could not import selected photo")
            }
        }
    }

    fun deleteGalleryItem(item: GalleryItemEntity) = viewModelScope.launch {
        repository.deleteGalleryItem(item)
        refreshSandboxFiles(_currentSandboxDir.value)
    }

    // File Manager Sandbox Operations
    fun refreshSandboxFiles(dir: File = _currentSandboxDir.value) {
        viewModelScope.launch {
            _currentSandboxDir.value = dir
            _sandboxFiles.value = repository.listSandboxFiles(dir)
        }
    }

    fun navigateUpSandboxDir(): Boolean {
        val current = _currentSandboxDir.value
        val root = repository.sandboxRootDir
        if (current.canonicalPath == root.canonicalPath) return false
        val parent = current.parentFile ?: root
        refreshSandboxFiles(parent)
        return true
    }

    fun createFolder(name: String) = viewModelScope.launch {
        if (repository.createSandboxFolder(_currentSandboxDir.value, name)) {
            refreshSandboxFiles(_currentSandboxDir.value)
        }
    }

    fun createOrUpdateTextFile(name: String, content: String) = viewModelScope.launch {
        if (repository.writeSandboxTextFile(_currentSandboxDir.value, name, content)) {
            refreshSandboxFiles(_currentSandboxDir.value)
        }
    }

    fun deleteFile(file: File) = viewModelScope.launch {
        repository.deleteSandboxFile(file)
        refreshSandboxFiles(_currentSandboxDir.value)
    }

    fun importDocument(uri: Uri, displayName: String) = viewModelScope.launch {
        if (repository.importDocumentIntoSandbox(_currentSandboxDir.value, uri, displayName)) {
            refreshSandboxFiles(_currentSandboxDir.value)
            postToast("Imported $displayName")
        }
    }

    // Network Diagnostics
    fun runNetworkProbe(host: String) {
        viewModelScope.launch {
            _isProbingNetwork.value = true
            refreshTelemetry()
            _networkProbeResult.value = telemetryManager.runConnectivityDiagnostic(host)
            _isProbingNetwork.value = false
        }
    }

    // Reset & Wipe
    fun resetSettingsOnly() {
        viewModelScope.launch {
            repository.resetSettingsOnly()
            _shellState.update { it.copy(isLocked = false) }
            postToast("UK defaults restored")
        }
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            repository.clearAllUserData()
            _shellState.update {
                it.copy(
                    isLocked = false,
                    recentApps = emptyList(),
                    notifications = defaultNotifications()
                )
            }
            refreshSandboxFiles(repository.sandboxRootDir)
            postToast("All local user data cleared and reset")
        }
    }
}
