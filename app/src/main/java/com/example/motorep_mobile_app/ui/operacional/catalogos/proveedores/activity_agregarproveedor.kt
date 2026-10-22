package com.example.motorep_mobile_app.ui.operacional.catalogos.proveedores

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
import com.example.motorep_mobile_app.data.entity.Proveedor
import com.example.motorep_mobile_app.data.remote.dto.toDto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Proveedor_ApiRepository
import com.example.motorep_mobile_app.data.repository.Proveedor_Repository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.ui.common.MenuLateral
import kotlinx.coroutines.launch
import retrofit2.HttpException

class activity_agregarproveedor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregarproveedor)

        MenuLateral(this).configurar()

        val btnguardar =
            findViewById<Button>(R.id.btnGuardarProveedor)

        val edtNombre =
            findViewById<EditText>(R.id.edtNombre)

        val edtTelefono =
            findViewById<EditText>(R.id.edtTelefono)



        // Base de datos local Room
        val database =
            Motorep_Database.getDatabase(applicationContext)

        val dao = database.proveedorDao()

        val repository =
            Proveedor_Repository(dao)

        // API de Azure
        val apiRepository =
            Proveedor_ApiRepository(
                RetrofitClient.create(this)
            )

        btnguardar.setOnClickListener {
            btnguardar.isEnabled = false
            Log.d("ProveedorCreate", "Solicitud de guardar proveedor recibida")

            val nombre =
                edtNombre.text.toString().trim()

            val telefono =
                edtTelefono.text.toString().trim()


            // Validaciones básicas
            if (nombre.isEmpty() ||
                telefono.isEmpty()
            ) {

                Log.w("ProveedorCreate", "Alta detenida: hay campos obligatorios vacíos")
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
            Log.d("ClienteCreate", "Conectividad disponible: $hayInternet")


            // El producto siempre se crea primero en Room con sincronizado = false
            // Solo después de recibir confirmación de Azure lo marcamos como sincronizado.

            val proveedor = Proveedor(
                nombre = nombre,
                telefono = telefono,
                sincronizado = false
            )

            lifecycleScope.launch {
                try {
                    repository.insertar(proveedor)
                    Log.d("ProveedorCreate", "Proveedor guardado localmente")
                } catch (e: Exception) {
                    Log.e("ProveedorCreate", "No se pudo guardar el Proveedor en Room", e)
                    Toast.makeText(
                        this@activity_agregarproveedor,
                        "No se pudo guardar el proveedor.",
                        Toast.LENGTH_LONG
                    ).show()
                    btnguardar.isEnabled = true
                    return@launch
                }

                var sincronizado = false
                if (hayInternet) {
                    try {
                        Log.d("ProveedorCreate", "Enviando proveedor a la API")
                        val respuesta = apiRepository.guardarProveedor(proveedor.toDto())
                        Log.d("ProveedorCreate", "Respuesta API recibida; serverId=${respuesta.id}")
                        val serverId = respuesta.id
                            ?: throw IllegalStateException("La API no devolvió el ID del proveedor creado ($respuesta).")

                        repository.marcarComoSincronizado(proveedor.syncId, serverId)
                        sincronizado = true
                    } catch (e: Exception) {
                        if (e is HttpException) {
                            val body = e.response()?.errorBody()?.string()
                            Log.e(
                                "ProveedorCreate",
                                "API rechazó la creación (HTTP ${e.code()}): $body",
                                e
                            )
                        } else {
                            Log.e("ProveedorCreate", "No se pudo sincronizar el proveedor con la API", e)
                        }
                    }
                } else {
                    Log.i("ProveedorCreate", "Sin conexión: se conserva el proveedor pendiente de sincronización")
                }

                val mensaje = when {
                    sincronizado -> "Proveedor guardado y sincronizado correctamente."
                    hayInternet -> "Proveedor guardado localmente. La sincronización con la API quedó pendiente."
                    else -> "Sin conexión: proveedor guardado localmente y pendiente de sincronización."
                }
                Toast.makeText(this@activity_agregarproveedor, mensaje, Toast.LENGTH_LONG).show()

                val intent = Intent(this@activity_agregarproveedor, activity_proveedores::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }
    }
}