package com.example.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.security.BiometricCapability
import com.example.data.security.SecurityManager
import com.example.model.VirtualPhoneSettings
import com.example.ui.theme.JetBrainsMonoFamily
import java.time.ZonedDateTime

@Composable
fun LockScreenOverlay(
    settings: VirtualPhoneSettings,
    nowLondon: ZonedDateTime,
    securityManager: SecurityManager,
    onVerifyPin: (String) -> Boolean,
    onBiometricUnlock: () -> Unit
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val biometricReady = settings.biometricEnabled &&
        securityManager.getBiometricCapability() == BiometricCapability.AVAILABLE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A1429), Color(0xFF13264D), Color(0xFF0A101E))
                )
            )
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Device locked",
                    tint = Color(0xFF9BB8FF),
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = settings.formatCurrentTime(nowLondon),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${settings.formatCurrentDate(nowLondon)} • ${settings.selectedCity.displayName}, UK",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = settings.virtualDeviceName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF9BB8FF)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PIN dots
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = errorText ?: "Enter 4-digit PIN to unlock",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (errorText != null) Color(0xFFFF8FA3) else Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(4) { index ->
                        val filled = index < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) Color(0xFF9BB8FF) else Color.White.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Numeric Keypad
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("CLR", "0", "DEL")
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        row.forEach { key ->
                            Surface(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        errorText = null
                                        when (key) {
                                            "CLR" -> enteredPin = ""
                                            "DEL" -> if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                            }
                                            else -> {
                                                if (enteredPin.length < 4) {
                                                    val next = enteredPin + key
                                                    enteredPin = next
                                                    if (next.length == 4) {
                                                        if (!onVerifyPin(next)) {
                                                            errorText = "Incorrect PIN. Try again."
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("lock_key_$key"),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    when (key) {
                                        "DEL" -> Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                            contentDescription = "Backspace",
                                            tint = Color.White
                                        )
                                        "CLR" -> Text(
                                            text = "CLR",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        else -> Text(
                                            text = key,
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (biometricReady && context is FragmentActivity) {
                Spacer(modifier = Modifier.height(18.dp))
                FilledTonalButton(
                    onClick = {
                        securityManager.authenticateWithBiometrics(
                            activity = context,
                            onSuccess = onBiometricUnlock,
                            onError = { msg -> errorText = msg }
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF1D3B7A),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Rounded.Fingerprint, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Use Biometrics")
                }
            }
        }
    }
}
