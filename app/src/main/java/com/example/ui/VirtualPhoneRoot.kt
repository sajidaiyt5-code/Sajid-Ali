package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ThemeMode
import com.example.model.UkCity
import com.example.model.VirtualAppId
import com.example.model.WallpaperStyle
import com.example.ui.apps.BrowserScreen
import com.example.ui.apps.CalculatorScreen
import com.example.ui.apps.CalendarScreen
import com.example.ui.apps.CameraScreen
import com.example.ui.apps.ClockAlarmScreen
import com.example.ui.apps.ContactsScreen
import com.example.ui.apps.DashboardScreen
import com.example.ui.apps.FileManagerScreen
import com.example.ui.apps.GalleryScreen
import com.example.ui.apps.MapsLocationScreen
import com.example.ui.apps.MessagesScreen
import com.example.ui.apps.NetworkScreen
import com.example.ui.apps.NotesScreen
import com.example.ui.apps.PhoneDialerScreen
import com.example.ui.apps.SettingsScreen
import com.example.ui.shell.AppDrawerOverlay
import com.example.ui.shell.LockScreenOverlay
import com.example.ui.shell.NotificationQuickSettingsShade
import com.example.ui.shell.RecentsSwitcherOverlay
import com.example.ui.shell.SmartphoneHomeScreen
import com.example.ui.shell.VirtualNavigationBar
import com.example.ui.shell.VirtualStatusBar
import com.example.ui.shell.WallpaperBackdrop
import com.example.ui.theme.UkVirtualPhoneTheme
import com.example.viewmodel.VirtualPhoneViewModel

@Composable
fun VirtualPhoneRoot(viewModel: VirtualPhoneViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val shellState by viewModel.shellState.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()
    val smsMessages by viewModel.smsMessages.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.calendarEvents.collectAsStateWithLifecycle()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val galleryItems by viewModel.galleryItems.collectAsStateWithLifecycle()
    val sandboxFiles by viewModel.sandboxFiles.collectAsStateWithLifecycle()
    val currentSandboxDir by viewModel.currentSandboxDir.collectAsStateWithLifecycle()
    val probeResult by viewModel.networkProbeResult.collectAsStateWithLifecycle()
    val isProbing by viewModel.isProbingNetwork.collectAsStateWithLifecycle()

    val isDark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(shellState.statusToastMessage) {
        val msg = shellState.statusToastMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    val shouldInterceptBack = !shellState.isLocked && (
        shellState.isShadeOpen ||
            shellState.isRecentsOpen ||
            shellState.isAppDrawerOpen ||
            shellState.isHomeCustomizeMode ||
            shellState.activeApp != null
        )

    BackHandler(enabled = shouldInterceptBack) {
        viewModel.navigateBack()
    }

    UkVirtualPhoneTheme(darkTheme = isDark) {
        Box(modifier = Modifier.fillMaxSize()) {
            val isOnHomeScreen = shellState.activeApp == null &&
                !shellState.isAppDrawerOpen &&
                !shellState.isShadeOpen &&
                !shellState.isRecentsOpen

            if (isOnHomeScreen || shellState.isAppDrawerOpen || shellState.isRecentsOpen) {
                WallpaperBackdrop(wallpaperStyle = settings.wallpaperStyle)
            }

            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    VirtualStatusBar(
                        settings = settings,
                        nowLondon = shellState.nowLondon,
                        batteryInfo = shellState.batteryInfo,
                        networkInfo = shellState.networkInfo,
                        isOnHomeScreen = isOnHomeScreen,
                        onOpenShade = { viewModel.toggleShade(true) }
                    )
                },
                bottomBar = {
                    VirtualNavigationBar(
                        isOnHomeScreen = isOnHomeScreen,
                        onBackClick = { viewModel.navigateBack() },
                        onHomeClick = { viewModel.navigateHome() },
                        onRecentsClick = { viewModel.toggleRecents() }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Crossfade(
                        targetState = shellState.activeApp,
                        animationSpec = tween(durationMillis = 160),
                        label = "virtual_app_transition"
                    ) { activeApp ->
                        when (activeApp) {
                            null -> SmartphoneHomeScreen(
                                settings = settings,
                                nowLondon = shellState.nowLondon,
                                batteryInfo = shellState.batteryInfo,
                                storageInfo = shellState.storageInfo,
                                networkInfo = shellState.networkInfo,
                                isCustomizeMode = shellState.isHomeCustomizeMode,
                                onToggleCustomizeMode = viewModel::setHomeCustomizeMode,
                                onOpenApp = viewModel::openApp,
                                onOpenAppDrawer = { viewModel.toggleAppDrawer(true) },
                                onTogglePinApp = viewModel::togglePinnedHomeApp,
                                onCycleWallpaper = {
                                    val entries = WallpaperStyle.entries
                                    val next = entries[(entries.indexOf(settings.wallpaperStyle) + 1) % entries.size]
                                    viewModel.setWallpaperStyle(next)
                                }
                            )

                            VirtualAppId.PHONE -> PhoneDialerScreen(
                                settings = settings,
                                contacts = contacts,
                                callLogs = callLogs,
                                pendingNumber = shellState.pendingDialNumber,
                                telemetryManager = viewModel.telemetryManager,
                                onConsumePendingNumber = viewModel::consumePendingDialNumber,
                                onLogCall = { name, num, type -> viewModel.logCall(name, num, type) },
                                onClearCallLogs = viewModel::clearCallLogs,
                                onOpenSms = viewModel::openSmsConversation,
                                onPostToast = viewModel::postToast,
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.CONTACTS -> ContactsScreen(
                                settings = settings,
                                contacts = contacts,
                                onSaveContact = viewModel::saveContact,
                                onDeleteContact = viewModel::deleteContact,
                                onDialContact = viewModel::dialNumberInPhoneApp,
                                onMessageContact = viewModel::openSmsConversation,
                                onPostToast = viewModel::postToast,
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.MESSAGES -> MessagesScreen(
                                settings = settings,
                                messages = smsMessages,
                                pendingPeer = shellState.pendingSmsPeer,
                                onConsumePendingPeer = viewModel::consumePendingSmsPeer,
                                onSendLocalSms = viewModel::sendSms,
                                onDeleteThread = viewModel::deleteSmsThread,
                                onPostToast = viewModel::postToast,
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.CALCULATOR -> CalculatorScreen(
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.CALENDAR -> CalendarScreen(
                                settings = settings,
                                events = calendarEvents,
                                onSaveEvent = viewModel::saveCalendarEvent,
                                onDeleteEvent = viewModel::deleteCalendarEvent,
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.CLOCK -> ClockAlarmScreen(
                                settings = settings,
                                nowLondon = shellState.nowLondon,
                                alarms = alarms,
                                telemetryManager = viewModel.telemetryManager,
                                onSaveAlarm = viewModel::saveAlarm,
                                onDeleteAlarm = viewModel::deleteAlarm,
                                onPostToast = viewModel::postToast,
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.NOTES -> NotesScreen(
                                settings = settings,
                                notes = notes,
                                onVerifyPin = viewModel::unlockWithPin,
                                onSaveNote = viewModel::saveNote,
                                onDeleteNote = viewModel::deleteNote,
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.FILES -> FileManagerScreen(
                                settings = settings,
                                currentDir = currentSandboxDir,
                                files = sandboxFiles,
                                repository = viewModel.repository,
                                onOpenDir = viewModel::refreshSandboxFiles,
                                onNavigateUp = viewModel::navigateUpSandboxDir,
                                onCreateFolder = viewModel::createFolder,
                                onWriteTextFile = viewModel::createOrUpdateTextFile,
                                onDeleteFile = viewModel::deleteFile,
                                onImportDocument = viewModel::importDocument,
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.GALLERY -> GalleryScreen(
                                settings = settings,
                                items = galleryItems,
                                onImportPhoto = viewModel::importPhotoToGallery,
                                onDeletePhoto = viewModel::deleteGalleryItem,
                                onOpenCamera = { viewModel.openApp(VirtualAppId.CAMERA) },
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.CAMERA -> CameraScreen(
                                photosDir = viewModel.repository.cameraPhotosDir,
                                onPhotoCaptured = viewModel::addGalleryPhotoFromCamera,
                                onOpenGallery = { viewModel.openApp(VirtualAppId.GALLERY) },
                                onPostToast = viewModel::postToast,
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.BROWSER -> BrowserScreen(
                                onPostToast = viewModel::postToast,
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.MAPS -> MapsLocationScreen(
                                settings = settings,
                                deviceLocation = shellState.deviceLocationInfo,
                                onSelectVirtualCity = viewModel::selectUkCity,
                                onRefreshDeviceLocation = viewModel::refreshTelemetry,
                                onPostToast = viewModel::postToast,
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.DASHBOARD -> DashboardScreen(
                                settings = settings,
                                nowLondon = shellState.nowLondon,
                                batteryInfo = shellState.batteryInfo,
                                storageInfo = shellState.storageInfo,
                                networkInfo = shellState.networkInfo,
                                telemetryManager = viewModel.telemetryManager,
                                onRefresh = viewModel::refreshTelemetry,
                                onOpenApp = viewModel::openApp,
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.NETWORK -> NetworkScreen(
                                networkInfo = shellState.networkInfo,
                                probeResult = probeResult,
                                isProbing = isProbing,
                                onRefreshTelemetry = viewModel::refreshTelemetry,
                                onRunProbe = viewModel::runNetworkProbe,
                                onOpenBrowser = { viewModel.openApp(VirtualAppId.BROWSER) },
                                onBack = viewModel::navigateHome
                            )

                            VirtualAppId.SETTINGS -> SettingsScreen(
                                settings = settings,
                                telemetryManager = viewModel.telemetryManager,
                                biometricCapability = viewModel.getBiometricCapability(),
                                isKeystoreBacked = viewModel.securityManager.isKeystoreBacked(),
                                onUpdateDeviceName = viewModel::updateDeviceName,
                                onSelectCity = viewModel::selectUkCity,
                                onSetRegionProfile = viewModel::setRegionProfile,
                                onSetUse24Hour = viewModel::setUse24Hour,
                                onSetDateFormat = viewModel::setDateFormat,
                                onSetTemperatureUnit = viewModel::setTemperatureUnit,
                                onSetThemeMode = viewModel::setThemeMode,
                                onSetWallpaperStyle = viewModel::setWallpaperStyle,
                                onUpdateSetting = viewModel::updateSetting,
                                onToggleAppEnabled = viewModel::toggleAppEnabled,
                                onSetPin = viewModel::setPinCode,
                                onDisablePin = viewModel::disablePinLock,
                                onLockNow = viewModel::lockDeviceNow,
                                onResetSettingsOnly = viewModel::resetSettingsOnly,
                                onClearAllUserData = viewModel::clearAllUserData,
                                onPostToast = viewModel::postToast,
                                onBack = viewModel::navigateHome
                            )
                        }
                    }

                    // App Drawer Overlay
                    AnimatedVisibility(
                        visible = shellState.isAppDrawerOpen,
                        enter = slideInVertically { it / 3 } + fadeIn(),
                        exit = slideOutVertically { it / 3 } + fadeOut()
                    ) {
                        AppDrawerOverlay(
                            settings = settings,
                            onSelectApp = viewModel::openApp,
                            onClose = { viewModel.toggleAppDrawer(false) }
                        )
                    }

                    // Recents / Overview Switcher Overlay
                    AnimatedVisibility(
                        visible = shellState.isRecentsOpen,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        RecentsSwitcherOverlay(
                            recentApps = shellState.recentApps,
                            onSelectApp = viewModel::openApp,
                            onCloseApp = viewModel::closeRecentApp,
                            onClearAll = viewModel::clearAllRecents,
                            onDismiss = { viewModel.toggleRecents(false) }
                        )
                    }
                }
            }

            // Fullscreen Notification & Quick Settings Shade Overlay
            AnimatedVisibility(
                visible = shellState.isShadeOpen,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                NotificationQuickSettingsShade(
                    settings = settings,
                    nowLondon = shellState.nowLondon,
                    batteryInfo = shellState.batteryInfo,
                    networkInfo = shellState.networkInfo,
                    notifications = shellState.notifications,
                    onClose = { viewModel.toggleShade(false) },
                    onCycleTheme = {
                        val modes = ThemeMode.entries
                        val next = modes[(modes.indexOf(settings.themeMode) + 1) % modes.size]
                        viewModel.setThemeMode(next)
                    },
                    onSelectNextCity = {
                        val cities = UkCity.entries
                        val next = cities[(cities.indexOf(settings.selectedCity) + 1) % cities.size]
                        viewModel.selectUkCity(next)
                    },
                    onToggle24Hour = { viewModel.setUse24Hour(!settings.use24HourClock) },
                    onLockNow = viewModel::lockDeviceNow,
                    onOpenApp = viewModel::openApp,
                    onDismissNotification = viewModel::dismissNotification,
                    onClearAllNotifications = viewModel::clearAllNotifications
                )
            }

            // Fullscreen Lock Screen Overlay
            AnimatedVisibility(
                visible = shellState.isLocked,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LockScreenOverlay(
                    settings = settings,
                    nowLondon = shellState.nowLondon,
                    securityManager = viewModel.securityManager,
                    onVerifyPin = viewModel::unlockWithPin,
                    onBiometricUnlock = viewModel::unlockWithBiometricSuccess
                )
            }
        }
    }
}
