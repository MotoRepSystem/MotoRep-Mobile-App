package com.example.motorep_mobile_app.ui.operacional.catalogos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motorep_mobile_app.Cliente_Adapter
import com.example.motorep_mobile_app.MainActivity
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.activity_agregarcliente
import com.example.motorep_mobile_app.activity_detallecliente
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_facturacion
import com.example.motorep_mobile_app.ui.reportes.activity_incidencias
import com.example.motorep_mobile_app.ui.reportes.activity_indicadores
import com.example.motorep_mobile_app.ui.reportes.activity_tendencias
import com.example.motorep_mobile_app.ui.dashboard.activity_dashboard
import com.google.android.material.navigation.NavigationView
import com.example.motorep_mobile_app.data.database.MotorepDatabase
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.motorep_mobile_app.data.remote.dto.toDto
import com.example.motorep_mobile_app.data.remote.dto.toEntity
import com.example.motorep_mobile_app.data.repository.Cliente_ApiRepository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.viewmodel.Cliente_ApiViewModel
import kotlinx.coroutines.launch

class activity_clientes : AppCompatActivity() {


    private lateinit var apiViewModel: Cliente_ApiViewModel
    private lateinit var apiRepository: Cliente_ApiRepository // NUEVO: necesario para sincronizarPendientes()
    private lateinit var viewModel: Cliente_Viewmodel
    private lateinit var adapter: Cliente_Adapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clientes)


        MenuLateral(this).configurar()


        val database = MotorepDatabase.getDatabase(this)
        val dao = database.clienteDao()
        val repository = Cliente_Repository(dao)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
               if (modelClass.isAssignableFrom(Cliente_Viewmodel::class.java)) {
                   return Cliente_Viewmodel(repository) as T
               }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        viewModel = ViewModelProvider(this, factory)[Cliente_Viewmodel::class.java]


        //accedemos al recicler view
        val reciclerViewCliente = findViewById<RecyclerView>(R.id.reciclerviewClientes)
        reciclerViewCliente.layoutManager = LinearLayoutManager(this)



        //cramos el adapter y usarlo en el recicler
        //val adapter = Cliente_Adapter()
        //reciclerViewCliente.adapter = adapter

        val adapter = Cliente_Adapter{cliente -> val intent = Intent(this, activity_detallecliente::class.java)
            intent.putExtra("id", cliente.id)
            intent.putExtra("nombre", cliente.nombre)
            intent.putExtra("cedula", cliente.cedula)
            intent.putExtra("telefono", cliente.telefono)
            startActivity(intent)
        }
        reciclerViewCliente.adapter = adapter

        //observamos los cambios en la lista de clientes y actualizamos el adapter
        viewModel.cliente.observe(this) {
            cliente -> adapter.submitList(cliente)
        }

        //boton flotante para agregar cliente
        val btnAgregar = findViewById<FloatingActionButton>(R.id.FloatingActionButtonAgregar)
        btnAgregar.setOnClickListener {
            val intent = Intent(this, activity_agregarcliente::class.java)
            startActivity(intent)
        }




        // GET remoto (API -> SQL Server)
        apiRepository = Cliente_ApiRepository(RetrofitClient.create(this))
        apiViewModel = Cliente_ApiViewModel(apiRepository)

        /*apiViewModel.obtenerClientes { listDto ->
            val listaClientes = listDto.map { dto ->
                Cliente(
                    id = dto.id,
                    nombre = dto.Nombres ?: "",
                    apellido = dto.Apellidos ?: "",
                    cedula = dto.NumCedula ?: "",
                    telefono = dto.NumTelefono ?: ""
                )
            }
            adapter.submitList(listaClientes)
        }*/


    }
    override fun onResume() {
        super.onResume()
        cargarClientesRemotos()
    }

    private fun cargarClientesRemotos() {

        if (NetworkUtils.hayInternet(this)) {
            sincronizarPendientes {
                apiViewModel.obtenerClientes { listaDto ->
                    val listaClientes =
                        listaDto.map { it.toEntity() }
                    lifecycleScope.launch {
                        listaClientes.forEach { cliente ->

                            viewModel.sincronizarDesdeAzure(cliente)
                        }
                        viewModel.eliminarClientesQueYaNoExistenEnAzure(
                            listaClientes
                        )
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
            // CLIENTES PENDIENTES DE CREAR
            val pendientesCrear =
                viewModel.obtenerPendientesCrear()
            android.util.Log.d(
                "SYNC_DEBUG",
                "Pendientes a crear: ${pendientesCrear.size}"
            )
            for (cliente in pendientesCrear) {

                try {
                    val respuesta =
                        apiRepository.guardarCliente(
                            cliente.toDto()
                        )
                    val serverId = respuesta.id
                    if (serverId == null) {
                        throw Exception(
                            "Azure no devolvió el ID del producto."
                        )
                    }

                    // Guardamos el ID de Azure y marcamos como sincronizado
                    viewModel.marcarComoSincronizado(
                        cliente.syncId,
                        serverId
                    )
                    android.util.Log.d(
                        "SYNC_DEBUG",
                        "Sincronizado con éxito: ${cliente.nombre}"
                    )
                } catch (e: Exception) {
                    android.util.Log.e(
                        "SYNC_DEBUG",
                        "Error al sincronizar ${cliente.nombre}: ${e.message}"
                    )
                }
            }

            // PRODUCTOS PENDIENTES DE ELIMINAR
            val pendientesEliminar =
                viewModel.obtenerPendientesEliminar()
            android.util.Log.d(
                "SYNC_DEBUG",
                "Pendientes a eliminar: ${pendientesEliminar.size}"
            )

            for (producto in pendientesEliminar) {
                try {
                    val serverId = producto.serverId
                    if (serverId == null) {
                        // Nunca llegó a Azure, solamente se elimina localmente
                        viewModel.eliminarPorId(producto.id)
                        android.util.Log.d(
                            "SYNC_DEBUG",
                            "Eliminado solamente de Room: ${producto.nombre}"
                        )
                        continue
                    }

                    // Eliminar usando el ID de Azure
                    apiRepository.eliminarCliente(serverId)
                    // Después de confirmar el borrado remoto, eliminamos el registro local
                    viewModel.eliminarPorId(producto.id)
                    android.util.Log.d(
                        "SYNC_DEBUG",
                        "Eliminado con éxito: ${producto.nombre}"
                    )

                } catch (e: Exception) {

                    android.util.Log.e(
                        "SYNC_DEBUG",
                        "Error al eliminar ${producto.nombre}: ${e.message}"
                    )
                }
            }

            onFinish()
        }
    }

}