package com.example.motorep_mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.motorep_mobile_app.data.database.MotorepDatabase
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Cliente_ApiRepository
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.ui.dashboard.activity_dashboard
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_clientes
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_empleados
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_inventario
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_proveedores
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_marcas
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_facturacion
import com.example.motorep_mobile_app.ui.reportes.activity_incidencias
import com.example.motorep_mobile_app.ui.reportes.activity_indicadores
import com.example.motorep_mobile_app.ui.reportes.activity_tendencias
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class activity_detallecliente : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detallecliente)


        MenuLateral(this).configurar()


        // Paso 1: Obtenemos los datos enviados desde la pantalla anterior
        val id = intent.getIntExtra("id", -1)
        val syncId = intent.getStringExtra("syncId") ?: ""
        val serverId = intent.getIntExtra("serverId", 0)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val apellido = intent.getStringExtra("apellido") ?: ""
        val cedula = intent.getStringExtra("cedula") ?: ""
        val telefono = intent.getStringExtra("telefono") ?: ""

        // Paso 2: Vinculamos los elementos del diseño (XML) con el código
        val txtNombre = findViewById<TextView>(R.id.tvNombreCliente)
        val txtApellido = findViewById<TextView>(R.id.tvApellido)
        val txtCedula = findViewById<TextView>(R.id.tvCedula)
        val txtTelefono = findViewById<TextView>(R.id.tvTelefono)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)


        // Paso 3: Mostramos los datos en pantalla
        txtNombre.text = nombre
        txtApellido.text = apellido
        txtCedula.text = cedula
        txtTelefono.text = telefono

        val btnEditar = findViewById<Button>(R.id.btnEditar)
        btnEditar.setOnClickListener {
            val intent = Intent(this, activity_editarcliente::class.java)
            intent.putExtra("id", id)
            intent.putExtra("nombre", nombre)
            intent.putExtra("apellido", apellido)
            intent.putExtra("cedula", cedula)
            intent.putExtra("telefono", telefono)

            startActivity(intent)
        }

        // Paso 4: Configuramos el ViewModel para acceder a la base de datos
        val database = MotorepDatabase.getDatabase(applicationContext)
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
        val viewModel = ViewModelProvider(this, factory)[Cliente_Viewmodel::class.java]

        // Programamos el evento clic del botón Eliminar
        btnEliminar.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Eliminar cliente")
                .setMessage("¿Desea eliminar definitivamente este cliente?")
                .setPositiveButton("Sí") { _, _ ->

                    lifecycleScope.launch {

                        val hayInternet =
                            NetworkUtils.hayInternet(this@activity_detallecliente)
                        if (hayInternet) {
                            try {
                                // API
                                val apiRepository =
                                    Cliente_ApiRepository(
                                        RetrofitClient.create(
                                            this@activity_detallecliente
                                        )
                                    )
                                // Primero eliminamos de api
                                if (serverId != 0) { apiRepository.eliminarCliente(serverId) }

                                // Si Azure confirmó el borrado,
                                // eliminamos también de Room.
                                viewModel.eliminarPorId(id)

                                Toast.makeText(
                                    this@activity_detallecliente,
                                    "Cliente eliminado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                                finish()

                            } catch (e: Exception) {

                                Toast.makeText(
                                    this@activity_detallecliente,
                                    "No se pudo eliminar el cliente de Azure.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } else {

                            // Sin Internet:
                            // lo marcamos como pendiente de eliminación.
                            val clientePendienteEliminar =
                                Cliente(
                                    id = id,
                                    serverId =
                                        if (serverId == 0) null
                                        else serverId,
                                    nombre = nombre,
                                    apellido = apellido,
                                    cedula = cedula,
                                    telefono = telefono,
                                    pendienteEliminar = true
                                )

                            viewModel.actualizarCliente(
                                clientePendienteEliminar
                            )

                            Toast.makeText(
                                this@activity_detallecliente,
                                "Sin conexión: se eliminará cuando vuelva Internet.",
                                Toast.LENGTH_LONG
                            ).show()

                            finish()
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }


    }
}