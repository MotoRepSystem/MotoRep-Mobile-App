package com.example.motorep_mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.example.motorep_mobile_app.databinding.ActivityEditarclienteBinding
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_clientes
import com.google.android.material.navigation.NavigationView
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

        // Inicializar el ViewModel
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
        viewModel = ViewModelProvider(this, factory)[Cliente_Viewmodel::class.java]




        val id = intent.getIntExtra("id", 0)
        val serverId = intent.getIntExtra("serverId", 0)
        val syncId = intent.getStringExtra("syncId") ?: ""
        val nombre = intent.getStringExtra("nombre")
        val Apellido = intent.getStringExtra("apellido")
        val cedula = intent.getStringExtra("cedula")
        val telefono = intent.getStringExtra("telefono")

        // Asignar los valores a los campos de la pantalla (UI)
        binding.edtNombre.setText(nombre)
        binding.edtApellido.setText(Apellido)
        binding.edtCedula.setText(cedula)
        binding.edtTelefono.setText(telefono)


        // --- AQUÍ DEBE IR EL LISTENER ---
        binding.btnActualizarCliente.setOnClickListener {

            val nuevoNombre = binding.edtNombre.text.toString()
            val nuevoApellido = binding.edtApellido.text.toString()
            val nuevaCedula = binding.edtCedula.text.toString()
            val nuevaTelefono = binding.edtTelefono.text.toString()

            // Validación básica de campos vacíos o incorrectos
            if (nuevoNombre.isEmpty() || nuevoApellido.isEmpty() || nuevaCedula.isEmpty() || nuevaTelefono.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Crear un objeto Cliente con los nuevos valores
            val clienteActualizado = Cliente(
                id = id,
                serverId = if (serverId == 0) null else serverId,
                syncId = syncId,
                nombre = nuevoNombre,
                apellido = nuevoApellido,
                cedula = nuevaCedula,
                telefono = nuevaTelefono,
                sincronizado = false // Marcamos false para que el sistema intente enviarlo a la API
            )
            
            lifecycleScope.launch {
                val hayInternet = NetworkUtils.hayInternet(this@activity_editarcliente)
                
                // Actualizamos primero la base de datos local
                viewModel.actualizarCliente(clienteActualizado)
                
                if (hayInternet) {
                    try {
                        val apiRepository =
                            Cliente_ApiRepository(RetrofitClient.create(this@activity_editarcliente))
                        
                        // Si ya tiene un ID en el servidor, usamos PUT
                        if (serverId != 0) {
                            apiRepository.actualizarCliente(
                                serverId, 
                                com.example.motorep_mobile_app.data.remote.dto.Cliente_dto(
                                    id = serverId,
                                    syncId = syncId,
                                    Nombres = nuevoNombre,
                                    Apellidos = nuevoApellido,
                                    NumCedula = nuevaCedula,
                                    NumTelefono = nuevaTelefono
                                )
                            )
                            // Marcamos como sincronizado
                            viewModel.marcarComoSincronizado(syncId, serverId)
                            Toast.makeText(this@activity_editarcliente, "Cliente actualizado con éxito en Azure", Toast.LENGTH_SHORT).show()
                        } else {
                            // En caso de que se haya creado offline y aún no tuviera serverId, tratamos de enviarlo como nuevo
                            Toast.makeText(this@activity_editarcliente, "El cliente se actualizará pronto en Azure", Toast.LENGTH_SHORT).show()
                        }
                    } catch(e: Exception) {
                        Toast.makeText(this@activity_editarcliente, "Actualizado localmente. Pendiente sincronizar a Azure.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@activity_editarcliente, "Sin Internet. Actualizado localmente.", Toast.LENGTH_LONG).show()
                }

                val intent = Intent(this@activity_editarcliente, activity_clientes::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }

    }
}