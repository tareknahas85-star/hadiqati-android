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
import android.view.View
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
            } catch (e: Exception) {
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
    } catch (e: Exception) { null }

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
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url.toString()
                    // Allow Google OAuth URLs inside WebView
                    if (url.contains("accounts.google.com") || url.contains("googleapis.com")) {
                        return false
                    }
                    // External links open in browser
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

                // Handle Google Sign-In popup window
                override fun onCreateWindow(
                    view: WebView, isDialog: Boolean,
                    isUserGesture: Boolean, resultMsg: Message?
                ): Boolean {
                    val popup = WebView(this@MainActivity).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.userAgentString = view.settings.userAgentString
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(v: WebView, req: WebResourceRequest): Boolean {
                                return false
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

                // Handle JS alerts
                override fun onJsAlert(
                    view: WebView, url: String, message: String,
                    result: JsResult
                ): Boolean {
                    return false // default handling
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
                // Close popup first if exists
                if (container.childCount > 1) {
                    val popup = container.getChildAt(container.childCount - 1)
                    container.removeView(popup)
                    return
                }
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }
}
