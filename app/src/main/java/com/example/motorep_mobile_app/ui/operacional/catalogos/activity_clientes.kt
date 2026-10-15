package com.example.motorep_mobile_app.ui.operacional.catalogos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motorep_mobile_app.Cliente_Adapter
import com.example.motorep_mobile_app.MainActivity
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.activity_agregarcliente
import com.example.motorep_mobile_app.activity_detallecliente
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_caja
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_compras
import com.example.motorep_mobile_app.ui.operacional.movimientos.activity_facturacion
import com.example.motorep_mobile_app.ui.reportes.activity_incidencias
import com.example.motorep_mobile_app.ui.reportes.activity_indicadores
import com.example.motorep_mobile_app.ui.reportes.activity_tendencias
import com.example.motorep_mobile_app.ui.dashboard.activity_dashboard
import com.google.android.material.navigation.NavigationView
import com.example.motorep_mobile_app.data.database.MotorepDatabase
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.motorep_mobile_app.data.remote.api.ApiService
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Cliente_ApiRepository
import com.example.motorep_mobile_app.viewmodel.Cliente_ApiViewModel

class activity_clientes : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navViews: NavigationView

    private lateinit var apiViewModel: Cliente_ApiViewModel
    private lateinit var apiRepository: Cliente_ApiRepository // NUEVO: necesario para sincronizarPendientes()
    private lateinit var viewModel: Cliente_Viewmodel
    private lateinit var adapter: Cliente_Adapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clientes)


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


        val database = MotorepDatabase.getDatabase(this)
        val dao = database.clienteDao()
        val repository = Cliente_Repository(dao)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
               if (modelClass.isAssignableFrom(Cliente_Viewmodel::class.java)) {
                   return Cliente_Viewmodel(repository) as T
               }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        val viewModel = ViewModelProvider(this, factory)[Cliente_Viewmodel::class.java]


        //accedemos al recicler view
        val reciclerViewCliente = findViewById<RecyclerView>(R.id.reciclerviewClientes)
        reciclerViewCliente.layoutManager = LinearLayoutManager(this)



        //cramos el adapter y usarlo en el recicler
        //val adapter = Cliente_Adapter()
        //reciclerViewCliente.adapter = adapter

        val adapter = Cliente_Adapter{cliente -> val intent = Intent(this, activity_detallecliente::class.java)
            intent.putExtra("id", cliente.id)
            intent.putExtra("nombre", cliente.nombre)
            intent.putExtra("cedula", cliente.cedula)
            intent.putExtra("telefono", cliente.telefono)
            startActivity(intent)
        }
        reciclerViewCliente.adapter = adapter

        //observamos los cambios en la lista de clientes y actualizamos el adapter
        viewModel.cliente.observe(this) {
            cliente -> adapter.submitList(cliente)
        }

        //boton flotante para agregar cliente
        val btnAgregar = findViewById<FloatingActionButton>(R.id.FloatingActionButtonAgregar)
        btnAgregar.setOnClickListener {
            val intent = Intent(this, activity_agregarcliente::class.java)
            startActivity(intent)
        }



        // GET remoto (API -> SQL Server)
        apiRepository = Cliente_ApiRepository(RetrofitClient.create(this))
        apiViewModel = Cliente_ApiViewModel(apiRepository)

        apiViewModel.obtenerClientes { listDto ->
            val listaClientes = listDto.map { dto ->
                Cliente(
                    id = dto.id,
                    nombre = dto.Nombres ?: "",
                    apellido = dto.Apellidos?: "",
                    cedula = dto.NumCedula?: "",
                    telefono = dto.NumTelefono?: ""
                )
            }
            adapter.submitList(listaClientes)
        }

    }
}