package com.example.motorep_mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.data.database.MotorepDatabase
import com.example.motorep_mobile_app.data.remote.dto.Cliente_dto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Cliente_ApiRepository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_clientes
import kotlinx.coroutines.launch

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
            MotorepDatabase.getDatabase(applicationContext)

        val dao = database.clienteDao()

        val repository =
            Cliente_Repository(dao)

        // API de Azure
        val apiRepository =
            Cliente_ApiRepository(
                RetrofitClient.create(this)
            )

        btnguardar.setOnClickListener {

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

                    // Guardar primero en Room
                    repository.insertar(cliente)

                    if (hayInternet) {

                        // Preparamos el DTO utilizando syncId como identificador
                        val clienteDto = Cliente_dto(
                            id = null,
                            syncId = cliente.syncId,
                            nombre = cliente.nombre,
                            apellido = cliente.apellido,
                            cedula = cliente.cedula,
                            telefono = cliente.telefono
                        )

                        // Enviar a Azure
                        val respuesta =
                            apiRepository.guardarClientes(
                                clienteDto
                            )

                        // Verificar que Azure devolvió el ID
                        val serverId = respuesta.id

                        if (serverId == null) {
                            throw Exception("Azure no devolvió el ID del producto.")
                        }

                        // Guardar el ID de Azure y marcar como sincronizado
                        repository.marcarComoSincronizado(
                            cliente.syncId,
                            serverId
                        )

                        Toast.makeText(
                            this@activity_agregarcliente,
                            "Producto guardado y sincronizado correctamente.",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        Toast.makeText(
                            this@activity_agregarcliente,
                            "Sin conexión: el producto se guardó localmente y quedará pendiente de sincronización.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    // Limpiar campos
                    edtNombre.text.clear()
                    edtApellido.text.clear()
                    edtCedula.text.clear()
                    edtTelefono.text.clear()

                    // Regresar al módulo de productos
                    val intent =
                        Intent(
                            this@activity_agregarcliente,
                            activity_clientes::class.java
                        )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP

                    startActivity(intent)
                    finish()

                } catch (e: Exception) {


                    // Si Azure falla, el producto YA está en Room con sincronizado = false, por lo tanto NO se pierde.
                    //Posteriormente WorkManager podrá enviarlo.

                    Toast.makeText(
                        this@activity_agregarcliente,
                        "Cliente guardado localmente. Pendiente de sincronización.",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent =
                        Intent(
                            this@activity_agregarcliente,
                            activity_clientes::class.java
                        )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP

                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}