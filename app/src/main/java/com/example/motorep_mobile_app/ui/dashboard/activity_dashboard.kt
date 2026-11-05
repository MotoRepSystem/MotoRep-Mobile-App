package com.example.motorep_mobile_app.ui.dashboard

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.google.android.material.navigation.NavigationView

import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.example.motorep_mobile_app.data.session.SessionManager

class activity_dashboard : AppCompatActivity() {

    private lateinit var webView: WebView
    private val reporteUrl = "https://app.powerbi.com/view?r=eyJrIjoiY2IwMzdlMmMtOTk1My00NTc4LTg5YTctZmRmNTJlZTlmN2M2IiwidCI6ImU0NzY0NmZlLWRhMjctNDUxOC04NDM2LTVmOGIxNThiYTEyNyIsImMiOjR9"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_dashboard)

        MenuLateral(this).configurar()

        configurarAccesoDashboard()
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

    private fun configurarAccesoDashboard() {
        val sessionManager = SessionManager(this)
        val grupos = sessionManager.obtenerGrupos()

        val esAdministrador = grupos.any {
            it.equals("Administrador", ignoreCase = true)
        }

        val esJefeVentas = grupos.any {
            it.equals("Jefe de ventas", ignoreCase = true)
        }

        val esGerencia = grupos.any {
            it.equals("Gerencia", ignoreCase = true)
        }

        // El administrador conserva los accesos operativos.
        if (esAdministrador) return

        val accesosPermitidos = when {
            esJefeVentas -> setOf(
                R.id.LinearLayoutIndicadores,
                R.id.LinearLayoutIncidencias
            )

            esGerencia -> setOf(
                R.id.LinearLayoutIndicadores,
                R.id.LinearLayoutTendencias,
                R.id.LinearLayoutIncidencias
            )

            else -> emptySet()
        }

        // Ocultar accesos directos que no corresponden al grupo.
        val accesosDashboard = listOf(
            R.id.drawerLayoutFacturas,
            R.id.drawerLayoutCompras,
            R.id.drawerLayoutCaja,
            R.id.drawerLayoutProveedores,
            R.id.drawerLayoutMarcas,
            R.id.drawerLayoutClientes,
            R.id.drawerLayoutEmpleados,
            R.id.drawerLayoutInventario,
            R.id.LinearLayoutNotificaciones,
            R.id.LinearLayoutConfiguracion
        )

        accesosDashboard.forEach { id ->
            findViewById<View>(id)?.visibility =
                View.GONE
        }

        // Los reportes autorizados permanecen visibles.
        val accesosDeReportes = listOf(
            R.id.LinearLayoutIndicadores,
            R.id.LinearLayoutTendencias,
            R.id.LinearLayoutIncidencias
        )

        accesosDeReportes.forEach { id ->
            findViewById<View>(id)?.visibility =
                if (id in accesosPermitidos) View.VISIBLE else View.GONE
        }
    }

}