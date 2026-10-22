package com.example.motorep_mobile_app.ui.operacional.catalogos.proveedores

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import com.example.motorep_mobile_app.data.remote.dto.toDto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.data.repository.Proveedor_Repository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.databinding.ActivityEditarclienteBinding
import com.example.motorep_mobile_app.databinding.ActivityEditarproveedorBinding
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.ui.operacional.catalogos.clientes.activity_clientes
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.example.motorep_mobile_app.viewmodel.Proveedor_Viewmodel
import kotlinx.coroutines.launch
import retrofit2.HttpException

class activity_editarproveedor : AppCompatActivity() {

    private lateinit var binding: ActivityEditarproveedorBinding
    private lateinit var viewModel: Proveedor_Viewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditarproveedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MenuLateral(this).configurar()

        // Inicializar Room
        val database = Motorep_Database.getDatabase(applicationContext)
        val dao = database.proveedorDao()
        val repository = Proveedor_Repository(dao)
        val factory =
            object : ViewModelProvider.Factory {

                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {

                    if (
                        modelClass.isAssignableFrom(
                            Proveedor_Viewmodel::class.java
                        )
                    ) {
                        return Proveedor_Viewmodel(repository) as T
                    }

                    throw IllegalArgumentException(
                        "Unknown ViewModel class"
                    )
                }
            }

        viewModel = ViewModelProvider(this, factory)[Proveedor_Viewmodel::class.java]

        // Recuperar información del proveedor
        val id =
            intent.getIntExtra("id", 0)

        val syncId =
            intent.getStringExtra("syncId") ?: ""

        val nombre =
            intent.getStringExtra("nombre") ?: ""

        val serverId =
            intent.getIntExtra("serverId", 0)
        Log.d(
            "EDITAR_DEBUG",
            "id=$id | syncId='$syncId' | serverId=$serverId | nombre=$nombre")

        val telefono =
            intent.getStringExtra("telefono") ?: ""



        // Mostrar datos actuales
        binding.edtNombre.setText(nombre)
        binding.edtTelefono.setText(telefono)

        // Actualizar
        binding.btnGuardarProveedor.setOnClickListener {

            val nuevoNombre =
                binding.edtNombre.text
                    .toString()
                    .trim()

            val nuevoTelefono =
                binding.edtTelefono.text
                    .toString()
                    .trim()

            // Validación
            if (
                nuevoNombre.isEmpty() ||
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
            val proveedorActualizado =
                Proveedor(
                    id = id,
                    syncId = syncId,
                    serverId =
                        if (serverId == 0) null
                        else serverId,
                    nombre = nuevoNombre,
                    telefono = nuevoTelefono,
                    sincronizado = false
                )

            lifecycleScope.launch {
                try {
                    viewModel.actualizarProveedor(proveedorActualizado)
                } catch (e: Exception) {
                    Log.e("ProveedorUpdate", "No se pudo actualizar el proveedor en Room", e)
                    Toast.makeText(
                        this@activity_editarproveedor,
                        "No se pudo guardar la actualización.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                if (serverId != 0 && NetworkUtils.hayInternet(this@activity_editarproveedor)) {
                    try {
                        val api = RetrofitClient.create(this@activity_editarproveedor)
                        val proveedorDto = proveedorActualizado.toDto()

                        api.actualizarProveedor(serverId, proveedorDto)
                        viewModel.marcarComoSincronizado(syncId, serverId)
                        Toast.makeText(
                            this@activity_editarproveedor,
                            "Proveedor actualizado correctamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        if (e is HttpException) {
                            val body = e.response()?.errorBody()?.string()
                            Log.e(
                                "ProveedorUpdate",
                                "API rechazó la actualización (HTTP ${e.code()}): $body",
                                e
                            )
                        } else {
                            Log.e("ProveedorUpdate", "No se pudo sincronizar la actualización con la API", e)
                        }
                        Toast.makeText(
                            this@activity_editarproveedor,
                            "Actualizado localmente. La sincronización con la API quedó pendiente.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@activity_editarproveedor,
                        "Actualizado localmente. Se sincronizará cuando haya conexión.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                regresarAProveedores()
            }
        }
    }

    private fun regresarAProveedores() {

        val intent =
            Intent(
                this,
                activity_proveedores::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP

        startActivity(intent)

        finish()
    }
}