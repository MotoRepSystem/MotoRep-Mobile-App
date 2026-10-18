
package com.example.motorep_mobile_app.ui.operacional.catalogos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.motorep_mobile_app.Empleado_Adpter
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.activity_agregarempleado

import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.entity.Empleado
import com.example.motorep_mobile_app.data.remote.dto.toDto
import com.example.motorep_mobile_app.data.remote.dto.toEntity
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Empleado_ApiRepository
import com.example.motorep_mobile_app.data.repository.Empleado_Repository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.viewmodel.Empleado_ApiViewModel
import com.example.motorep_mobile_app.viewmodel.Empleado_ViewModel

import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class activity_empleados : AppCompatActivity() {
/*
    private lateinit var adapter: Empleado_Adpter

    // Acceso a Room Local.
    private lateinit var repository: Empleado_Repository
    private lateinit var viewModel: Empleado_ViewModel
    private var empleadosLocales: List<Empleado> = emptyList()

    // Acceso a la API.
    private lateinit var apiRepository: Empleado_ApiRepository
    private lateinit var apiViewModel: Empleado_ApiViewModel

    // Texto actual del buscador.
    private var textoBusqueda: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_empleados)

        MenuLateral(this).configurar()

        configurarLista()
        configurarBuscador()
        configurarBaseLocal()
        configurarApi()
        configurarBotonAgregar()
    }

    override fun onResume() {
        super.onResume()
        cargarEmpleadosRemotos()
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
                    aplicarFiltro(empleadosLocales)
                    searchView.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    textoBusqueda = newText.orEmpty()
                    aplicarFiltro(empleadosLocales)
                    return true
                }
            }
        )
    }

    // --------------------------------------------------
    // 3. CONSULTAR EMPLEADOS GUARDADOS EN ROOM
    // --------------------------------------------------

    private fun configurarBaseLocal() {

        val database = Motorep_Database.getDatabase(applicationContext)
        val dao = database.empleadoDao()
        repository = Empleado_Repository(dao)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(Empleado_ViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return Empleado_ViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        viewModel = ViewModelProvider(this, factory)[Empleado_ViewModel::class.java]

        viewModel.empleado.observe(this) { empleado ->
            empleadosLocales = empleado
            aplicarFiltro(empleadosLocales)
        }
    }

    // --------------------------------------------------
    // 4. INICIALIZAR REPOSITORIO Y VIEWMODEL DE LA API
    // --------------------------------------------------

    private fun configurarApi() {
        apiRepository = Empleado_ApiRepository(RetrofitClient.create(this))
        apiViewModel = Empleado_ApiViewModel(apiRepository)
    }

    // --------------------------------------------------
    // 5. OBTENER EMPLEADOS DESDE EL SERVIDOR (OFFLINE-FIRST)
    // --------------------------------------------------

    private fun cargarEmpleadosRemotos() {

        if (NetworkUtils.hayInternet(this)) {

            sincronizarPendientes {

                apiViewModel.obtenerEmpleado { listaDto ->

                    val listaEmpleados = listaDto.map { it.toEntity() }

                    lifecycleScope.launch {

                        listaEmpleados.forEach { empleado ->
                            viewModel.sincronizarDesdeAzure(empleado)
                        }

                        viewModel.eliminarEmpleadosQueYaNoExistenEnAzure(listaEmpleados)
                    }
                }
            }

        } else {

            Toast.makeText(
                this,
                "Sin conexión. Mostrando datos guardados localmente.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sincronizarPendientes(onFinish: () -> Unit) {
        lifecycleScope.launch {

            val pendientesCrear = viewModel.obtenerPendientesCrear()

            Log.d("SYNC_DEBUG", "Empleados pendientes a crear: ${pendientesCrear.size}")

            for (empleado in pendientesCrear) {
                try {
                    val respuesta = apiRepository.guardarEmpleado(empleado.toDto())

                    val serverId = respuesta.id

                    if (serverId != null) {
                        viewModel.marcarComoSincronizado(empleado.syncId, serverId)
                        Log.d("SYNC_DEBUG", "Empleado sincronizado con éxito: ${empleado.Nombres}")
                    }
                } catch (e: Exception) {
                    Log.e("SYNC_DEBUG", "Error al sincronizar empleado ${empleado.Nombres}: ${e.message}")
                }
            }

            val pendientesEliminar = viewModel.obtenerPendientesEliminar()

            Log.d("SYNC_DEBUG", "Empleados pendientes a eliminar: ${pendientesEliminar.size}")

            for (empleado in pendientesEliminar) {
                try {
                    val serverId = empleado.serverId

                    if (serverId == null) {
                        viewModel.eliminarPorId(empleado.id)
                        Log.d("SYNC_DEBUG", "Empleado eliminado de Room: ${empleado.Nombres}")
                        continue
                    }

                    apiRepository.eliminarEmpleado(serverId)
                    viewModel.eliminarPorId(empleado.id)
                    Log.d("SYNC_DEBUG", "Empleado eliminado con éxito de Azure: ${empleado.Nombres}")

                } catch (e: Exception) {
                    Log.e("SYNC_DEBUG", "Error al eliminar empleado ${empleado.Nombres}: ${e.message}")
                }
            }

            onFinish()
        }
    }

    // --------------------------------------------------
    // 6. FILTRAR Y MOSTRAR EMPLEADOS
    // --------------------------------------------------

    private fun aplicarFiltro(listaEmpleados: List<Empleado>) {

        val texto = textoBusqueda.trim()

        val empleadosFiltrados = if (texto.isEmpty()) {
            listaEmpleados
        } else {
            listaEmpleados.filter { empleado ->
                val nombreCompleto = "${empleado.Nombres} ${empleado.Apellidos}"
                nombreCompleto.contains(texto, ignoreCase = true) ||
                        empleado.Nombres.contains(texto, ignoreCase = true) ||
                        empleado.Apellidos.contains(texto, ignoreCase = true) ||
                        empleado.NumCedula.contains(texto, ignoreCase = true) ||
                        empleado.Telefono.contains(texto, ignoreCase = true)
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
                Intent(this, activity_agregarempleado::class.java)
            )
        }
    }*/
}
