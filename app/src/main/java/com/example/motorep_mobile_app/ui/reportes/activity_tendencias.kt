package com.example.motorep_mobile_app.ui.reportes

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.motorep_mobile_app.MainActivity
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_inventario
import com.example.motorep_mobile_app.ui.dashboard.activity_dashboard
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_clientes
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_empleados
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_caja
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_compras
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_facturacion
import com.google.android.material.navigation.NavigationView

class activity_tendencias : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navViews: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tendencias)



        drawerLayout = findViewById(R.id.DrawerLayout)
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
        btnFacturas.setOnClickListener {
            val intent = Intent(this, activity_facturacion::class.java)
            startActivity(intent)
        }

        val btnCompras = findViewById<LinearLayout>(R.id.drawerLayoutCompras)
        btnCompras.setOnClickListener {
            val intent = Intent(this, activity_compras::class.java)
            startActivity(intent)
        }

        val btnClientes = findViewById<LinearLayout>(R.id.drawerLayoutClientes)
        btnClientes.setOnClickListener {
            val intent = Intent(this, activity_clientes::class.java)
            startActivity(intent)
        }

        val btnEmpleados = findViewById<LinearLayout>(R.id.drawerLayoutEmpleados)
        btnEmpleados.setOnClickListener {
            val intent = Intent(this, activity_empleados::class.java)
            startActivity(intent)
        }

        val btnInventario = findViewById<LinearLayout>(R.id.drawerLayoutInventario)
        btnInventario.setOnClickListener {
            val intent = Intent(this, activity_inventario::class.java)
            startActivity(intent)
        }

        val btnCaja = findViewById<LinearLayout>(R.id.drawerLayoutCaja)
        btnCaja.setOnClickListener {
            val intent = Intent(this, activity_caja::class.java)
            startActivity(intent)
        }


        val btnIndicadores = findViewById<LinearLayout>(R.id.LinearLayoutIndicadores)
        btnIndicadores.setOnClickListener {
            val intent = Intent(this, activity_indicadores::class.java)
            startActivity(intent)
        }

        val btnTendencias = findViewById<LinearLayout>(R.id.LinearLayoutTendencias)
        btnTendencias.setOnClickListener {
            val intent = Intent(this, activity_tendencias::class.java)
            startActivity(intent)
        }

        val btnIncidencias = findViewById<LinearLayout>(R.id.LinearLayoutIncidencias)
        btnIncidencias.setOnClickListener {
            val intent = Intent(this, activity_incidencias::class.java)
            startActivity(intent)
        }
        val btnInicio = findViewById<LinearLayout>(R.id.drawerLayoutInicio)
        btnInicio.setOnClickListener {
            val intent = Intent(this, activity_dashboard::class.java)
            startActivity(intent)
        }
        val btnSalir = findViewById<LinearLayout>(R.id.linearLayoutCerrarSesion)
        btnSalir.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}