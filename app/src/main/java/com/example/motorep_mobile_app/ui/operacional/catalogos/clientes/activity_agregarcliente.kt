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
import com.example.motorep_mobile_app.data.remote.dto.toDto
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
            Log.d("ClienteCreate", "Solicitud de guardar cliente recibida")

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

                Log.w("ClienteCreate", "Alta detenida: hay campos obligatorios vacíos")
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
            Log.d("ClienteCreate", "Conectividad disponible: $hayInternet")


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
                    Log.d("ClienteCreate", "Cliente guardado localmente")
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
                        Log.d("ClienteCreate", "Enviando cliente a la API")
                        val respuesta = apiRepository.guardarCliente(cliente.toDto())
                        Log.d("ClienteCreate", "Respuesta API recibida; serverId=${respuesta.id}")
                        val serverId = respuesta.id
                            ?: throw IllegalStateException("La API no devolvió el ID del cliente creado ($respuesta).")

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
                } else {
                    Log.i("ClienteCreate", "Sin conexión: se conserva el cliente pendiente de sincronización")
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