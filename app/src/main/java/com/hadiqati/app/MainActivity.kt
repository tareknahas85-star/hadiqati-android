package com.hadiqati.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private var cameraUri: Uri? = null

    // ── File/camera launcher ─────────────────────────────────────────
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris: Array<Uri>? = if (result.resultCode == Activity.RESULT_OK) {
            when {
                // Camera returns null data — use stored URI
                result.data == null && cameraUri != null -> arrayOf(cameraUri!!)
                // Gallery returns URI in data
                result.data?.clipData != null -> {
                    val clip = result.data!!.clipData!!
                    Array(clip.itemCount) { clip.getItemAt(it).uri }
                }
                result.data?.data != null -> arrayOf(result.data!!.data!!)
                else -> null
            }
        } else null
        cameraUri = null
        fileCallback?.onReceiveValue(uris)
        fileCallback = null
    }

    // ── Camera helper ─────────────────────────────────────────────────
    private fun createImageFile(): File? = try {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        File.createTempFile("PLANT_${ts}_", ".jpg", dir)
    } catch (e: Exception) { null }

    private fun launchCamera() {
        val file = createImageFile() ?: return
        cameraUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        launcher.launch(intent)
    }

    private fun launchGallery() {
        launcher.launch(Intent(Intent.ACTION_PICK).apply { type = "image/*" })
    }

    // ── JavaScript → Android bridge ───────────────────────────────────
    inner class Bridge {
        @JavascriptInterface fun openCamera()  = runOnUiThread { launchCamera() }
        @JavascriptInterface fun openGallery() = runOnUiThread { launchGallery() }
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

                // File chooser: distinguish camera vs gallery by isCaptureEnabled()
                override fun onShowFileChooser(
                    webView: WebView,
                    callback: ValueCallback<Array<Uri>>,
                    params: FileChooserParams
                ): Boolean {
                    fileCallback?.onReceiveValue(null)
                    fileCallback = callback
                    return try {
                        if (params.isCaptureEnabled()) launchCamera()
                        else launcher.launch(params.createIntent())
                        true
                    } catch (e: Exception) {
                        fileCallback = null; callback.onReceiveValue(null); false
                    }
                }

                // Auto-grant geolocation permission to our trusted local page
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
