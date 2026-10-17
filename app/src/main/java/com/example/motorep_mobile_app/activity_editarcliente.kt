package com.example.motorep_mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.motorep_mobile_app.data.database.MotorepDatabase
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.remote.dto.Cliente_dto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.example.motorep_mobile_app.databinding.ActivityEditarclienteBinding
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_clientes
import kotlinx.coroutines.launch

class activity_editarcliente : AppCompatActivity() {




    private lateinit var binding: ActivityEditarclienteBinding
    private lateinit var viewModel: Cliente_Viewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityEditarclienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MenuLateral(this).configurar()

        // Inicializar Room
        val database = MotorepDatabase.getDatabase(applicationContext)

        val dao = database.clienteDao()

        val repository = Cliente_Repository(dao)

        val factory =
            object : ViewModelProvider.Factory {

                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {

                    if (
                        modelClass.isAssignableFrom(
                            Cliente_Viewmodel::class.java
                        )
                    ) {
                        return Cliente_Viewmodel(repository) as T
                    }

                    throw IllegalArgumentException(
                        "Unknown ViewModel class"
                    )
                }
            }

        viewModel = ViewModelProvider(this, factory) [Cliente_Viewmodel::class.java]

        // Recuperar información del producto
        val id =
            intent.getIntExtra("id", 0)

        val syncId =
            intent.getStringExtra("syncId") ?: ""

        val nombre =
            intent.getStringExtra("nombre") ?: ""

        val serverId =
            intent.getIntExtra("serverId", 0)
        android.util.Log.d(
            "EDITAR_DEBUG",
            "id=$id | syncId='$syncId' | serverId=$serverId | nombre=$nombre")

        val apellido =
            intent.getStringExtra("apellido") ?: ""

        val cedula =
            intent.getStringExtra("cedula") ?: ""

        val telefono =
            intent.getStringExtra("telefono") ?: ""



        // Mostrar datos actuales
        binding.edtNombre.setText(nombre)
        binding.edtApellido.setText(apellido)
        binding.edtCedula.setText(cedula)
        binding.edtTelefono.setText(telefono)

        // Actualizar
        binding.btnActualizarCliente.setOnClickListener {

            val nuevoNombre =
                binding.edtNombre.text
                    .toString()
                    .trim()

            val nuevaApellido =
                binding.edtApellido.text
                    .toString()
                    .trim()

            val nuevoCedula =
                binding.edtCedula.text
                    .toString()
                    .trim()

            val nuevoTelefono =
                binding.edtTelefono.text
                    .toString()
                    .trim()

            // Validación
            if (
                nuevoNombre.isEmpty() ||
                nuevaApellido.isEmpty() ||
                nuevoCedula.isEmpty() ||
                nuevoTelefono.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Por favor llena todos los campos",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }


            // Crear producto actualizado para Room
            val clienteActualizado =
                Cliente(
                    id = id,
                    syncId = syncId,
                    serverId =
                        if (serverId == 0) null
                        else serverId,
                    nombre = nuevoNombre,
                    apellido = nuevaApellido,
                    cedula = nuevoCedula,
                    telefono = nuevoTelefono,
                    sincronizado = false
                )

            // Primero actualizamos localmente
            viewModel.actualizarCliente(
                clienteActualizado
            )

            // Si el cliente ya existe en Azure,
            // actualizamos mediante PUT.
            if (serverId != 0) {

                lifecycleScope.launch {

                    try {

                        val api =
                            RetrofitClient.create(
                                this@activity_editarcliente
                            )

                        val clienteDto =
                            Cliente_dto(
                                id = serverId,
                                syncId = syncId,
                                nombre = nuevoNombre,
                                apellido = nuevaApellido,
                                cedula = nuevoCedula,
                                telefono = nuevoTelefono
                            )

                        // Esperamos realmente la respuesta de Azure
                        api.actualizarCliente(
                            serverId,
                            clienteDto
                        )

                        // Azure confirmó la actualización.
                        // Ahora marcamos Room como sincronizado.
                        viewModel.marcarComoSincronizado(
                            syncId,
                            serverId
                        )

                        Toast.makeText(
                            this@activity_editarcliente,
                            "Cliente actualizado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        regresarAClientes()

                    } catch (e: Exception) {

                        // Si falla Azure, el cliente queda
                        // como pendiente de sincronización.
                        Toast.makeText(
                            this@activity_editarcliente,
                            "Actualizado localmente. Se sincronizará cuando haya conexión.",
                            Toast.LENGTH_LONG
                        ).show()

                        regresarAClientes()
                    }
                }

            } else {

                // Cliente que todavía no tiene ID de Azure.
                // Se queda pendiente para sincronización.
                Toast.makeText(
                    this,
                    "Cliente actualizado localmente. Se sincronizará después.",
                    Toast.LENGTH_SHORT
                ).show()

                regresarAClientes()
            }
        }
    }

    private fun regresarAClientes() {

        val intent =
            Intent(
                this,
                activity_clientes::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP

        startActivity(intent)

        finish()
    }
}