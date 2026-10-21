package com.example.motorep_mobile_app.ui.operacional.catalogos.clientes

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
import com.example.motorep_mobile_app.Cliente_Adapter
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.remote.dto.toDto
import com.example.motorep_mobile_app.data.remote.dto.toEntity
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Cliente_ApiRepository
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.viewmodel.Cliente_ApiViewModel
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class activity_clientes : AppCompatActivity() {

    // Las guardamos como propiedades de la clase para poder reutilizarlas en onResume() sin volver a crearlas
    private lateinit var apiViewModel: Cliente_ApiViewModel
    private lateinit var apiRepository: Cliente_ApiRepository

    // NUEVO: necesario para sincronizarPendientes()
    private lateinit var viewModel: Cliente_Viewmodel
    private lateinit var adapter: Cliente_Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clientes)

        MenuLateral(this).configurar()

        // Nos permite recrear y traer el flujo de la base de datos para poder utilizarlo aquí
        val database = Motorep_Database.getDatabase(applicationContext)
        val dao = database.clienteDao()
        val repository = Cliente_Repository(dao)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                if (modelClass.isAssignableFrom(Cliente_Viewmodel::class.java)) {
                    return Cliente_Viewmodel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(
            this,
            factory
        )[Cliente_Viewmodel::class.java]

        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

        // Accedemos al recyclerView
        val recyclerCliente = findViewById<RecyclerView>(R.id.reciclerviewClientes)
        recyclerCliente.layoutManager = LinearLayoutManager(this)

        // Creamos la instancia al adapter y nos permitirá utilizarlo en el recycler.
        adapter = Cliente_Adapter { cliente ->
            val intent = Intent(this, activity_detallecliente::class.java)
            intent.putExtra("id", cliente.id)
            intent.putExtra("syncId", cliente.syncId)
            intent.putExtra("serverId", cliente.serverId ?: 0)
            intent.putExtra("nombre", cliente.nombre)
            intent.putExtra("apellido", cliente.apellido)
            intent.putExtra("cedula", cliente.cedula)
            intent.putExtra("telefono", cliente.telefono)

            startActivity(intent)
        }
        recyclerCliente.adapter = adapter

        // Permite preguntar si desde el ViewModel nos envia Productos (fuente local, Room)
        viewModel.clientes.observe(this) { clientes ->
            adapter.submitList(clientes)
        }

        // GET remoto (API -> SQL Server)
        apiRepository = Cliente_ApiRepository(RetrofitClient.create(this))
        apiViewModel = Cliente_ApiViewModel(apiRepository)

        // Realizamos un evento, al hacer click en el botón agregar se nos abrirá la pantalla detalleProducto
        val newCliente = findViewById<FloatingActionButton>(R.id.FloatingActionButtonAgregar)
        newCliente.setOnClickListener {
            val intent = Intent(this, activity_agregarcliente::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarClientesRemotos()
    }

    private fun cargarClientesRemotos() {

        if (NetworkUtils.hayInternet(this)) {
            apiViewModel.obtenerClientes { listaDto ->
                val listaCliente = listaDto.map { it.toEntity() }

                lifecycleScope.launch {
                    listaCliente.forEach { cliente ->
                        viewModel.sincronizarDesdeAzure(cliente)
                    }

                    viewModel.eliminarClientesQueYaNoExistenEnAzure(listaCliente)
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

            // PRODUCTOS PENDIENTES DE CREAR

            val pendientesCrear =
                viewModel.obtenerPendientesCrear()

            Log.d(
                "SYNC_DEBUG",
                "Pendientes a crear: ${pendientesCrear.size}"
            )

            for (cliente in pendientesCrear) {

                try {
                    val serverId = if (cliente.serverId == null) {
                        val respuesta = apiRepository.guardarCliente(cliente.toDto())
                        respuesta.id
                            ?: throw IllegalStateException("La API no devolvió el ID del cliente.")
                    } else {
                        apiRepository.actualizarCliente(cliente.serverId, cliente.toDto())
                        cliente.serverId
                    }

                    viewModel.marcarComoSincronizado(
                        cliente.syncId,
                        serverId
                    )

                    Log.d(
                        "SYNC_DEBUG",
                        "Sincronizado con éxito: ${cliente.nombre}"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "SYNC_DEBUG",
                        "Error al sincronizar ${cliente.nombre}: ${e.message}",
                        e
                    )
                }
            }

            // CLIENTES PENDIENTES DE ELIMINAR

            val pendientesEliminar =
                viewModel.obtenerPendientesEliminar()

            Log.d(
                "SYNC_DEBUG",
                "Pendientes a eliminar: ${pendientesEliminar.size}"
            )

            for (cliente in pendientesEliminar) {

                try {

                    val serverId = cliente.serverId

                    if (serverId == null) {
                        // Nunca llegó a Azure, solamente se elimina localmente
                        viewModel.eliminarPorId(cliente.id)

                        Log.d(
                            "SYNC_DEBUG",
                            "Eliminado solamente de Room: ${cliente.nombre}"
                        )

                        continue
                    }

                    // Eliminar usando el ID de Azure
                    apiRepository.eliminarCliente(serverId)

                    // Después de confirmar el borrado remoto, eliminamos el registro local
                    viewModel.eliminarPorId(cliente.id)

                    Log.d(
                        "SYNC_DEBUG",
                        "Eliminado con éxito: ${cliente.nombre}"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "SYNC_DEBUG",
                        "Error al eliminar ${cliente.nombre}: ${e.message}",
                        e
                    )
                }
            }

            onFinish()
        }

    }

}