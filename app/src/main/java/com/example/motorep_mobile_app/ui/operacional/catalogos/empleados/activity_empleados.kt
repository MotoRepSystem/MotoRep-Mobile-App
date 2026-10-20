package com.example.motorep_mobile_app.ui.operacional.catalogos.empleados

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motorep_mobile_app.Empleado_Adpter
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.database.Motorep_Database
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
    // Las guardamos como propiedades de la clase para poder reutilizarlas en onResume() sin volver a crearlas
    private lateinit var apiViewModel: Empleado_ApiViewModel
    private lateinit var apiRepository: Empleado_ApiRepository // NUEVO: necesario para sincronizarPendientes()
    private lateinit var viewModel: Empleado_ViewModel
    private lateinit var adapter: Empleado_Adpter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_empleados)


        MenuLateral(this).configurar()


        // Nos permite recrear y traer el flujo de la base de datos para poder utilizarlo aquí
        val database = Motorep_Database.getDatabase(applicationContext)
        val dao = database.empleadoDao()
        val repository = Empleado_Repository(dao)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                if (modelClass.isAssignableFrom(Empleado_ViewModel::class.java)) {
                    return Empleado_ViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(
            this,
            factory
        )[Empleado_ViewModel::class.java]


        // Accedemos al recyclerView
        val recyclerEmpleados = findViewById<RecyclerView>(R.id.reciclerviewEmpleados)
        recyclerEmpleados.layoutManager = LinearLayoutManager(this)

        // Creamos la instancia al adapter y nos permitirá utilizarlo en el recycler.
        adapter = Empleado_Adpter { empleado ->
            val intent = Intent(this, activity_detalleempleado::class.java)
            intent.putExtra("id", empleado.id)
            intent.putExtra("syncId", empleado.syncId)
            intent.putExtra("serverId", empleado.serverId ?: 0)
            intent.putExtra("nombre", empleado.nombre)
            intent.putExtra("apellido", empleado.apellido)
            intent.putExtra("cedula", empleado.cedula)
            intent.putExtra("telefono", empleado.telefono)

            startActivity(intent)
        }
        recyclerEmpleados.adapter = adapter

        // Permite preguntar si desde el ViewModel nos envia Productos (fuente local, Room)
        viewModel.Empleados.observe(this) { empleados ->
            adapter.submitList(empleados)
        }

        // GET remoto (API -> SQL Server)
        apiRepository = Empleado_ApiRepository(RetrofitClient.create(this))
        apiViewModel = Empleado_ApiViewModel(apiRepository)

        // Realizamos un evento, al hacer click en el botón agregar se nos abrirá la pantalla detalleProducto
        val newEmpleado = findViewById<FloatingActionButton>(R.id.FloatingActionButtonAgregar)
        newEmpleado.setOnClickListener {
            val intent = Intent(this, activity_agregarempleado::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarEmpleadosRemotos()
    }

    private fun cargarEmpleadosRemotos() {

        if (NetworkUtils.hayInternet(this)) {
            apiViewModel.obtenerEmpleados { listaDto ->
                val listaEmpleado = listaDto.map { it.toEntity() }

                lifecycleScope.launch {
                    listaEmpleado.forEach { empleado ->
                        viewModel.sincronizarDesdeAzure(empleado)
                    }

                    viewModel.eliminarEmpleadosQueYaNoExistenEnAzure(listaEmpleado)
                    sincronizarPendientes {}
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

            // EMPLEADOS PENDIENTES DE CREAR

            val pendientesCrear =
                viewModel.obtenerPendientesCrear()

            Log.d(
                "SYNC_DEBUG",
                "Pendientes a crear: ${pendientesCrear.size}"
            )

            for (empleado in pendientesCrear) {

                try {
                    val serverId = if (empleado.serverId == null) {
                        val respuesta = apiRepository.guardarEmpleado(empleado.toDto())
                        respuesta.id
                            ?: throw IllegalStateException("La API no devolvió el ID del empleado.")
                    } else {
                        apiRepository.actualizarEmpleado(empleado.serverId, empleado.toDto())
                        empleado.serverId
                    }

                    viewModel.marcarComoSincronizado(
                        empleado.syncId,
                        serverId
                    )

                    Log.d(
                        "SYNC_DEBUG",
                        "Sincronizado con éxito: ${empleado.nombre}"
                    )

                } catch (e: retrofit2.HttpException) {
                    val cuerpo = e.response()?.errorBody()?.string()
                    Log.e("SYNC", "HTTP ${e.code()} -> $cuerpo")
                }
            }

            // CLIENTES PENDIENTES DE ELIMINAR

            val pendientesEliminar =
                viewModel.obtenerPendientesEliminar()

            Log.d(
                "SYNC_DEBUG",
                "Pendientes a eliminar: ${pendientesEliminar.size}"
            )

            for (empleado in pendientesEliminar) {

                try {

                    val serverId = empleado.serverId

                    if (serverId == null) {
                        // Nunca llegó a Azure, solamente se elimina localmente
                        viewModel.eliminarPorId(empleado.id)

                        Log.d(
                            "SYNC_DEBUG",
                            "Eliminado solamente de Room: ${empleado.nombre}"
                        )

                        continue
                    }

                    // Eliminar usando el ID de Azure
                    apiRepository.eliminarEmpleado(serverId)

                    // Después de confirmar el borrado remoto, eliminamos el registro local
                    viewModel.eliminarPorId(empleado.id)

                    Log.d(
                        "SYNC_DEBUG",
                        "Eliminado con éxito: ${empleado.nombre}"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "SYNC_DEBUG",
                        "Error al eliminar ${empleado.nombre}: ${e.message}",
                        e
                    )
                }
            }

            onFinish()
        }


    }

}