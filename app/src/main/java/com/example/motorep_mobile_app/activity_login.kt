package com.example.motorep_mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class activity_login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Campo de usuario
        val txtUser =
            findViewById<TextInputEditText>(R.id.tieUser)

        // Campo de contraseña
        val txtPassword =
            findViewById<TextInputEditText>(R.id.tiePassword)

        // Botón para ingresar a la encuesta
        val btnEncuesta =
            findViewById<Button>(R.id.btnUsersForms)

        // Evento del botón de encuesta
        btnEncuesta.setOnClickListener {

            val intent =
                Intent(
                    this@activity_login,
                    LoginEncuestaActivity::class.java
                )

            startActivity(intent)
        }
        // Contenedor del campo de usuario
        val layoutUser =
            findViewById<TextInputLayout>(R.id.txtLayoutUser)

        // Contenedor del campo de contraseña
        val layoutPassword =
            findViewById<TextInputLayout>(R.id.txtLayoutPassword)

        // Botón de iniciar sesión
        val btnInicio =
            findViewById<Button>(R.id.btnLogin)

        // Evento del botón
        btnInicio.setOnClickListener {

            // Obtener usuario
            val usuario =
                txtUser.text.toString().trim()

            // Obtener contraseña
            val contraseña =
                txtPassword.text.toString()

            // Limpiar errores anteriores
            layoutUser.error = null
            layoutPassword.error = null

            var datosValidos = true

            // Validar usuario
            if (usuario.isEmpty()) {

                layoutUser.error =
                    "El usuario es obligatorio"

                datosValidos = false
            }

            // Validar contraseña
            if (contraseña.isEmpty()) {

                layoutPassword.error =
                    "La contraseña es obligatoria"

                datosValidos = false

            } else if (contraseña.length < 6) {

                layoutPassword.error =
                    "La contraseña debe tener al menos 6 caracteres"

                datosValidos = false
            }

            // Si existen errores, no continuar
            if (!datosValidos) {
                return@setOnClickListener
            }

            /* Por ahora el proyecto solamente trabaja con navegación entre pantallas. La autenticación mediante API/JWT se agregara posteriormente.
             */

            val intent =
                Intent(this@activity_login, activity_dashboard::class.java)

            startActivity(intent)

            finish()
        }
    }
}