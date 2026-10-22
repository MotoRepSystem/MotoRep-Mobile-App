package com.example.motorep_mobile_app.ui.operacional.catalogos.proveedores

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.entity.Proveedor
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Cliente_ApiRepository
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.data.repository.Proveedor_ApiRepository
import com.example.motorep_mobile_app.data.repository.Proveedor_Repository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.ui.operacional.catalogos.clientes.activity_editarcliente
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.example.motorep_mobile_app.viewmodel.Proveedor_Viewmodel
import kotlinx.coroutines.launch
import retrofit2.HttpException

class activity_detalleproveedores : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalleproveedores)

        MenuLateral(this).configurar()

        // Obtenemos los datos enviados desde la pantalla anterior
        val id = intent.getIntExtra("id", -1)
        val syncId = intent.getStringExtra("syncId") ?: ""
        val serverId = intent.getIntExtra("serverId", 0)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val telefono = intent.getStringExtra("telefono") ?: ""

        // Vinculamos los elementos del diseño (XML) con el código
        val txtNombre = findViewById<TextView>(R.id.tvNombreProveedor)
        val txtTelefono = findViewById<TextView>(R.id.tvTelefono)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)

        //Mostramos los datos en pantalla
        txtNombre.text = nombre
        txtTelefono.text = telefono

        val btnEditar = findViewById<Button>(R.id.btnEditar)
        btnEditar.setOnClickListener {
            val intent = Intent(this, activity_editarproveedor::class.java)
            intent.putExtra("id", id)
            intent.putExtra("syncId", syncId)
            intent.putExtra("serverId", serverId)
            intent.putExtra("nombre", nombre)
            intent.putExtra("telefono", telefono)

            startActivity(intent)
        }

        // Configuramos el ViewModel para acceder a la base de datos
        val database = Motorep_Database.getDatabase(applicationContext)
        val dao = database.proveedorDao()
        val repository = Proveedor_Repository(dao)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(Proveedor_Viewmodel::class.java)) {
                    return Proveedor_Viewmodel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        val viewModel = ViewModelProvider(this, factory)[Proveedor_Viewmodel::class.java]

        // Programamos el evento clic del botón Eliminar
        btnEliminar.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Eliminar proveedor")
                .setMessage("¿Desea eliminar definitivamente este proveedor?")
                .setPositiveButton("Sí") { _, _ ->

                    lifecycleScope.launch {

                        val hayInternet =
                            NetworkUtils.hayInternet(this@activity_detalleproveedores)

                        if (hayInternet) {

                            try {

                                // API de Azure
                                val apiRepository =
                                    Proveedor_ApiRepository(
                                        RetrofitClient.create(
                                            this@activity_detalleproveedores
                                        )
                                    )

                                // Primero eliminamos de Azure
                                if (serverId != 0) {

                                    apiRepository.eliminarProveedor(serverId)

                                }

                                // Si Azure confirmó el borrado,
                                // eliminamos también de Room.
                                viewModel.eliminarPorId(id)

                                Toast.makeText(
                                    this@activity_detalleproveedores,
                                    "Proveedor eliminado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                                finish()

                            } catch (e: Exception) {
                                if (e is HttpException) {
                                    val body = e.response()?.errorBody()?.string()
                                    Log.e(
                                        "ProveedorDelete",
                                        "API rechazó la eliminación (HTTP ${e.code()}): $body",
                                        e
                                    )
                                } else {
                                    Log.e(
                                        "ProveedorDelete",
                                        "No se pudo eliminar el proveedor de la API",
                                        e
                                    )
                                }
                                Toast.makeText(
                                    this@activity_detalleproveedores,
                                    "No se pudo eliminar el proveedor de Azure.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } else {

                            // Sin Internet:
                            // lo marcamos como pendiente de eliminación.
                            val proveedorPendienteEliminar =
                                Proveedor(
                                    id = id,
                                    syncId = syncId,
                                    serverId =
                                        if (serverId == 0) null
                                        else serverId,
                                    nombre = nombre,
                                    telefono = telefono,
                                    pendienteEliminar = true
                                )

                            viewModel.actualizarProveedor(
                                proveedorPendienteEliminar
                            )

                            Toast.makeText(
                                this@activity_detalleproveedores,
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