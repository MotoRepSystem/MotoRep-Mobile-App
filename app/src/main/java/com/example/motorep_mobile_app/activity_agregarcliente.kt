package com.example.motorep_mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.example.motorep_mobile_app.data.database.MotorepDatabase
import com.example.motorep_mobile_app.data.remote.dto.Cliente_dto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Cliente_ApiRepository
import com.example.motorep_mobile_app.data.utils.NetworkUtils
import com.example.motorep_mobile_app.ui.dashboard.activity_dashboard
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_clientes
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_empleados
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_inventario
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_caja
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_compras
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_facturacion
import com.example.motorep_mobile_app.ui.reportes.activity_incidencias
import com.example.motorep_mobile_app.ui.reportes.activity_indicadores
import com.example.motorep_mobile_app.ui.reportes.activity_tendencias
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class activity_agregarcliente : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navViews: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregarcliente)

        drawerLayout = findViewById(R.id.DrawerLayout)
        navViews = findViewById(R.id.navigationView)


        val mainConstraint = findViewById<ConstraintLayout>(R.id.mainConstraint)
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayoutMainTitle)

        // Responsividad del Dashboard y del menú lateral
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            linearLayout.setPadding(
                linearLayout.paddingLeft,
                systemBars.top,
                linearLayout.paddingRight,
                linearLayout.paddingBottom
            )

            mainConstraint.setPadding(0, 0, 0, systemBars.bottom)

            navViews.setPadding(
                0,
                systemBars.top,
                0,
                systemBars.bottom
            )

            insets
        }

        val btnGuardar = findViewById<Button>(R.id.btnGuardarCliente)
        val edtNombre = findViewById<EditText>(R.id.edtNombre)
        val edtApellido = findViewById<EditText>(R.id.edtApellido)
        val edtCedula = findViewById<EditText>(R.id.edtCedula)
        val edtTelefono = findViewById<EditText>(R.id.edtTelefono)

        val database = MotorepDatabase.getDatabase(this)
        val dao = database.clienteDao()
        val repository = Cliente_Repository(dao)
        val apiRepository = Cliente_ApiRepository(RetrofitClient.create(this))

        val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(Cliente_Viewmodel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return Cliente_Viewmodel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        val viewModel = ViewModelProvider(this, factory).get(Cliente_Viewmodel::class.java)

        btnGuardar.setOnClickListener {

            val nombre =
                edtNombre.text.toString().trim()
            val apellido =
                edtApellido.text.toString().trim()
            val cedula =
                edtCedula.text.toString().trim()
            val telefonoTexto =
                edtTelefono.text.toString().trim()

            // Validaciones básicas
            if (nombre.isEmpty() ||
                apellido.isEmpty() ||
                cedula.isEmpty() ||
                telefonoTexto.isEmpty()
            ) {
                Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val hayInternet =
                NetworkUtils.hayInternet(this)

            // El producto siempre se crea primero en Room con sincronizado = false
            // Solo después de recibir confirmación de Azure lo marcamos como sincronizado.

            val cliente = Cliente(
                nombre = nombre,
                apellido = apellido,
                cedula = cedula,
                telefono = telefonoTexto,
                sincronizado = false
            )

            lifecycleScope.launch {
                try {
                    // Guardar primero en Room
                    repository.insertarCliente(cliente)

                    if (hayInternet) {
                        // Preparamos el DTO utilizando syncId como identificador
                        val clienteDto = Cliente_dto(
                            id = null,
                            syncId = cliente.syncId,
                            Nombres = cliente.nombre,
                            Apellidos = cliente.apellido,
                            NumCedula = cliente.cedula,
                            NumTelefono = cliente.telefono
                        )

                        // Enviar a Azure
                        val respuesta =
                            apiRepository.guardarCliente(
                                clienteDto
                            )

                        // Verificar que Azure devolvió el ID
                        val serverId = respuesta.id

                        if (serverId == null) {
                            throw Exception("Azure no devolvió el ID del producto.")
                        }

                        // Guardar el ID de Azure y marcar como sincronizado
                        repository.marcarComoSincronizado(
                            cliente.syncId,
                            serverId
                        )

                        Toast.makeText(
                            this@activity_agregarcliente ,
                            "Cliente guardado y sincronizado correctamente.",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        Toast.makeText(
                            this@activity_agregarcliente ,
                            "Sin conexión: el cliente se guardó localmente y quedará pendiente de sincronización.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    // Limpiar campos
                    edtNombre.text.clear()
                    edtApellido.text.clear()
                    edtCedula.text.clear()
                    edtTelefono.text.clear()

                    // Regresar al módulo de productos
                    val intent =
                        Intent(
                            this@activity_agregarcliente ,
                            activity_clientes::class.java
                        )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP

                    startActivity(intent)
                    finish()

                } catch (e: Exception) {


                    // Si Azure falla, el producto YA está en Room con sincronizado = false, por lo tanto NO se pierde.
                    //Posteriormente WorkManager podrá enviarlo.

                    Toast.makeText(
                        this@activity_agregarcliente ,
                        "Cliente guardado localmente. Pendiente de sincronización.",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent =
                        Intent(
                            this@activity_agregarcliente ,
                            activity_clientes::class.java
                        )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP

                    startActivity(intent)
                    finish()
                }
            }
        }



        // Botón abrir menú
        val btnMenu = findViewById<ImageView>(R.id.imgMenu)
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        /*// Botón cerrar menú
        val btnMenuVolver = findViewById<ImageView>(R.id.imgCerrarMenu)
        btnMenuVolver.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }*/

        val btnFacturas = findViewById<LinearLayout>(R.id.drawerLayoutFacturas)
        btnFacturas.setOnClickListener {
            val intent = Intent(this, activity_facturacion::class.java)
            startActivity(intent)
        }

        val btnCompras = findViewById<LinearLayout>(R.id.drawerLayoutCompras)
        btnCompras.setOnClickListener {
            val intent = Intent(this, activity_compras::class.java)
            startActivity(intent)
        }

        val btnClientes = findViewById<LinearLayout>(R.id.drawerLayoutClientes)
        btnClientes.setOnClickListener {
            val intent = Intent(this, activity_clientes::class.java)
            startActivity(intent)
        }

        val btnEmpleados = findViewById<LinearLayout>(R.id.drawerLayoutEmpleados)
        btnEmpleados.setOnClickListener {
            val intent = Intent(this, activity_empleados::class.java)
            startActivity(intent)
        }

        val btnInventario = findViewById<LinearLayout>(R.id.drawerLayoutInventario)
        btnInventario.setOnClickListener {
            val intent = Intent(this, activity_inventario::class.java)
            startActivity(intent)
        }

        val btnCaja = findViewById<LinearLayout>(R.id.drawerLayoutCaja)
        btnCaja.setOnClickListener {
            val intent = Intent(this, activity_caja::class.java)
            startActivity(intent)
        }


        val btnIndicadores = findViewById<LinearLayout>(R.id.LinearLayoutIndicadores)
        btnIndicadores.setOnClickListener {
            val intent = Intent(this, activity_indicadores::class.java)
            startActivity(intent)
        }

        val btnTendencias = findViewById<LinearLayout>(R.id.LinearLayoutTendencias)
        btnTendencias.setOnClickListener {
            val intent = Intent(this, activity_tendencias::class.java)
            startActivity(intent)
        }

        val btnIncidencias = findViewById<LinearLayout>(R.id.LinearLayoutIncidencias)
        btnIncidencias.setOnClickListener {
            val intent = Intent(this, activity_incidencias::class.java)
            startActivity(intent)
        }
        val btnInicio = findViewById<LinearLayout>(R.id.drawerLayoutInicio)
        btnInicio.setOnClickListener {
            val intent = Intent(this, activity_dashboard::class.java)
            startActivity(intent)
        }
        val btnSalir = findViewById<LinearLayout>(R.id.linearLayoutCerrarSesion)
        btnSalir.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }



    }
}