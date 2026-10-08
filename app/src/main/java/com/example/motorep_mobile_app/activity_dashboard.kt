package com.example.motorep_mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout

class activity_dashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var imgMenu: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_dashboard)

        // Referencias principales del Dashboard
        drawerLayout = findViewById(R.id.DrawerLayout)
        imgMenu = findViewById(R.id.imgMenu)

        // Ajuste de la pantalla para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.mainConstraint)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Configurar botón del menú
        configurarBotonMenu()

        // Configurar opciones del menú
        configurarNavegacion()
    }

    // BOTÓN DEL MENÚ LATERAL

    private fun configurarBotonMenu() {

        imgMenu.setOnClickListener {

            // Abrir el menú lateral
            drawerLayout.openDrawer(
                findViewById(R.id.navigationView)
            )
        }
    }

    // NAVEGACIÓN DEL MENÚ

    private fun configurarNavegacion() {

        // INICIO

        findViewById<LinearLayout>(R.id.drawerLayoutInicio)
            .setOnClickListener {

                // Dashboard.
                drawerLayout.closeDrawers()
            }

        // FACTURACIÓN

        findViewById<LinearLayout>(R.id.drawerLayoutFacturas)
            .setOnClickListener {

                abrirPantalla(activity_facturacion::class.java)
            }

        // COMPRAS

        findViewById<LinearLayout>(R.id.drawerLayoutCompras)
            .setOnClickListener {

                abrirPantalla(activity_compras::class.java)
            }

        // CAJA

        findViewById<LinearLayout>(R.id.drawerLayoutCaja)
            .setOnClickListener {

                abrirPantalla(activity_caja::class.java)
            }

        // INVENTARIO

        findViewById<LinearLayout>(R.id.drawerLayoutInventario)
            .setOnClickListener {

                abrirPantalla(activity_inventario::class.java)
            }

        // CLIENTES
        findViewById<LinearLayout>(R.id.drawerLayoutClientes)
            .setOnClickListener {

                abrirPantalla(activity_clientes::class.java)
            }

        // EMPLEADOS

        findViewById<LinearLayout>(R.id.drawerLayoutEmpleados)
            .setOnClickListener {

                abrirPantalla(activity_empleados::class.java)
            }

        // INDICADORES

        findViewById<LinearLayout>(R.id.LinearLayoutIndicadores)
            .setOnClickListener {

                abrirPantalla(activity_indicadores::class.java)
            }

        // TENDENCIAS

        findViewById<LinearLayout>(R.id.LinearLayoutTendencias)
            .setOnClickListener {

                abrirPantalla(activity_tendencias::class.java)
            }

        // INCIDENCIAS

        findViewById<LinearLayout>(R.id.LinearLayoutIncidencias)
            .setOnClickListener {

                abrirPantalla(activity_incidencias::class.java)
            }

        // NOTIFICACIONES

        findViewById<LinearLayout>(R.id.LinearLayoutNotificaciones)
            .setOnClickListener {

                abrirPantalla(activity_notificaciones::class.java)
            }

        // CONFIGURACIÓN

        findViewById<LinearLayout>(R.id.LinearLayoutConfiguracion)
            .setOnClickListener {

                abrirPantalla(activity_configuracion::class.java)
            }

        // CERRAR SESIÓN
        findViewById<LinearLayout>(R.id.LinearLayoutCerrarSesion)
            .setOnClickListener {

                val intent =
                    Intent(this, activity_login::class.java)

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)

                finish()
            }
    }

    // FUNCIÓN AUXILIAR PARA ABRIR ACTIVITIES

    private fun abrirPantalla(pantalla: Class<*>) {

        // Cerrar el menú lateral
        drawerLayout.closeDrawers()

        // Crear Intent hacia la pantalla seleccionada
        val intent = Intent(this, pantalla)

        // Abrir la pantalla
        startActivity(intent)
    }
}