
package com.example.motorep_mobile_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.remote.dto.Empleado_dto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Empleado_ApiRepository
import com.example.motorep_mobile_app.viewmodel.Empleado_ApiViewModel

class activity_agregarempleado : AppCompatActivity() {

    private lateinit var apiViewModel: Empleado_ApiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregarempleado)

        val drawerLayout =
            findViewById<DrawerLayout>(R.id.DrawerLayout)

        findViewById<ImageView>(R.id.imgMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val edtNombre = findViewById<EditText>(R.id.edtNombre)
        val edtApellido = findViewById<EditText>(R.id.edtApellido)
        val edtCedula = findViewById<EditText>(R.id.edtCedula)
        val edtTelefono = findViewById<EditText>(R.id.edtTelefono)

        // Inicializar el acceso a la API.
        val repository =
            Empleado_ApiRepository(RetrofitClient.create(this))

        apiViewModel = Empleado_ApiViewModel(repository)

        val btnGuardar =
            findViewById<Button>(R.id.btnGuardarCliente)

        btnGuardar.setOnClickListener {

            val nombre = edtNombre.text.toString().trim()
            val apellido = edtApellido.text.toString().trim()
            val cedula = edtCedula.text.toString().trim()

            // Guardar solo dígitos para el teléfono.
            val telefono = edtTelefono.text.toString()
                .filter { it.isDigit() }

            // Validaciones de los campos obligatorios.
            if (nombre.isBlank() || apellido.isBlank() ||
                cedula.isBlank() || telefono.isBlank()
            ) {
                Toast.makeText(
                    this,
                    "Completa todos los campos.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (nombre.length > 100 || apellido.length > 100) {
                Toast.makeText(
                    this,
                    "Nombre y apellido admiten hasta 100 caracteres.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (cedula.length > 16) {
                Toast.makeText(
                    this,
                    "La cédula admite hasta 16 caracteres.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (telefono.length != 8) {
                Toast.makeText(
                    this,
                    "El teléfono debe contener 8 dígitos.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // El servidor genera el ID. Se envía null para omitir 'id' en la petición JSON.
            val nuevoEmpleado = Empleado_dto(
                id = null,
                Nombres = nombre,
                Apellidos = apellido,
                Telefono = telefono,
                NumCedula = cedula
            )

            btnGuardar.isEnabled = false

            apiViewModel.guardarEmpleado(nuevoEmpleado) { respuesta ->

                if (respuesta != null) {
                    Toast.makeText(
                        this,
                        "Empleado registrado correctamente.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Al regresar, la lista volverá a consultar la API.
                    finish()
                } else {
                    btnGuardar.isEnabled = true

                    Toast.makeText(
                        this,
                        "No se pudo registrar el empleado. " +
                                "Revisa la conexión y el servidor.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
