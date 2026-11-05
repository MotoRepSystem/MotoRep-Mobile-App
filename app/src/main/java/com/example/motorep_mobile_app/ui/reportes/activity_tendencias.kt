
package com.example.motorep_mobile_app.ui.reportes

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.ui.common.MenuLateral

class activity_tendencias : AppCompatActivity() {

    private lateinit var webView: WebView
    private val reporteUrl = "https://app.powerbi.com/view?r=eyJrIjoiMDhmMGU5NmMtMGI1OC00MmRkLWEyYTAtNmNkYmJkYWExODA0IiwidCI6ImU0NzY0NmZlLWRhMjctNDUxOC04NDM2LTVmOGIxNThiYTEyNyIsImMiOjR9"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tendencias)

        // Configurar menú lateral y permisos por rol.
        MenuLateral(this).configurar()


        webView = findViewById(R.id.webView)
        val progress = findViewById<ProgressBar>(R.id.progress)

        webView.settings.apply {
            javaScriptEnabled = true      // Power BI lo necesita
            domStorageEnabled = true      // también
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progress.visibility = View.GONE
            }
        }

        if (savedInstanceState == null) webView.loadUrl(reporteUrl)
        else webView.restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
