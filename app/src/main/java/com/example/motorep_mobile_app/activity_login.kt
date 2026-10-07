package com.example.motorep_mobile_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class activity_login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


         // Obtiene el campo de usuario definido en activity_login.xml.
        val txtUser =
            findViewById<TextInputEditText>(R.id.TieUser)

        // Obtiene el campo de contraseña definido en activity_login.xml.
        val txtPassword =
            findViewById<TextInputEditText>(R.id.TiePassword)

        // Obtiene el contenedor del campo de usuario.
        val layoutUser =
            findViewById<TextInputLayout>(R.id.txtLayoutUser)

        // Obtiene el contenedor del campo de contraseña.
        val layoutPassword =
            findViewById<TextInputLayout>(R.id.txtLayoutPassword)

        // Obtiene el botón "Iniciar Sesión".
        val btnInicio =
            findViewById<Button>(R.id.btnLogin)

        // Detecta cuando el usuario presiona el botón de iniciar sesión
        btnInicio.setOnClickListener {

            //Obtiene el contenido escrito en el campo de usuario.
            val usuario =
                txtUser.text.toString().trim()

            // Obtiene el contenido escrito en el campo de contraseña.
            val contraseña =
                txtPassword.text.toString()

            // Limpia los mensajes de error que hayan sidos mostrados en intentos anteriores
            layoutUser.error = null
            layoutPassword.error = null

            // Variable utilizada para determinar si todos los datos cumplen las validaciones locales.

            var datosValidos = true

            // Comprueba si el campo de usuario está vacío.
            if (usuario.isEmpty()) {

                // Muestra el mensaje de error debajo del campo.
                layoutUser.error = "El usuario es obligatorio"

                // Indica que existen datos incorrectos.
                datosValidos = false
            }

            // Comprueba si el campo de contraseña está vacío.
            if (contraseña.isEmpty()) {

                // Muestra el mensaje de error debajo del campo.
                layoutPassword.error = "La contraseña es obligatoria"

                // Indica que los datos no son validos
                datosValidos = false
            }

            // Comprueba que la contraseña tenga como mínimo 6 caracteres.
            if (contraseña.isNotEmpty() && contraseña.length < 6) {

                // Muestra un mensaje indicando la longitud mínima.
                layoutPassword.error =
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
                    val respuesta = RetrofitClient.create(this@MainActivity).login(
                        LoginRequest(usuario = usuario, password = contraseña)
                    )

                    // Login exitoso: el backend devolvió un token válido.
                    // Obtiene el token JWT enviado por la API.
                    val token = respuesta.token

                    // Guarda el token JWT mediante SessionManager.
                    val sessionManager = SessionManager(this@MainActivity)
                    sessionManager.guardarToken(token)

                    // Comprueba en Logcat que el token fue almacenado.
                    android.util.Log.d(
                        "JWT_TEST",
                        "Token guardado correctamente: ${token.take(10)}..."
                    )

                    // Abre el Dashboard después de guardar el token.
                    val intent = Intent(this@MainActivity, Dashboard::class.java)
                    startActivity(intent)
                    finish()

                } catch (e: Exception) {

                    android.util.Log.e(
                        "LOGIN_ERROR",
                        "Error real durante el login",
                        e
                    )

                    layoutUser.error = "Error de conexión"
                    layoutPassword.error = "Revise Logcat"
                }
            }
        }


    }
}