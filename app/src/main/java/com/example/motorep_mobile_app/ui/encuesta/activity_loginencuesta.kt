package com.example.motorep_mobile_app.ui.encuesta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.conexion.BuscarClienteRequest
import com.example.motorep_mobile_app.conexion.BuscarClienteResponse
import com.example.motorep_mobile_app.conexion.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class activity_loginencuesta : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar la pantalla de borde a borde.
        enableEdgeToEdge()

        // Cargar el diseño de la pantalla de búsqueda.
        setContentView(R.layout.activity_login_encuesta)

        // Ajustar la pantalla a las barras del sistema.
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

        // Campo donde el cliente escribirá su nombre completo.
        val txtUsuario =
            findViewById<TextInputEditText>(
                R.id.tieUsuarioEncuesta
            )

        // Contenedor del campo para mostrar errores.
        val layoutUsuario =
            findViewById<TextInputLayout>(
                R.id.txtLayoutUsuarioEncuesta
            )

        // Botón para buscar al cliente y continuar.
        val btnContinuar =
            findViewById<Button>(
                R.id.btnContinuarEncuesta
            )

        // Evento del botón Continuar.
        btnContinuar.setOnClickListener {

            // Obtener el nombre completo escrito por el cliente.
            val nombreCompleto =
                txtUsuario.text.toString().trim()

            // Limpiar errores anteriores.
            layoutUsuario.error = null

            // Validar que el nombre no esté vacío.
            if (nombreCompleto.isEmpty()) {

                layoutUsuario.error =
                    "El nombre completo es obligatorio"

                return@setOnClickListener
            }

            // Desactivar el botón mientras se realiza la búsqueda.
            btnContinuar.isEnabled = false

            // Preparar los datos que se enviarán a Django.
            val datos = BuscarClienteRequest(
                nombre_completo = nombreCompleto
            )

            // Crear la llamada a la API.
            val llamada =
                RetrofitClient.clienteEncuestaApi.buscarCliente(datos)

            // Mostrar en Logcat la URL que se está enviando.
            android.util.Log.d(
                "ENCUESTA_URL",
                llamada.request().toString()
            )

            // Ejecutar la búsqueda del cliente mediante Django.
            llamada.enqueue(object : Callback<BuscarClienteResponse> {

                override fun onResponse(
                    call: Call<BuscarClienteResponse>,
                    response: Response<BuscarClienteResponse>
                ) {

                    // Reactivar el botón.
                    btnContinuar.isEnabled = true

                    // Verificar si la búsqueda fue exitosa.
                    if (response.isSuccessful) {

                        // Obtener los datos del cliente.
                        val cliente = response.body()

                        if (cliente != null) {

                            // Verificar si el cliente tiene facturas.
                            if (cliente.facturas.isEmpty()) {

                                Toast.makeText(
                                    this@activity_loginencuesta,
                                    "No encontramos facturas asociadas a tu cuenta",
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            // Preparar la pantalla de encuesta.
                            val intent = Intent(
                                this@activity_loginencuesta,
                                EncuestaActivity::class.java
                            )

                            // Preparar la lista de identificadores de facturas.
                            val facturaIds = ArrayList<Int>()

                            // Preparar las etiquetas que se mostrarán en el Spinner.
                            val facturaLabels = ArrayList<String>()

                            // Recorrer las facturas recibidas desde Django.
                            cliente.facturas.forEach { factura ->

                                // Guardar el ID real de la factura.
                                facturaIds.add(factura.factura_id)

                                // Crear el texto visible para el cliente.
                                facturaLabels.add(
                                    "Factura ${factura.numero_factura} - " +
                                            "${factura.fecha} - " +
                                            "C$${factura.total}"
                                )
                            }

                            // Enviar los identificadores de las facturas.
                            intent.putIntegerArrayListExtra(
                                "factura_ids",
                                facturaIds
                            )

                            // Enviar las etiquetas de las facturas.
                            intent.putStringArrayListExtra(
                                "factura_labels",
                                facturaLabels
                            )

                            // Enviar el identificador del cliente.
                            intent.putExtra(
                                "cliente_id",
                                cliente.cliente_id
                            )

                            // Enviar el nombre completo del cliente.
                            intent.putExtra(
                                "usuario_encuesta",
                                "${cliente.nombres} ${cliente.apellidos}"
                            )

                            // Abrir la pantalla de encuesta.
                            startActivity(intent)

                            // Cerrar la pantalla de búsqueda.
                            finish()

                        } else {

                            Toast.makeText(
                                this@activity_loginencuesta,
                                "No se recibió información del cliente",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } else {

                        // Mostrar mensajes según el código de respuesta.
                        when (response.code()) {

                            404 -> Toast.makeText(
                                this@activity_loginencuesta,
                                "No se encontró ningún cliente con ese nombre",
                                Toast.LENGTH_LONG
                            ).show()

                            409 -> Toast.makeText(
                                this@activity_loginencuesta,
                                "Se encontraron varios clientes con ese nombre",
                                Toast.LENGTH_LONG
                            ).show()

                            else -> Toast.makeText(
                                this@activity_loginencuesta,
                                "Error del servidor: ${response.code()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                // Procesar errores de conexión.
                override fun onFailure(
                    call: Call<BuscarClienteResponse>,
                    t: Throwable
                ) {

                    // Registrar el error en Logcat.
                    android.util.Log.e(
                        "ENCUESTA_API",
                        "Error al buscar cliente: ${t.message}",
                        t
                    )

                    // Reactivar el botón.
                    btnContinuar.isEnabled = true

                    // Mostrar el mensaje de error.
                    Toast.makeText(
                        this@activity_loginencuesta,
                        "No se pudo conectar con Django: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }
}