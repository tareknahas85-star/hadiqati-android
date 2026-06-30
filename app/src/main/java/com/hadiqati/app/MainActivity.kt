package com.hadiqati.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.provider.MediaStore
import android.util.Base64
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var container: FrameLayout
    private var cameraUri: Uri? = null

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = when {
                result.data == null && cameraUri != null -> cameraUri
                result.data?.data != null               -> result.data!!.data
                result.data?.clipData != null           ->
                    result.data!!.clipData!!.getItemAt(0).uri
                else -> null
            }
            cameraUri = null
            uri?.let { readAndSendImage(it) }
        } else {
            cameraUri = null
        }
    }

    private fun readAndSendImage(uri: Uri) {
        Thread {
            try {
                val stream = contentResolver.openInputStream(uri) ?: return@Thread
                val bytes  = stream.readBytes()
                stream.close()
                val mime   = contentResolver.getType(uri) ?: "image/jpeg"
                val b64    = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val data   = "data:$mime;base64,$b64"
                runOnUiThread {
                    webView.evaluateJavascript("onNativePhoto('$data')", null)
                }
            } catch (_: Exception) {
                runOnUiThread {
                    webView.evaluateJavascript("toast('\u274c \u062e\u0637\u0623 \u0641\u064a \u0642\u0631\u0627\u0621\u0629 \u0627\u0644\u0635\u0648\u0631\u0629')", null)
                }
            }
        }.start()
    }

    private fun createImageFile(): File? = try {
        val ts  = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        File.createTempFile("PLANT_${ts}_", ".jpg", dir)
    } catch (_: Exception) { null }

    private fun launchCamera() {
        val file = createImageFile() ?: run {
            webView.post { webView.evaluateJavascript("toast('\u274c \u0644\u0627 \u064a\u0645\u0643\u0646 \u0627\u0644\u0648\u0635\u0648\u0644 \u0644\u0644\u0643\u0627\u0645\u064a\u0631\u0627')", null) }
            return
        }
        cameraUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        launcher.launch(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        )
    }

    private fun launchGallery() {
        launcher.launch(Intent(Intent.ACTION_PICK).apply { type = "image/*" })
    }

    // Chrome-like user agent (remove WebView "wv" flag for Google OAuth)
    private fun cleanUserAgent(ua: String): String =
        ua.replace("; wv)", ")").replace("Version/4.0 ", "")

    inner class Bridge {
        @JavascriptInterface
        fun openCamera()  = runOnUiThread { launchCamera() }

        @JavascriptInterface
        fun openGallery() = runOnUiThread { launchGallery() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        container = FrameLayout(this)

        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled        = true
                domStorageEnabled        = true
                allowFileAccess          = true
                cacheMode                = WebSettings.LOAD_DEFAULT
                mixedContentMode         = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(true)
                setGeolocationEnabled(true)
                // Remove WebView identifier so Google OAuth works
                userAgentString = cleanUserAgent(userAgentString)
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url.toString()
                    if (url.contains("accounts.google.com") || url.contains("googleapis.com")) {
                        return false
                    }
                    if (url.startsWith("http")) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        return true
                    }
                    return false
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView,
                    callback: ValueCallback<Array<Uri>>,
                    params: FileChooserParams
                ): Boolean {
                    callback.onReceiveValue(null)
                    return true
                }

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String,
                    callback: GeolocationPermissions.Callback
                ) { callback.invoke(origin, true, false) }

                // Handle Google Sign-In popup
                override fun onCreateWindow(
                    view: WebView, isDialog: Boolean,
                    isUserGesture: Boolean, resultMsg: Message?
                ): Boolean {
                    val popup = WebView(this@MainActivity).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        // Same clean user agent for popup
                        settings.userAgentString = cleanUserAgent(settings.userAgentString)

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(v: WebView, req: WebResourceRequest): Boolean {
                                val u = req.url.toString()
                                // Intercept OAuth redirect
                                if (u.startsWith("https://hadiqati-garden.web.app/callback")) {
                                    // Token is in fragment - but fragment not in URL here
                                    // Load the URL to get the fragment via JS
                                    return false
                                }
                                return false
                            }

                            override fun onPageFinished(v: WebView, url: String) {
                                // Check if we landed on the callback page
                                if (url.startsWith("https://hadiqati-garden.web.app/callback")) {
                                    // Extract token from URL fragment via JS
                                    v.evaluateJavascript(
                                        "(function(){ return window.location.hash; })()"
                                    ) { hash ->
                                        val h = hash.replace("\"", "")
                                        if (h.contains("access_token")) {
                                            val params = h.removePrefix("#").split("&")
                                            var token = ""
                                            var expires = "3600"
                                            for (p in params) {
                                                val kv = p.split("=", limit = 2)
                                                if (kv.size == 2) {
                                                    when (kv[0]) {
                                                        "access_token" -> token = kv[1]
                                                        "expires_in"   -> expires = kv[1]
                                                    }
                                                }
                                            }
                                            if (token.isNotEmpty()) {
                                                runOnUiThread {
                                                    // Pass token back to main WebView
                                                    webView.evaluateJavascript(
                                                        "onOAuthToken('$token','$expires')", null
                                                    )
                                                    // Close popup
                                                    container.removeView(v)
                                                    v.destroy()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onCloseWindow(window: WebView) {
                                container.removeView(window)
                                window.destroy()
                            }
                        }
                    }

                    container.addView(popup, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    ))
                    val transport = resultMsg?.obj as? WebView.WebViewTransport
                    transport?.webView = popup
                    resultMsg?.sendToTarget()
                    return true
                }

                override fun onCloseWindow(window: WebView) {
                    container.removeView(window)
                    window.destroy()
                }
            }

            addJavascriptInterface(Bridge(), "Android")
            loadUrl("file:///android_asset/index.html")
        }

        container.addView(webView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        setContentView(container)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (container.childCount > 1) {
                    val popup = container.getChildAt(container.childCount - 1)
                    container.removeView(popup)
                    if (popup is WebView) popup.destroy()
                    return
                }
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }
}
