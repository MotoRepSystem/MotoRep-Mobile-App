
package com.example.motorep_mobile_app.ui.operacional.catalogos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.motorep_mobile_app.Empleado_Adpter
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.activity_agregarempleado

import com.example.motorep_mobile_app.data.database.MotorepDatabase
import com.example.motorep_mobile_app.data.entity.Empleado
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Empleado_ApiRepository
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.viewmodel.Empleado_ApiViewModel

import com.google.android.material.floatingactionbutton.FloatingActionButton

class activity_empleados : AppCompatActivity() {



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


        MenuLateral(this).configurar()
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

}
