package com.example.motorep_mobile_app.ui.operacional.catalogos.proveedores

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
import com.example.motorep_mobile_app.Proveedor_Adapter
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.remote.dto.toDto
import com.example.motorep_mobile_app.data.remote.dto.toEntity
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Cliente_ApiRepository
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.data.repository.Proveedor_ApiRepository
import com.example.motorep_mobile_app.data.repository.Proveedor_Repository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.ui.operacional.catalogos.clientes.activity_agregarcliente
import com.example.motorep_mobile_app.ui.operacional.catalogos.clientes.activity_detallecliente
import com.example.motorep_mobile_app.viewmodel.Cliente_ApiViewModel
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.example.motorep_mobile_app.viewmodel.Proveedor_ApiViewModel
import com.example.motorep_mobile_app.viewmodel.Proveedor_Viewmodel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class activity_proveedores : AppCompatActivity() {

    // Las guardamos como propiedades de la clase para poder reutilizarlas en onResume() sin volver a crearlas
    private lateinit var apiViewModel: Proveedor_ApiViewModel
    private lateinit var apiRepository: Proveedor_ApiRepository

    // NUEVO: necesario para sincronizarPendientes()
    private lateinit var viewModel: Proveedor_Viewmodel
    private lateinit var adapter: Proveedor_Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_proveedores)

        MenuLateral(this).configurar()

        // Nos permite recrear y traer el flujo de la base de datos para poder utilizarlo aquí
        val database = Motorep_Database.getDatabase(applicationContext)
        val dao = database.proveedorDao()
        val repository = Proveedor_Repository(dao)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                if (modelClass.isAssignableFrom(Proveedor_Viewmodel::class.java)) {
                    return Proveedor_Viewmodel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(
            this,
            factory
        )[Proveedor_Viewmodel::class.java]


        // Accedemos al recyclerView
        val recyclerProveedor = findViewById<RecyclerView>(R.id.reciclerviewProveedores)
        recyclerProveedor.layoutManager = LinearLayoutManager(this)

        // Creamos la instancia al adapter y nos permitirá utilizarlo en el recycler.
        adapter = Proveedor_Adapter { proveedor ->
            val intent = Intent(this, activity_detalleproveedores::class.java)
            intent.putExtra("id", proveedor.id)
            intent.putExtra("syncId", proveedor.syncId)
            intent.putExtra("serverId", proveedor.serverId ?: 0)
            intent.putExtra("nombre", proveedor.nombre)
            intent.putExtra("telefono", proveedor.telefono)

            startActivity(intent)
        }
        recyclerProveedor.adapter = adapter

        // Permite preguntar si desde el ViewModel nos envia Productos (fuente local, Room)
        viewModel.proveedores.observe(this) { proveedores ->
            adapter.submitList(proveedores)
        }

        // GET remoto (API -> SQL Server)
        apiRepository = Proveedor_ApiRepository(RetrofitClient.create(this))
        apiViewModel = Proveedor_ApiViewModel(apiRepository)

        // Realizamos un evento, al hacer click en el botón agregar se nos abrirá la pantalla detalleProducto
        val newProveedor = findViewById<FloatingActionButton>(R.id.FloatingActionButtonAgregar)
        newProveedor.setOnClickListener {
            val intent = Intent(this, activity_agregarproveedor::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarProveedoresRemotos()
    }

    private fun cargarProveedoresRemotos() {

        if (NetworkUtils.hayInternet(this)) {
            apiViewModel.obtenerProveedores { listaDto ->
                val listaProveedor = listaDto.map { it.toEntity() }

                lifecycleScope.launch {
                    listaProveedor.forEach { proveedor ->
                        viewModel.sincronizarDesdeAzure(proveedor)
                    }

                    viewModel.eliminarProveedoresQueYaNoExistenEnAzure(listaProveedor)
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

            // PROVEEDORES PENDIENTES DE CREAR

            val pendientesCrear =
                viewModel.obtenerPendientesCrear()

            Log.d(
                "SYNC_DEBUG",
                "Pendientes a crear: ${pendientesCrear.size}"
            )

            for (proveedor in pendientesCrear) {

                try {
                    val serverId = if (proveedor.serverId == null) {
                        val respuesta = apiRepository.guardarProveedor(proveedor.toDto())
                        respuesta.id
                            ?: throw IllegalStateException("La API no devolvió el ID del proveedor.")
                    } else {
                        apiRepository.actualizarProveedor(proveedor.serverId, proveedor.toDto())
                        proveedor.serverId
                    }

                    viewModel.marcarComoSincronizado(
                        proveedor.syncId,
                        serverId
                    )

                    Log.d(
                        "SYNC_DEBUG",
                        "Sincronizado con éxito: ${proveedor.nombre}"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "SYNC_DEBUG",
                        "Error al sincronizar ${proveedor.nombre}: ${e.message}",
                        e
                    )
                }
            }

            // PROVEEDORES PENDIENTES DE ELIMINAR

            val pendientesEliminar =
                viewModel.obtenerPendientesEliminar()

            Log.d(
                "SYNC_DEBUG",
                "Pendientes a eliminar: ${pendientesEliminar.size}"
            )

            for (proveedor in pendientesEliminar) {

                try {

                    val serverId = proveedor.serverId

                    if (serverId == null) {
                        // Nunca llegó a Azure, solamente se elimina localmente
                        viewModel.eliminarPorId(proveedor.id)

                        Log.d(
                            "SYNC_DEBUG",
                            "Eliminado solamente de Room: ${proveedor.nombre}"
                        )

                        continue
                    }

                    // Eliminar usando el ID de Azure
                    apiRepository.eliminarProveedor(serverId)

                    // Después de confirmar el borrado remoto, eliminamos el registro local
                    viewModel.eliminarPorId(proveedor.id)

                    Log.d(
                        "SYNC_DEBUG",
                        "Eliminado con éxito: ${proveedor.nombre}"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "SYNC_DEBUG",
                        "Error al eliminar ${proveedor.nombre}: ${e.message}",
                        e
                    )
                }
            }

            onFinish()
        }

    }
}