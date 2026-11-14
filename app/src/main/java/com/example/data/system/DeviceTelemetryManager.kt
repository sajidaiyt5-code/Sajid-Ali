package com.example.data.system

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

data class RealBatteryInfo(
    val levelPercent: Int,
    val isCharging: Boolean,
    val statusLabel: String,
    val temperatureCelsius: Float?
)

data class RealStorageInfo(
    val totalGb: Double,
    val freeGb: Double,
    val usedGb: Double,
    val usedFraction: Float
)

data class RealNetworkInfo(
    val isConnected: Boolean,
    val transportType: String, // "Wi-Fi", "Cellular", "Ethernet", "VPN", "Offline"
    val isValidated: Boolean,
    val isMetered: Boolean,
    val interfaceName: String,
    val dnsServers: List<String>,
    val downstreamKbps: Int,
    val upstreamKbps: Int
)

data class RealDeviceLocationInfo(
    val hasPermission: Boolean,
    val provider: String?,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Float?,
    val statusMessage: String
)

data class NetworkDiagnosticResult(
    val host: String,
    val dnsResolvedIp: String?,
    val httpStatusCode: Int?,
    val latencyMs: Long?,
    val success: Boolean,
    val summary: String
)

class DeviceTelemetryManager(private val context: Context) {

    fun getRealBatteryInfo(): RealBatteryInfo {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 100

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val statusLabel = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }

        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val tempC = if (tempTenths != null && tempTenths != Int.MIN_VALUE) tempTenths / 10f else null

        return RealBatteryInfo(
            levelPercent = pct.coerceIn(0, 100),
            isCharging = isCharging,
            statusLabel = statusLabel,
            temperatureCelsius = tempC
        )
    }

    fun getRealStorageInfo(): RealStorageInfo {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBytes = stat.blockCountLong * blockSize
        val availableBytes = stat.availableBlocksLong * blockSize
        val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)

        val gbFactor = 1024.0 * 1024.0 * 1024.0
        val totalGb = totalBytes / gbFactor
        val freeGb = availableBytes / gbFactor
        val usedGb = usedBytes / gbFactor
        val fraction = if (totalBytes > 0) (usedBytes.toDouble() / totalBytes.toDouble()).toFloat() else 0f

        return RealStorageInfo(
            totalGb = totalGb,
            freeGb = freeGb,
            usedGb = usedGb,
            usedFraction = fraction.coerceIn(0f, 1f)
        )
    }

    fun getRealNetworkInfo(): RealNetworkInfo {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return RealNetworkInfo(false, "Unavailable", false, true, "none", emptyList(), 0, 0)

        val activeNetwork = cm.activeNetwork
            ?: return RealNetworkInfo(false, "Offline", false, false, "none", emptyList(), 0, 0)

        val caps = cm.getNetworkCapabilities(activeNetwork)
            ?: return RealNetworkInfo(false, "Offline", false, false, "none", emptyList(), 0, 0)

        val linkProps = cm.getLinkProperties(activeNetwork)

        val transport = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "Connected"
        }

        val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isMetered = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        return RealNetworkInfo(
            isConnected = isInternet,
            transportType = transport,
            isValidated = isValidated,
            isMetered = isMetered,
            interfaceName = linkProps?.interfaceName ?: "unknown",
            dnsServers = linkProps?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList(),
            downstreamKbps = caps.linkDownstreamBandwidthKbps,
            upstreamKbps = caps.linkUpstreamBandwidthKbps
        )
    }

    fun getRealDeviceLocation(): RealDeviceLocationInfo {
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCoarse) {
            return RealDeviceLocationInfo(
                hasPermission = false,
                provider = null,
                latitude = null,
                longitude = null,
                accuracyMeters = null,
                statusMessage = "Coarse location permission not granted. Using virtual UK region only."
            )
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return RealDeviceLocationInfo(
                hasPermission = true,
                provider = null,
                latitude = null,
                longitude = null,
                accuracyMeters = null,
                statusMessage = "LocationManager unavailable on this hardware."
            )

        val providers = lm.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val loc = runCatching { lm.getLastKnownLocation(provider) }.getOrNull() ?: continue
            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                bestLocation = loc
            }
        }

        return if (bestLocation != null) {
            RealDeviceLocationInfo(
                hasPermission = true,
                provider = bestLocation.provider ?: "system",
                latitude = bestLocation.latitude,
                longitude = bestLocation.longitude,
                accuracyMeters = bestLocation.accuracy,
                statusMessage = "Last known device fix from Android LocationManager (${bestLocation.provider})"
            )
        } else {
            RealDeviceLocationInfo(
                hasPermission = true,
                provider = providers.firstOrNull(),
                latitude = null,
                longitude = null,
                accuracyMeters = null,
                statusMessage = "Permission granted, waiting for device GPS/network fix (unavailable in sandboxed container)."
            )
        }
    }

    suspend fun runConnectivityDiagnostic(host: String = "www.gov.uk"): NetworkDiagnosticResult =
        withContext(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            try {
                val address = InetAddress.getByName(host)
                val url = URL("https://$host")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    requestMethod = "HEAD"
                    instanceFollowRedirects = true
                }
                val code = conn.responseCode
                conn.disconnect()
                val elapsed = System.currentTimeMillis() - start
                NetworkDiagnosticResult(
                    host = host,
                    dnsResolvedIp = address.hostAddress,
                    httpStatusCode = code,
                    latencyMs = elapsed,
                    success = code in 200..399,
                    summary = "HTTPS $code • ${elapsed}ms • Resolved ${address.hostAddress}"
                )
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis() - start
                NetworkDiagnosticResult(
                    host = host,
                    dnsResolvedIp = null,
                    httpStatusCode = null,
                    latencyMs = elapsed,
                    success = false,
                    summary = "Probe failed (${e.javaClass.simpleName}): ${e.message ?: "Offline"}"
                )
            }
        }

    fun playDtmfTone(char: Char) {
        val toneType = when (char) {
            '0' -> ToneGenerator.TONE_DTMF_0
            '1' -> ToneGenerator.TONE_DTMF_1
            '2' -> ToneGenerator.TONE_DTMF_2
            '3' -> ToneGenerator.TONE_DTMF_3
            '4' -> ToneGenerator.TONE_DTMF_4
            '5' -> ToneGenerator.TONE_DTMF_5
            '6' -> ToneGenerator.TONE_DTMF_6
            '7' -> ToneGenerator.TONE_DTMF_7
            '8' -> ToneGenerator.TONE_DTMF_8
            '9' -> ToneGenerator.TONE_DTMF_9
            '*' -> ToneGenerator.TONE_DTMF_S
            '#' -> ToneGenerator.TONE_DTMF_P
            else -> return
        }
        runCatching {
            val tg = ToneGenerator(AudioManager.STREAM_DTMF, 65)
            tg.startTone(toneType, 120)
        }
    }

    fun triggerAlertChimeAndVibrate() {
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.play()
        }
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(220, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(220)
            }
        }
    }

    fun getAndroidDeviceSummary(): Map<String, String> {
        return mapOf(
            "Hardware Model" to "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            "Android Version" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "Build ID" to Build.DISPLAY,
            "ABI" to (Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a")
        )
    }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
