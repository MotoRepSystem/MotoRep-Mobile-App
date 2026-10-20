package com.example.motorep_mobile_app.ui.operacional.catalogos.empleados

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.entity.Empleado
import com.example.motorep_mobile_app.data.remote.dto.toDto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Empleado_Repository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.databinding.ActivityEditarempleadoBinding
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.viewmodel.Empleado_ViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException

class activity_editarempleado : AppCompatActivity() {


    private lateinit var binding: ActivityEditarempleadoBinding
    private lateinit var viewModel: Empleado_ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityEditarempleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MenuLateral(this).configurar()

        // Inicializar Room
        val database = Motorep_Database.getDatabase(applicationContext)

        val dao = database.empleadoDao()

        val repository = Empleado_Repository(dao)

        val factory =
            object : ViewModelProvider.Factory {

                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {

                    if (
                        modelClass.isAssignableFrom(
                            Empleado_ViewModel::class.java
                        )
                    ) {
                        return Empleado_ViewModel(repository) as T
                    }

                    throw IllegalArgumentException(
                        "Unknown ViewModel class"
                    )
                }
            }

        viewModel = ViewModelProvider(this, factory)[Empleado_ViewModel::class.java]

        // Recuperar información del producto
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
            val empleadoActualizado =
                Empleado(
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

            lifecycleScope.launch {
                try {
                    viewModel.actualizarEmpleado(empleadoActualizado)
                } catch (e: Exception) {
                    Log.e("EmpleadoUpdate", "No se pudo actualizar el empleado en Room", e)
                    Toast.makeText(
                        this@activity_editarempleado,
                        "No se pudo guardar la actualización.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                if (serverId != 0 && NetworkUtils.hayInternet(this@activity_editarempleado)) {
                    try {
                        val api = RetrofitClient.create(this@activity_editarempleado)
                        val empleadoDto = empleadoActualizado.toDto()

                        api.actualizarEmpleado(serverId, empleadoDto)
                        viewModel.marcarComoSincronizado(syncId, serverId)
                        Toast.makeText(
                            this@activity_editarempleado,
                            "Empleado actualizado correctamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        if (e is HttpException) {
                            val body = e.response()?.errorBody()?.string()
                            Log.e(
                                "EmpleadoUpdate",
                                "API rechazó la actualización (HTTP ${e.code()}): $body",
                                e
                            )
                        } else {
                            Log.e("EmpleadoUpdate", "No se pudo sincronizar la actualización con la API", e)
                        }
                        Toast.makeText(
                            this@activity_editarempleado,
                            "Actualizado localmente. La sincronización con la API quedó pendiente.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@activity_editarempleado,
                        "Actualizado localmente. Se sincronizará cuando haya conexión.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                regresarAEmpleados()
            }
        }
    }

    private fun regresarAEmpleados() {

        val intent =
            Intent(
                this,
                activity_empleados::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP

        startActivity(intent)

        finish()
    }
}