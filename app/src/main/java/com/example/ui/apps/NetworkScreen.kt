package com.example.ui.apps

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.SettingsEthernet
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.system.NetworkDiagnosticResult
import com.example.data.system.RealNetworkInfo
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun NetworkScreen(
    networkInfo: RealNetworkInfo,
    probeResult: NetworkDiagnosticResult?,
    isProbing: Boolean,
    onRefreshTelemetry: () -> Unit,
    onRunProbe: (String) -> Unit,
    onOpenBrowser: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    VirtualAppScaffold(
        title = "Internet & Network",
        subtitle = "Real ConnectivityManager Telemetry",
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DisclosureNoticeCard(
                    text = "Network telemetry reflects your host device's real Wi-Fi / Cellular connection. This app does not create fake network links or emulate an independent UK SIM card."
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (networkInfo.isConnected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (networkInfo.isConnected) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (networkInfo.isConnected) {
                                    "Online via ${networkInfo.transportType}"
                                } else {
                                    "Offline — No Active Network"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (networkInfo.isValidated) {
                                    "Internet reachability validated by Android"
                                } else {
                                    "Not validated or restricted"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        FilledTonalButton(onClick = onRefreshTelemetry) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Router, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Link & Interface Properties", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(10.dp))
                        NetworkStatRow("Transport", networkInfo.transportType)
                        NetworkStatRow("Interface Name", networkInfo.interfaceName)
                        NetworkStatRow("Metered Connection", if (networkInfo.isMetered) "Yes (Cellular / Metered)" else "No (Unmetered)")
                        NetworkStatRow("Downstream Cap", "${networkInfo.downstreamKbps} kbps")
                        NetworkStatRow("Upstream Cap", "${networkInfo.upstreamKbps} kbps")
                        NetworkStatRow(
                            "DNS Servers",
                            if (networkInfo.dnsServers.isEmpty()) "None reported" else networkInfo.dnsServers.joinToString(", ")
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.NetworkCheck, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text("Network Troubleshooting & Probe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Run a live DNS + HTTPS HEAD check against UK endpoints to verify internet health.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onRunProbe("www.gov.uk") },
                                enabled = !isProbing,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Test GOV.UK")
                            }
                            OutlinedButton(
                                onClick = { onRunProbe("www.bbc.co.uk") },
                                enabled = !isProbing,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Test BBC.CO.UK")
                            }
                        }

                        if (isProbing) {
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(10.dp))
                                Text("Running DNS & HTTPS diagnostic...", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        probeResult?.let { res ->
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (res.success) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (res.success) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(res.host, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = res.summary,
                                        fontFamily = JetBrainsMonoFamily,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                            } else {
                                Intent(Settings.ACTION_WIFI_SETTINGS)
                            }
                            runCatching { context.startActivity(intent) }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.SettingsEthernet, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Wi-Fi Panel")
                    }
                    FilledTonalButton(
                        onClick = onOpenBrowser,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Open UK Browser")
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontFamily = JetBrainsMonoFamily, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
