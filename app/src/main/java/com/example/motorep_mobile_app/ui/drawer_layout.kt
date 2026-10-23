
package com.example.motorep_mobile_app.ui.common

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.motorep_mobile_app.MainActivity
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.session.SessionManager
import com.example.motorep_mobile_app.ui.dashboard.activity_dashboard
import com.example.motorep_mobile_app.ui.operacional.catalogos.clientes.activity_clientes
import com.example.motorep_mobile_app.ui.operacional.catalogos.empleados.activity_empleados
import com.example.motorep_mobile_app.ui.operacional.inventario.activity_inventario
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_marcas
import com.example.motorep_mobile_app.ui.operacional.catalogos.proveedores.activity_proveedores
import com.example.motorep_mobile_app.ui.reportes.activity_incidencias
import com.example.motorep_mobile_app.ui.reportes.activity_indicadores
import com.example.motorep_mobile_app.ui.reportes.activity_tendencias
import com.google.android.material.navigation.NavigationView

class MenuLateral(private val activity: AppCompatActivity) {

    private val drawerLayout: DrawerLayout =
        activity.findViewById(R.id.DrawerLayout)

    private val navView: NavigationView =
        activity.findViewById(R.id.navigationView)

    private val sessionManager = SessionManager(activity)

    // Destinos disponibles desde el menú lateral.
    private val destinos: Map<Int, Class<out Activity>> = mapOf(
        R.id.drawerLayoutInicio to activity_dashboard::class.java,
        R.id.drawerLayoutProveedores to activity_proveedores::class.java,
        R.id.drawerLayoutMarcas to activity_marcas::class.java,
        R.id.drawerLayoutClientes to activity_clientes::class.java,
        R.id.drawerLayoutEmpleados to activity_empleados::class.java,
        R.id.drawerLayoutInventario to activity_inventario::class.java,
        R.id.LinearLayoutIndicadores to activity_indicadores::class.java,
        R.id.LinearLayoutTendencias to activity_tendencias::class.java,
        R.id.LinearLayoutIncidencias to activity_incidencias::class.java
    )

    // Opciones permitidas según el grupo de Django.

    private fun obtenerOpcionesPermitidas(): Set<Int> {
        val grupos = sessionManager.obtenerGrupos()

        val esAdministrador = grupos.any {
            it.equals("Administrador", ignoreCase = true)
        }

        val esGerencia = grupos.any {
            it.equals("Gerencia", ignoreCase = true)
        }

        return when {
            // Accesos operativos del Administrador.
            esAdministrador -> setOf(
                R.id.drawerLayoutInicio,
                R.id.drawerLayoutProveedores,
                R.id.drawerLayoutMarcas,
                R.id.drawerLayoutClientes,
                R.id.drawerLayoutEmpleados,
                R.id.drawerLayoutInventario
            )

            // Accesos de consulta de Gerencia.
            esGerencia -> setOf(
                R.id.drawerLayoutInicio,
                R.id.LinearLayoutIndicadores,
                R.id.LinearLayoutTendencias,
                R.id.LinearLayoutIncidencias
            )

            // Sin permisos reconocidos, no mostrar módulos.
            else -> emptySet()
        }
    }


    fun configurar() {
        configurarInsets()
        aplicarPermisosAlMenu()
        configurarBotonMenu()
        configurarNavegacion()
        configurarCerrarSesion()
        configurarBotonAtras()
    }

    private fun aplicarPermisosAlMenu() {
        val permitidos = obtenerOpcionesPermitidas()

        destinos.keys.forEach { id ->
            activity.findViewById<View>(id)?.visibility =
                if (id in permitidos) View.VISIBLE else View.GONE
        }
    }

    private fun configurarInsets() {
        val mainConstraint =
            activity.findViewById<ConstraintLayout>(R.id.mainConstraint)

        val linearLayout =
            activity.findViewById<LinearLayout>(R.id.linearLayoutMainTitle)

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            linearLayout.setPadding(
                linearLayout.paddingLeft,
                bars.top,
                linearLayout.paddingRight,
                linearLayout.paddingBottom
            )

            mainConstraint.setPadding(0, 0, 0, bars.bottom)
            navView.setPadding(0, bars.top, 0, bars.bottom)

            insets
        }
    }

    private fun configurarBotonMenu() {
        activity.findViewById<ImageView>(R.id.imgMenu)
            ?.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
    }

    private fun configurarNavegacion() {
        val permitidos = obtenerOpcionesPermitidas()

        destinos.forEach { (idBoton, destino) ->
            val boton = activity.findViewById<View>(idBoton)

            boton?.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)

                // Comprobar permisos antes de abrir la pantalla.
                if (idBoton !in permitidos) {
                    return@setOnClickListener
                }

                if (activity::class.java != destino) {
                    val intent = Intent(activity, destino).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }

                    activity.startActivity(intent)
                }
            }
        }
    }

    private fun configurarCerrarSesion() {
        activity.findViewById<LinearLayout>(
            R.id.linearLayoutCerrarSesion
        )?.setOnClickListener {
            // Elimina el token y los grupos guardados localmente.
            sessionManager.cerrarSesion()

            val intent = Intent(
                activity,
                MainActivity::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            activity.startActivity(intent)
            activity.finish()
        }
    }

    private fun configurarBotonAtras() {
        activity.onBackPressedDispatcher.addCallback(activity) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                isEnabled = false
                activity.onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}
