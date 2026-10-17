package com.example.motorep_mobile_app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.remote.dto.LoginRequest
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.ui.encuesta.activity_loginencuesta
import com.example.motorep_mobile_app.ui.dashboard.activity_dashboard
import com.example.motorep_mobile_app.data.session.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

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

        val btnEncuesta = findViewById<Button>(R.id.btnEncuesta)
        btnEncuesta.setOnClickListener {
            val intent = Intent(this, activity_loginencuesta::class.java)
            startActivity(intent)
        }

            /*val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val intent = Intent(this, activity_dashboard::class.java)
            startActivity(intent)
        }*/


        // Obtiene el campo de usuario definido en activity_login.xml.
        val txtUsuario =
            findViewById<TextInputEditText>(R.id.tieUser)

        // Obtiene el campo de contraseña definido en activity_login.xml.
        val txtContraseña =
            findViewById<TextInputEditText>(R.id.tiePassword)

        // Obtiene el contenedor del campo de usuario.
        val layoutUsuario =
            findViewById<TextInputLayout>(R.id.txtLayoutUser)

        // Obtiene el contenedor del campo de contraseña.
        val layoutContraseña =
            findViewById<TextInputLayout>(R.id.txtLayoutPassword)

        // Obtiene el botón "Iniciar Sesión".
        val btnInicioS =
            findViewById<Button>(R.id.btnLogin)

        // Detecta cuando el usuario presiona el botón de iniciar sesión
        btnInicioS.setOnClickListener {

            //Obtiene el contenido escrito en el campo de usuario.
            val usuario =
                txtUsuario.text.toString().trim()

            // Obtiene el contenido escrito en el campo de contraseña.
            val contraseña =
                txtContraseña.text.toString()

            // Limpia los mensajes de error que hayan sidos mostrados en intentos anteriores
            layoutUsuario.error = null
            layoutContraseña.error = null

            // Variable utilizada para determinar si todos los datos cumplen las validaciones locales.

            var datosValidos = true

            // Comprueba si el campo de usuario está vacío.
            if (usuario.isEmpty()) {

                // Muestra el mensaje de error debajo del campo.
                layoutUsuario.error = "El usuario es obligatorio"

                // Indica que existen datos incorrectos.
                datosValidos = false
            }

            // Comprueba si el campo de contraseña está vacío.
            if (contraseña.isEmpty()) {

                // Muestra el mensaje de error debajo del campo.
                layoutContraseña.error = "La contraseña es obligatoria"

                // Indica que los datos no son validos
                datosValidos = false
            }

            // Comprueba que la contraseña tenga como mínimo 6 caracteres.
            if (contraseña.isNotEmpty() && contraseña.length < 6) {

                // Muestra un mensaje indicando la longitud mínima.
                layoutContraseña.error =
                    "La contraseña debe tener al menos 6 caracteres"

                // Indica que los datos no son válidos.
                datosValidos = false
            }

            //Si alguno de los campos no cumple las validaciones, no se continúa con el proceso de autenticación
            if (!datosValidos) {
                return@setOnClickListener
            }

            // Login real contra la API con JWT
            lifecycleScope.launch {
                try {
                    val respuesta = RetrofitClient.create(this@activity_login).login(
                        LoginRequest(username = usuario, password = contraseña)
                    )
                    // Login exitoso: el backend devolvió un token válido.
                    // Obtiene el token JWT enviado por la API.
                    val token = respuesta.access
                    // Guarda el token JWT mediante SessionManager.
                    val sessionManager =    SessionManager(this@activity_login)
                    sessionManager.guardarToken(token)
                    // Comprueba en Logcat que el token fue almacenado.
                    android.util.Log.d(
                        "JWT_TEST",
                        "Token guardado correctamente: ${token.take(10)}..."
                    )
                    // Abre el Dashboard después de guardar el token.
                    val intent = Intent(this@activity_login, activity_dashboard::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    android.util.Log.e(
                        "LOGIN_ERROR",
                        "Error real durante el login",
                        e
                    )
                    layoutUsuario.error = "Error de conexión"
                    layoutContraseña.error = "Revise Logcat"
                }
            }
        }


    }
}