package com.example.motorep_mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginEncuestaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_login_encuesta)

        // Ajustar la pantalla a las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

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

        // Campo donde el cliente escribirá su usuario
        val txtUsuario =
            findViewById<TextInputEditText>(
                R.id.tieUsuarioEncuesta
            )

        // Contenedor del campo para mostrar errores
        val layoutUsuario =
            findViewById<TextInputLayout>(
                R.id.txtLayoutUsuarioEncuesta
            )

        // Botón para continuar hacia la encuesta
        val btnContinuar =
            findViewById<Button>(
                R.id.btnContinuarEncuesta
            )

        // Evento del botón Continuar
        btnContinuar.setOnClickListener {

            // Obtener el usuario escrito
            val usuario =
                txtUsuario.text.toString().trim()

            // Limpiar errores anteriores
            layoutUsuario.error = null

            // Validar que el usuario no esté vacío
            if (usuario.isEmpty()) {

                layoutUsuario.error =
                    "El usuario es obligatorio"

                return@setOnClickListener
            }

            // Mostrar temporalmente el usuario recibido
            Toast.makeText(
                this,
                "Bienvenido, $usuario",
                Toast.LENGTH_SHORT
            ).show()

            // Abrir la pantalla de encuesta
            val intent =
                Intent(
                    this@LoginEncuestaActivity,
                    EncuestaActivity::class.java
                )

            // Enviar el usuario a la encuesta
            intent.putExtra(
                "usuario_encuesta",
                usuario
            )

            startActivity(intent)

            // Cerrar el login de encuesta
            finish()
        }
    }
}