package com.hadiqati.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.WindowManager
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var cameraUri: Uri? = null

    // ── Launcher: handles camera + gallery results ────────────────────
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = when {
                result.data == null && cameraUri != null -> cameraUri  // camera
                result.data?.data != null               -> result.data!!.data  // gallery single
                result.data?.clipData != null           ->
                    result.data!!.clipData!!.getItemAt(0).uri  // gallery multi
                else -> null
            }
            cameraUri = null
            uri?.let { readAndSendImage(it) }
        } else {
            cameraUri = null
        }
    }

    // ── Read image from URI → base64 → call onNativePhoto() in JS ─────
    // This is the CORRECT way to pass images to WebView on Android.
    // File/content URIs are NOT directly readable by WebView file inputs.
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
                    // evaluateJavascript must run on main thread
                    webView.evaluateJavascript("onNativePhoto('$data')", null)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    webView.evaluateJavascript("toast('❌ خطأ في قراءة الصورة')", null)
                }
            }
        }.start()
    }

    // ── Camera ────────────────────────────────────────────────────────
    private fun createImageFile(): File? = try {
        val ts  = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        File.createTempFile("PLANT_${ts}_", ".jpg", dir)
    } catch (e: Exception) { null }

    private fun launchCamera() {
        val file = createImageFile() ?: run {
            webView.post { webView.evaluateJavascript("toast('❌ لا يمكن الوصول للكاميرا')", null) }
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

    // ── JavaScript ↔ Android Bridge ───────────────────────────────────
    inner class Bridge {
        @JavascriptInterface
        fun openCamera()  = runOnUiThread { launchCamera() }

        @JavascriptInterface
        fun openGallery() = runOnUiThread { launchGallery() }

        // Google Sign-In — requires Google Cloud project setup
        // See: https://console.cloud.google.com → OAuth credentials
        @JavascriptInterface
        fun signInWithGoogle() = runOnUiThread {
            webView.evaluateJavascript(
                "toast('⚙️ يحتاج ضبط Google Cloud Client ID — راجع الإعدادات')", null
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled      = true
                domStorageEnabled      = true
                allowFileAccess        = true
                cacheMode              = WebSettings.LOAD_DEFAULT
                mixedContentMode       = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                setGeolocationEnabled(true)
            }
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                // We handle all file/camera via Bridge, not file input
                // Return true + null to cleanly reject the input mechanism
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
            }
            addJavascriptInterface(Bridge(), "Android")
            loadUrl("file:///android_asset/index.html")
        }

        setContentView(webView)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }
}
