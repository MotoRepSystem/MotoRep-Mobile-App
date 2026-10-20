package com.example.motorep_mobile_app.ui.operacional.catalogos.empleados

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
import com.example.motorep_mobile_app.data.entity.Empleado
import com.example.motorep_mobile_app.data.remote.dto.Empleado_dto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Empleado_ApiRepository
import com.example.motorep_mobile_app.data.repository.Empleado_Repository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.ui.common.MenuLateral
import kotlinx.coroutines.launch
import retrofit2.HttpException

class activity_agregarempleado : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregarempleado)

        MenuLateral(this).configurar()

        val btnguardar =
            findViewById<Button>(R.id.btnGuardarEmpleado)

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

        val dao = database.empleadoDao()

        val repository =
            Empleado_Repository(dao)

        // API de Azure
        val apiRepository =
            Empleado_ApiRepository(
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

            val empleado = Empleado(
                nombre = nombre,
                apellido = apellido,
                cedula = cedula,
                telefono = telefono,
                sincronizado = false
            )

            lifecycleScope.launch {
                try {
                    repository.insertar(empleado)
                } catch (e: Exception) {
                    Log.e("EmpladoCreate", "No se pudo guardar el empleado en Room", e)
                    Toast.makeText(
                        this@activity_agregarempleado,
                        "No se pudo guardar el Empleado.",
                        Toast.LENGTH_LONG
                    ).show()
                    btnguardar.isEnabled = true
                    return@launch
                }

                var sincronizado = false
                if (hayInternet) {
                    try {
                        val respuesta = apiRepository.guardarEmpleado(
                            Empleado_dto(
                                id = null,
                                syncId = empleado.syncId,
                                nombre = empleado.nombre,
                                apellido = empleado.apellido,
                                cedula = empleado.cedula,
                                telefono = empleado.telefono
                            )
                        )
                        val serverId = respuesta.id
                            ?: throw IllegalStateException("La API no devolvió el ID del empleado creado.")

                        repository.marcarComoSincronizado(empleado.syncId, serverId)
                        sincronizado = true
                    } catch (e: Exception) {
                        if (e is HttpException) {
                            val body = e.response()?.errorBody()?.string()
                            Log.e(
                                "EmpleadoCreate",
                                "API rechazó la creación (HTTP ${e.code()}): $body",
                                e
                            )
                        } else {
                            Log.e("EmpleadoCreate", "No se pudo sincronizar el empleado con la API", e)
                        }
                    }
                }

                val mensaje = when {
                    sincronizado -> "Empleado guardado y sincronizado correctamente."
                    hayInternet -> "Empleado guardado localmente. La sincronización con la API quedó pendiente."
                    else -> "Sin conexión: empleado guardado localmente y pendiente de sincronización."
                }
                Toast.makeText(this@activity_agregarempleado, mensaje, Toast.LENGTH_LONG).show()

                val intent = Intent(this@activity_agregarempleado, activity_empleados::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }
    }
}