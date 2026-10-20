package com.example.motorep_mobile_app.ui.operacional.catalogos.clientes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.remote.dto.Cliente_dto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Cliente_ApiRepository
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.ui.common.MenuLateral
import kotlinx.coroutines.launch
import retrofit2.HttpException

class activity_agregarcliente : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregarcliente)

        MenuLateral(this).configurar()

        val btnguardar =
            findViewById<Button>(R.id.btnGuardarCliente)

        val edtNombre =
            findViewById<EditText>(R.id.edtNombre)

        val edtApellido =
            findViewById<EditText>(R.id.edtApellido)

        val edtCedula =
            findViewById<EditText>(R.id.edtCedula)

        val edtTelefono =
            findViewById<EditText>(R.id.edtTelefono)



        // Base de datos local Room
        val database =
            Motorep_Database.getDatabase(applicationContext)

        val dao = database.clienteDao()

        val repository =
            Cliente_Repository(dao)

        // API de Azure
        val apiRepository =
            Cliente_ApiRepository(
                RetrofitClient.create(this)
            )

        btnguardar.setOnClickListener {
            btnguardar.isEnabled = false

            val nombre =
                edtNombre.text.toString().trim()

            val apellido =
                edtApellido.text.toString().trim()

            val cedula =
                edtCedula.text.toString().trim()

            val telefono =
                edtTelefono.text.toString().trim()


            // Validaciones básicas
            if (nombre.isEmpty() ||
                apellido.isEmpty() ||
                cedula.isEmpty() ||
                telefono.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Completa todos los campos.",
                    Toast.LENGTH_SHORT
                ).show()

                btnguardar.isEnabled = true
                return@setOnClickListener
            }


            val hayInternet =
                NetworkUtils.hayInternet(this)


            // El producto siempre se crea primero en Room con sincronizado = false
            // Solo después de recibir confirmación de Azure lo marcamos como sincronizado.

            val cliente = Cliente(
                nombre = nombre,
                apellido = apellido,
                cedula = cedula,
                telefono = telefono,
                sincronizado = false
            )

            lifecycleScope.launch {
                try {
                    repository.insertar(cliente)
                } catch (e: Exception) {
                    Log.e("ClienteCreate", "No se pudo guardar el cliente en Room", e)
                    Toast.makeText(
                        this@activity_agregarcliente,
                        "No se pudo guardar el cliente.",
                        Toast.LENGTH_LONG
                    ).show()
                    btnguardar.isEnabled = true
                    return@launch
                }

                var sincronizado = false
                if (hayInternet) {
                    try {
                        val respuesta = apiRepository.guardarCliente(
                            Cliente_dto(
                                id = null,
                                syncId = cliente.syncId,
                                nombre = cliente.nombre,
                                apellido = cliente.apellido,
                                cedula = cliente.cedula,
                                telefono = cliente.telefono
                            )
                        )
                        val serverId = respuesta.id
                            ?: throw IllegalStateException("La API no devolvió el ID del cliente creado.")

                        repository.marcarComoSincronizado(cliente.syncId, serverId)
                        sincronizado = true
                    } catch (e: Exception) {
                        if (e is HttpException) {
                            val body = e.response()?.errorBody()?.string()
                            Log.e(
                                "ClienteCreate",
                                "API rechazó la creación (HTTP ${e.code()}): $body",
                                e
                            )
                        } else {
                            Log.e("ClienteCreate", "No se pudo sincronizar el cliente con la API", e)
                        }
                    }
                }

                val mensaje = when {
                    sincronizado -> "Cliente guardado y sincronizado correctamente."
                    hayInternet -> "Cliente guardado localmente. La sincronización con la API quedó pendiente."
                    else -> "Sin conexión: cliente guardado localmente y pendiente de sincronización."
                }
                Toast.makeText(this@activity_agregarcliente, mensaje, Toast.LENGTH_LONG).show()

                val intent = Intent(this@activity_agregarcliente, activity_clientes::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }
    }
}