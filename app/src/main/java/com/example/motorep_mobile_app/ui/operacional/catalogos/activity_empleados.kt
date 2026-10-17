
package com.example.motorep_mobile_app.ui.operacional.catalogos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.motorep_mobile_app.Empleado_Adpter
import com.example.motorep_mobile_app.MainActivity
import com.example.motorep_mobile_app.R
<<<<<<< Updated upstream
=======
import com.example.motorep_mobile_app.activity_agregarempleado

import com.example.motorep_mobile_app.data.database.MotorepDatabase
import com.example.motorep_mobile_app.data.entity.Empleado
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Empleado_ApiRepository
import com.example.motorep_mobile_app.viewmodel.Empleado_ApiViewModel

import com.example.motorep_mobile_app.ui.dashboard.activity_dashboard
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_caja
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_compras
>>>>>>> Stashed changes
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_facturacion
import com.example.motorep_mobile_app.ui.reportes.activity_incidencias
import com.example.motorep_mobile_app.ui.reportes.activity_indicadores
import com.example.motorep_mobile_app.ui.reportes.activity_tendencias

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class activity_empleados : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navViews: NavigationView

    private lateinit var adapter: Empleado_Adpter

    // Acceso a la API.
    private lateinit var apiRepository: Empleado_ApiRepository
    private lateinit var apiViewModel: Empleado_ApiViewModel

    // Empleados almacenados localmente en Room.
    private var empleadosLocales: List<Empleado> = emptyList()

    // Empleados recibidos del servidor.
    // null significa que todavía no hemos recibido una respuesta correcta.
    private var empleadosRemotos: List<Empleado>? = null

    // Texto actual del buscador.
    private var textoBusqueda: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_empleados)

        // Inicializar el menú lateral.
        drawerLayout = findViewById(R.id.DrawerLayout)
        navViews = findViewById(R.id.navigationView)

        val mainConstraint =
            findViewById<ConstraintLayout>(R.id.mainConstraint)

        val linearLayout =
            findViewById<LinearLayout>(R.id.linearLayoutMainTitle)

        // Ajustar la pantalla a las barras del sistema.
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { _, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            linearLayout.setPadding(
                linearLayout.paddingLeft,
                systemBars.top,
                linearLayout.paddingRight,
                linearLayout.paddingBottom
            )

            mainConstraint.setPadding(
                0,
                0,
                0,
                systemBars.bottom
            )

            navViews.setPadding(
                0,
                systemBars.top,
                0,
                systemBars.bottom
            )

            insets
        }

        // Abrir el menú lateral.
        findViewById<ImageView>(R.id.imgMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

<<<<<<< Updated upstream
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
            val intent = Intent(this, activity_marcas::class.java)
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
            val intent = Intent(this, activity_proveedores::class.java)
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

=======
        configurarLista()
        configurarBuscador()
        configurarBaseLocal()
        configurarApi()
        configurarBotonAgregar()
        configurarNavegacion()
>>>>>>> Stashed changes
    }

    // Consultar nuevamente la API al regresar a esta pantalla.
    // Por ejemplo, después de registrar un empleado.
    override fun onResume() {
        super.onResume()

        if (::apiViewModel.isInitialized) {
            cargarEmpleadosRemotos()
        }
    }

    // --------------------------------------------------
    // 1. CONFIGURAR RECYCLERVIEW Y ADAPTADOR
    // --------------------------------------------------

    private fun configurarLista() {

        val recyclerView =
            findViewById<RecyclerView>(R.id.reciclerviewClientes)

        adapter = Empleado_Adpter { empleado ->

            Toast.makeText(
                this,
                "${empleado.Nombres} ${empleado.Apellidos}",
                Toast.LENGTH_SHORT
            ).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    // --------------------------------------------------
    // 2. CONFIGURAR BUSCADOR
    // --------------------------------------------------

    private fun configurarBuscador() {

        val searchView =
            findViewById<SearchView>(R.id.searchView)

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    textoBusqueda = query.orEmpty()
                    actualizarLista()
                    searchView.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    textoBusqueda = newText.orEmpty()
                    actualizarLista()
                    return true
                }
            }
        )
    }

    // --------------------------------------------------
    // 3. CONSULTAR EMPLEADOS GUARDADOS EN ROOM
    // --------------------------------------------------

    private fun configurarBaseLocal() {

        val database =
            MotorepDatabase.getDatabase(applicationContext)

        val empleadoDao = database.empleado_Dao()

        empleadoDao.ObtenerTodos().observe(this) { empleados ->

            empleadosLocales = empleados

            // Actualizar la lista si cambian los registros locales.
            actualizarLista()
        }
    }

    // --------------------------------------------------
    // 4. INICIALIZAR REPOSITORIO Y VIEWMODEL DE LA API
    // --------------------------------------------------

    private fun configurarApi() {

        apiRepository = Empleado_ApiRepository(
            RetrofitClient.create(this)
        )

        apiViewModel = Empleado_ApiViewModel(apiRepository)
    }

    // --------------------------------------------------
    // 5. OBTENER EMPLEADOS DESDE EL SERVIDOR
    // --------------------------------------------------

    private fun cargarEmpleadosRemotos() {

        apiViewModel.obtenerEmpleado { listaDto ->

            // Convertir el DTO recibido de Azure a la entidad local.
            empleadosRemotos = listaDto.map { dto ->

                Empleado(
                    id = dto.id ?: 0,
                    Nombres = dto.Nombres ?: "",
                    Apellidos = dto.Apellidos ?: "",
                    NumCedula = dto.NumCedula ?: "",
                    Telefono = dto.Telefono ?: ""
                )
            }

            actualizarLista()
        }
    }

    // --------------------------------------------------
    // 6. MOSTRAR Y FILTRAR EMPLEADOS
    // --------------------------------------------------

    private fun actualizarLista() {

        // Si la API ya respondió, utilizar sus datos.
        // Si todavía no ha respondido correctamente,
        // mostrar los empleados locales de Room.
        val fuenteEmpleados =
            empleadosRemotos ?: empleadosLocales

        val texto = textoBusqueda.trim()

        val empleadosFiltrados = if (texto.isEmpty()) {

            fuenteEmpleados

        } else {

            fuenteEmpleados.filter { empleado ->

                val nombreCompleto =
                    "${empleado.Nombres} ${empleado.Apellidos}"

                nombreCompleto.contains(
                    texto,
                    ignoreCase = true
                ) ||
                        empleado.Nombres.contains(
                            texto,
                            ignoreCase = true
                        ) ||
                        empleado.Apellidos.contains(
                            texto,
                            ignoreCase = true
                        ) ||
                        empleado.NumCedula.contains(
                            texto,
                            ignoreCase = true
                        ) ||
                        empleado.Telefono.contains(
                            texto,
                            ignoreCase = true
                        )
            }
        }

        adapter.submitList(empleadosFiltrados)
    }

    // --------------------------------------------------
    // 7. BOTÓN PARA AGREGAR UN EMPLEADO
    // --------------------------------------------------

    private fun configurarBotonAgregar() {

        findViewById<FloatingActionButton>(
            R.id.FloatingActionButtonAgregar
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    activity_agregarempleado::class.java
                )
            )
        }
    }

    // --------------------------------------------------
    // 8. NAVEGACIÓN DEL MENÚ LATERAL
    // --------------------------------------------------

    private fun configurarNavegacion() {

        findViewById<LinearLayout>(R.id.drawerLayoutFacturas)
            .setOnClickListener {
                navegarA(activity_facturacion::class.java)
            }

        findViewById<LinearLayout>(R.id.drawerLayoutCompras)
            .setOnClickListener {
                navegarA(activity_compras::class.java)
            }

        findViewById<LinearLayout>(R.id.drawerLayoutClientes)
            .setOnClickListener {
                navegarA(activity_clientes::class.java)
            }

        findViewById<LinearLayout>(R.id.drawerLayoutEmpleados)
            .setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }

        findViewById<LinearLayout>(R.id.drawerLayoutInventario)
            .setOnClickListener {
                navegarA(activity_inventario::class.java)
            }

        findViewById<LinearLayout>(R.id.drawerLayoutCaja)
            .setOnClickListener {
                navegarA(activity_caja::class.java)
            }

        findViewById<LinearLayout>(R.id.LinearLayoutIndicadores)
            .setOnClickListener {
                navegarA(activity_indicadores::class.java)
            }

        findViewById<LinearLayout>(R.id.LinearLayoutTendencias)
            .setOnClickListener {
                navegarA(activity_tendencias::class.java)
            }

        findViewById<LinearLayout>(R.id.LinearLayoutIncidencias)
            .setOnClickListener {
                navegarA(activity_incidencias::class.java)
            }

        findViewById<LinearLayout>(R.id.drawerLayoutInicio)
            .setOnClickListener {
                navegarA(activity_dashboard::class.java)
            }

        findViewById<LinearLayout>(R.id.linearLayoutCerrarSesion)
            .setOnClickListener {
                navegarA(MainActivity::class.java)
            }
    }

    private fun navegarA(destino: Class<*>) {

        drawerLayout.closeDrawer(GravityCompat.START)

        startActivity(
            Intent(this, destino)
        )
    }
}
