package com.example.ui.apps

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private val UK_BOOKMARKS = listOf(
    "GOV.UK" to "https://www.gov.uk",
    "BBC News" to "https://www.bbc.co.uk/news",
    "Met Office" to "https://www.metoffice.gov.uk",
    "NHS" to "https://www.nhs.uk",
    "TfL" to "https://tfl.gov.uk"
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    onPostToast: (String) -> Unit,
    onBack: () -> Unit
) {
    var currentUrl by remember { mutableStateOf("https://www.gov.uk") }
    var urlInput by remember { mutableStateOf("https://www.gov.uk") }
    var loadProgress by remember { mutableIntStateOf(100) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    fun navigateTo(raw: String) {
        val trimmed = raw.trim()
        val normalized = when {
            trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("http://") -> "https://" + trimmed.removePrefix("http://")
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://www.google.co.uk/search?q=${trimmed.replace(" ", "+")}"
        }
        urlInput = normalized
        currentUrl = normalized
        webViewRef?.loadUrl(normalized)
    }

    VirtualAppScaffold(
        title = "UK Browser",
        subtitle = "HTTPS Secure Browsing • United Kingdom",
        onBack = {
            if (webViewRef?.canGoBack() == true) {
                webViewRef?.goBack()
            } else {
                onBack()
            }
        },
        actions = {
            IconButton(onClick = { webViewRef?.goBack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Page back")
            }
            IconButton(onClick = { webViewRef?.goForward() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Page forward")
            }
            IconButton(onClick = { webViewRef?.reload() }) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reload")
            }
            IconButton(
                onClick = {
                    webViewRef?.clearCache(true)
                    webViewRef?.clearHistory()
                    onPostToast("Browser cache and history cleared")
                }
            ) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear browser cache")
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "HTTPS Secure",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { navigateTo(urlInput) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UK_BOOKMARKS.forEach { (label, url) ->
                    AssistChip(
                        onClick = { navigateTo(url) },
                        label = { Text(label) }
                    )
                }
            }

            if (loadProgress in 1..99) {
                LinearProgressIndicator(
                    progress = { loadProgress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val reqUrl = request?.url?.toString() ?: return false
                                    return if (reqUrl.startsWith("https://")) {
                                        urlInput = reqUrl
                                        false
                                    } else {
                                        true
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    if (url != null) urlInput = url
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    loadProgress = newProgress
                                }
                            }
                            loadUrl(currentUrl)
                            webViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
