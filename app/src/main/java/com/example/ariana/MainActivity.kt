package com.example.ariana

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1001
        private const val FILE_CHOOSER_REQUEST_CODE = 1002
    }

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        requestAppPermissions()

        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // WebView uses native hardware acceleration via window compositor
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                mediaPlaybackRequiresUserGesture = false
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_DEFAULT
                displayZoomControls = false
                builtInZoomControls = false
                setSupportZoom(false)

                // Smooth scrolling and pre-raster optimizations
                setOffscreenPreRaster(true)
            }

            // Expose native Android bridge for biometric and persistent storage
            addJavascriptInterface(AndroidBiometricBridge(this@MainActivity, this), "AndroidBridge")

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(request.resources)
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    callback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    if (callback != null) {
                        filePathCallback = callback
                        val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "image/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                        try {
                            startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
                            return true
                        } catch (e: Exception) {
                            filePathCallback = null
                            return false
                        }
                    }
                    return false
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    return if (url.startsWith("file://") || url.startsWith("http://") || url.startsWith("https://")) {
                        false
                    } else {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }
                }
            }

            loadUrl("file:///android_asset/index.html")
        }

        setContentView(webView)

        val handledTypes = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        ViewCompat.setOnApplyWindowInsetsListener(webView) { view, insets ->
            val handledInsets = insets.getInsets(handledTypes)
            view.setPadding(handledInsets.left, handledInsets.top, handledInsets.right, handledInsets.bottom)
            WindowInsetsCompat.Builder(insets)
                .setInsets(handledTypes, Insets.NONE)
                .build()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback != null) {
                val results = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                filePathCallback?.onReceiveValue(results)
                filePathCallback = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}

class AndroidBiometricBridge(
    private val activity: FragmentActivity,
    private val webView: WebView
) {
    @JavascriptInterface
    fun getItem(key: String): String? {
        val prefs = activity.getSharedPreferences("ariana_storage", android.content.Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }

    @JavascriptInterface
    fun setItem(key: String, value: String): Boolean {
        val prefs = activity.getSharedPreferences("ariana_storage", android.content.Context.MODE_PRIVATE)
        return prefs.edit().putString(key, value).commit()
    }

    @JavascriptInterface
    fun removeItem(key: String): Boolean {
        val prefs = activity.getSharedPreferences("ariana_storage", android.content.Context.MODE_PRIVATE)
        return prefs.edit().remove(key).commit()
    }

    @JavascriptInterface
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        val canAuth = biometricManager.canAuthenticate(authenticators)
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    @JavascriptInterface
    fun authenticate(title: String, subtitle: String) {
        activity.runOnUiThread {
            try {
                val executor = ContextCompat.getMainExecutor(activity)
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title.ifBlank { "ورود به آریانا" })
                    .setSubtitle(subtitle.ifBlank { "لطفاً اثر انگشت خود را تأیید کنید" })
                    .setNegativeButtonText("انصراف")
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                BiometricManager.Authenticators.BIOMETRIC_WEAK
                    )
                    .build()

                val biometricPrompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            webView.evaluateJavascript(
                                "window.onNativeBiometricSuccess && window.onNativeBiometricSuccess();",
                                null
                            )
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            val cleanErr = errString.toString().replace("'", "\\'").replace("\"", "\\\"")
                            webView.evaluateJavascript(
                                "window.onNativeBiometricError && window.onNativeBiometricError('$cleanErr', $errorCode);",
                                null
                            )
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            webView.evaluateJavascript(
                                "window.onNativeBiometricFailed && window.onNativeBiometricFailed();",
                                null
                            )
                        }
                    }
                )

                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                val cleanErr = (e.message ?: "خطای احراز هویت").replace("'", "\\'").replace("\"", "\\\"")
                webView.evaluateJavascript(
                    "window.onNativeBiometricError && window.onNativeBiometricError('$cleanErr', -1);",
                    null
                )
            }
        }
    }
}
