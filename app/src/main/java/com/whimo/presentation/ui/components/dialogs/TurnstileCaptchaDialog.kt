/*
 * Copyright (c) 2025 EFI (https://efi.int/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.whimo.presentation.ui.components.dialogs

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.whimo.BuildConfig
import com.whimo.R
import org.json.JSONObject
import kotlin.math.roundToInt

private const val TAG = "TurnstileCaptcha"
private const val DEFAULT_CAPTCHA_VIEW_HEIGHT_DP = 220
private const val MIN_CAPTCHA_VIEW_HEIGHT_DP = 180
private const val MAX_CAPTCHA_VIEW_HEIGHT_DP = 260
private val CompactCaptchaScript = """
    (function() {
        if (document.getElementById('android-compact-captcha-style')) {
            return;
        }

        const style = document.createElement('style');
        style.id = 'android-compact-captcha-style';
        style.textContent = `
            html, body, #root {
                min-height: auto !important;
                background: transparent !important;
            }

            #root > div {
                min-height: auto !important;
                width: 100% !important;
                justify-content: flex-start !important;
                padding: 0 !important;
                gap: 16px !important;
                background: transparent !important;
            }

            iframe[src*="challenges.cloudflare.com"] {
                max-width: 100% !important;
            }
        `;
        document.head.appendChild(style);

        const fitTurnstile = function() {
            const frame = document.querySelector('iframe[src*="challenges.cloudflare.com"]');
            if (!frame) {
                return;
            }

            const widget = frame.closest('.cf-turnstile') || frame.parentElement || frame;
            const availableWidth = document.documentElement.clientWidth;
            const widgetWidth = Math.ceil(frame.getBoundingClientRect().width || frame.scrollWidth || 300);
            const scale = Math.min(1, availableWidth / widgetWidth);

            widget.style.maxWidth = '100%';
            widget.style.transformOrigin = 'top center';
            widget.style.zoom = scale;
        };

        fitTurnstile();

        if (!window.androidCaptchaFitTimer) {
            let attempts = 0;
            window.androidCaptchaFitTimer = window.setInterval(function() {
                fitTurnstile();
                attempts += 1;
                if (attempts >= 8) {
                    window.clearInterval(window.androidCaptchaFitTimer);
                    window.androidCaptchaFitTimer = null;
                }
            }, 250);
        }
    })();
""".trimIndent()
private val CaptchaContentHeightScript = """
    (function() {
        const content = document.querySelector('#root > div') || document.body;
        return Math.ceil(content.getBoundingClientRect().height);
    })();
""".trimIndent()

@Composable
fun TurnstileCaptchaDialog(
    captchaUrl: String,
    onTokenReceived: (String) -> Unit,
    onError: () -> Unit,
    onDismiss: () -> Unit,
) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var captchaViewHeight by remember { mutableStateOf(DEFAULT_CAPTCHA_VIEW_HEIGHT_DP.dp) }

    LaunchedEffect(captchaUrl) {
        if (captchaUrl.isBlank() || captchaUrl == "DEFAULT_URL") {
            onError()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
            webView = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(captchaViewHeight),
                        factory = { context ->
                            createTurnstileWebView(
                                context = context,
                                captchaUrl = captchaUrl,
                                onTokenReceived = onTokenReceived,
                                onError = onError,
                                onContentHeightChanged = { contentHeightDp ->
                                    captchaViewHeight = contentHeightDp
                                        .coerceIn(
                                            MIN_CAPTCHA_VIEW_HEIGHT_DP,
                                            MAX_CAPTCHA_VIEW_HEIGHT_DP,
                                        )
                                        .dp
                                },
                            ).also { webView = it }
                        }
                    )

                    TextButton(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onClick = onDismiss,
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createTurnstileWebView(
    context: android.content.Context,
    captchaUrl: String,
    onTokenReceived: (String) -> Unit,
    onError: () -> Unit,
    onContentHeightChanged: (Int) -> Unit,
): WebView {
    val mainHandler = Handler(Looper.getMainLooper())

    return WebView(context).apply {
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.applyCompactCaptchaLayout(onContentHeightChanged)
                logDebug("captcha page loaded: ${url.orEmpty().withoutQuery()}")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                if (request?.isForMainFrame == true) {
                    logDebug("captcha page error: ${error?.errorCode} ${error?.description}")
                    mainHandler.post { onError() }
                }
            }
        }
        webChromeClient = WebChromeClient()
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        setBackgroundColor(android.graphics.Color.TRANSPARENT)

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

        addJavascriptInterface(
            AndroidCaptchaBridge(
                onTokenReceived = { token ->
                    mainHandler.post { onTokenReceived(token) }
                },
                onError = { code ->
                    logDebug("captcha page returned error: $code")
                    mainHandler.post { onError() }
                },
            ),
            "AndroidCaptcha",
        )

        loadUrl(captchaUrl)
    }
}

private fun WebView.applyCompactCaptchaLayout(onContentHeightChanged: (Int) -> Unit) {
    evaluateJavascript(CompactCaptchaScript) {
        measureCaptchaContentHeight(onContentHeightChanged)
    }
    listOf(300L, 1_000L).forEach { delayMillis ->
        postDelayed(
            {
                evaluateJavascript(CompactCaptchaScript) {
                    measureCaptchaContentHeight(onContentHeightChanged)
                }
            },
            delayMillis,
        )
    }
}

private fun WebView.measureCaptchaContentHeight(onContentHeightChanged: (Int) -> Unit) {
    evaluateJavascript(CaptchaContentHeightScript) { result ->
        result.parseJavascriptNumber()?.let(onContentHeightChanged)
    }
}

private fun String.parseJavascriptNumber(): Int? {
    return trim('"').toFloatOrNull()?.roundToInt()
}

private class AndroidCaptchaBridge(
    private val onTokenReceived: (String) -> Unit,
    private val onError: (String?) -> Unit,
) {
    @JavascriptInterface
    fun postMessage(message: String?) {
        if (message.isNullOrBlank()) {
            onError("empty_message")
            return
        }

        val payload = runCatching { JSONObject(message) }.getOrElse {
            onError("invalid_message")
            return
        }

        when (payload.optString("status")) {
            "success" -> {
                val token = payload.optString("token")
                if (token.isBlank()) {
                    onError("empty_token")
                } else {
                    onTokenReceived(token)
                }
            }
            "error" -> onError(payload.optString("code").ifBlank { "unknown_error" })
            else -> onError("unknown_status")
        }
    }
}

private fun String.withoutQuery(): String {
    return runCatching {
        Uri.parse(this).buildUpon().clearQuery().fragment(null).build().toString()
    }.getOrDefault(this)
}

private fun logDebug(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(TAG, message)
    }
}
