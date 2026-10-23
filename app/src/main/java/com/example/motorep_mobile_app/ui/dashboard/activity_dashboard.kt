package com.example.motorep_mobile_app.ui.dashboard

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.google.android.material.navigation.NavigationView

import android.view.View
import com.example.motorep_mobile_app.data.session.SessionManager

class activity_dashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navViews: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_dashboard)

        MenuLateral(this).configurar()

        configurarAccesoDashboard()

        /*drawerLayout = findViewById(R.id.DrawerLayout)
        navViews = findViewById(R.id.navigationView)

        val mainConstraint = findViewById<ConstraintLayout>(R.id.mainConstraint)
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayoutMainTitle)

        // Responsividad del Dashboard y del menú lateral
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            linearLayout.setPadding(
                linearLayout.paddingLeft,
                systemBars.top,
                linearLayout.paddingRight,
                linearLayout.paddingBottom
            )

            mainConstraint.setPadding(0, 0, 0, systemBars.bottom)

            navViews.setPadding(
                0,
                systemBars.top,
                0,
                systemBars.bottom
            )

            insets
        }

        // Botón abrir menú
        val btnMenu = findViewById<ImageView>(R.id.imgMenu)
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        /*// Botón cerrar menú
        val btnMenuVolver = findViewById<ImageView>(R.id.imgCerrarMenu)
        btnMenuVolver.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }*/

        val btnFacturas = findViewById<LinearLayout>(R.id.drawerLayoutFacturas)
        btnFacturas?.setOnClickListener {
            val intent = Intent(this, activity_facturacion::class.java)
            startActivity(intent)
        }

        val btnCompras = findViewById<LinearLayout>(R.id.drawerLayoutCompras)
        btnCompras?.setOnClickListener {
            val intent = Intent(this, activity_marcas::class.java)
            startActivity(intent)
        }

        val btnClientes = findViewById<LinearLayout>(R.id.drawerLayoutClientes)
        btnClientes?.setOnClickListener {
            val intent = Intent(this, activity_clientes::class.java)
            startActivity(intent)
        }

        val btnEmpleados = findViewById<LinearLayout>(R.id.drawerLayoutEmpleados)
        btnEmpleados?.setOnClickListener {
            val intent = Intent(this, activity_empleados::class.java)
            startActivity(intent)
        }

        val btnInventario = findViewById<LinearLayout>(R.id.drawerLayoutInventario)
        btnInventario?.setOnClickListener {
            val intent = Intent(this, activity_inventario::class.java)
            startActivity(intent)
        }

        val btnCaja = findViewById<LinearLayout>(R.id.drawerLayoutCaja)
        btnCaja?.setOnClickListener {
            val intent = Intent(this, activity_proveedores::class.java)
            startActivity(intent)
        }


        val btnIndicadores = findViewById<LinearLayout>(R.id.LinearLayoutIndicadores)
        btnIndicadores?.setOnClickListener {
            val intent = Intent(this, activity_indicadores::class.java)
            startActivity(intent)
        }

        val btnTendencias = findViewById<LinearLayout>(R.id.LinearLayoutTendencias)
        btnTendencias?.setOnClickListener {
            val intent = Intent(this, activity_tendencias::class.java)
            startActivity(intent)
        }

        val btnIncidencias = findViewById<LinearLayout>(R.id.LinearLayoutIncidencias)
        btnIncidencias?.setOnClickListener {
            val intent = Intent(this, activity_incidencias::class.java)
            startActivity(intent)
        }
        val btnInicio = findViewById<LinearLayout>(R.id.drawerLayoutInicio)
        btnInicio?.setOnClickListener {
            val intent = Intent(this, activity_dashboard::class.java)
            startActivity(intent)
        }

        // Abrir la pantalla de Notificaciones.
        val btnNotificaciones =
            findViewById<LinearLayout>(R.id.LinearLayoutNotificaciones)

        btnNotificaciones?.setOnClickListener {
            val intent = Intent(this, activity_notificaciones::class.java)
            startActivity(intent)
        }

        // Abrir la pantalla de Configuración.
        val btnConfiguracion =
            findViewById<LinearLayout>(R.id.LinearLayoutConfiguracion)

        btnConfiguracion?.setOnClickListener {
            val intent = Intent(this, activity_configuracion::class.java)
            startActivity(intent)
        }

        val btnSalir = findViewById<LinearLayout>(R.id.linearLayoutCerrarSesion)
        btnSalir?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }*/
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