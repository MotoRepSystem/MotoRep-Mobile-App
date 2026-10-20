package com.example.motorep_mobile_app.ui.operacional.catalogos.empleados

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.entity.Empleado
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Empleado_ApiRepository
import com.example.motorep_mobile_app.data.repository.Empleado_Repository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.viewmodel.Empleado_ViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException

class activity_detalleempleado : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalleempleado)


        MenuLateral(this).configurar()

        // Obtenemos los datos enviados desde la pantalla anterior
        val id = intent.getIntExtra("id", -1)
        val syncId = intent.getStringExtra("syncId") ?: ""
        val serverId = intent.getIntExtra("serverId", 0)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val apellido = intent.getStringExtra("apellido") ?: ""
        val cedula = intent.getStringExtra("cedula") ?: ""
        val telefono = intent.getStringExtra("telefono") ?: ""

        // Vinculamos los elementos del diseño (XML) con el código
        val txtNombre = findViewById<TextView>(R.id.tvNombreEmpleado)
        val txtApellido = findViewById<TextView>(R.id.tvApellido)
        val txtCedula = findViewById<TextView>(R.id.tvCedula)
        val txtTelefono = findViewById<TextView>(R.id.tvTelefono)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)

        //Mostramos los datos en pantalla
        txtNombre.text = nombre
        txtApellido.text = apellido
        txtCedula.text = cedula
        txtTelefono.text = telefono

        val btnEditar = findViewById<Button>(R.id.btnEditar)
        btnEditar.setOnClickListener {
            val intent = Intent(this, activity_editarempleado::class.java)
            intent.putExtra("id", id)
            intent.putExtra("syncId", syncId)
            intent.putExtra("serverId", serverId)
            intent.putExtra("nombre", nombre)
            intent.putExtra("apellido", apellido)
            intent.putExtra("cedula", cedula)
            intent.putExtra("telefono", telefono)

            startActivity(intent)
        }

        // Configuramos el ViewModel para acceder a la base de datos
        val database = Motorep_Database.getDatabase(applicationContext)
        val dao = database.empleadoDao()
        val repository = Empleado_Repository(dao)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(Empleado_ViewModel::class.java)) {
                    return Empleado_ViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        val viewModel = ViewModelProvider(this, factory)[Empleado_ViewModel::class.java]

        // Programamos el evento clic del botón Eliminar
        btnEliminar.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Eliminar empleado")
                .setMessage("¿Desea eliminar definitivamente este empleado?")
                .setPositiveButton("Sí") { _, _ ->

                    lifecycleScope.launch {

                        val hayInternet =
                            NetworkUtils.hayInternet(this@activity_detalleempleado)

                        if (hayInternet) {

                            try {

                                // API de Azure
                                val apiRepository =
                                    Empleado_ApiRepository(
                                        RetrofitClient.create(
                                            this@activity_detalleempleado
                                        )
                                    )

                                // Primero eliminamos de Azure
                                if (serverId != 0) {

                                    apiRepository.eliminarEmpleado(serverId)

                                }

                                // Si Azure confirmó el borrado,
                                // eliminamos también de Room.
                                viewModel.eliminarPorId(id)

                                Toast.makeText(
                                    this@activity_detalleempleado,
                                    "Empleado eliminado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                                finish()

                            } catch (e: Exception) {
                                if (e is HttpException) {
                                    val body = e.response()?.errorBody()?.string()
                                    Log.e(
                                        "EmpleadoDelete",
                                        "API rechazó la eliminación (HTTP ${e.code()}): $body",
                                        e
                                    )
                                } else {
                                    Log.e(
                                        "EmpleadoDelete",
                                        "No se pudo eliminar el cliente de la API",
                                        e
                                    )
                                }
                                Toast.makeText(
                                    this@activity_detalleempleado,
                                    "No se pudo eliminar el cliente de Azure.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } else {

                            // Sin Internet:
                            // lo marcamos como pendiente de eliminación.
                            val empleadoPendienteEliminar =
                                Empleado(
                                    id = id,
                                    syncId = syncId,
                                    serverId =
                                        if (serverId == 0) null
                                        else serverId,
                                    nombre = nombre,
                                    apellido = apellido,
                                    cedula = cedula,
                                    telefono = telefono,
                                    pendienteEliminar = true
                                )

                            viewModel.actualizarEmpleado(
                                empleadoPendienteEliminar
                            )

                            Toast.makeText(
                                this@activity_detalleempleado,
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