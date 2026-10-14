package com.example.motorep_mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.motorep_mobile_app.data.database.MotorepDatabase
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.example.motorep_mobile_app.databinding.ActivityEditarclienteBinding
import com.example.motorep_mobile_app.ui.operacional.catalogos.activity_clientes
import com.google.android.material.navigation.NavigationView

class activity_editarcliente : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navViews: NavigationView

    private lateinit var binding: ActivityEditarclienteBinding
    private lateinit var viewModel: Cliente_Viewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityEditarclienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = findViewById(R.id.DrawerLayout)
        navViews = findViewById(R.id.navigationView)

        val mainConstraint = findViewById<ConstraintLayout>(R.id.mainConstraint)
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayoutMainTitle)


        ViewCompat.setOnApplyWindowInsetsListener(binding.mainConstraint) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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


        // Inicializar el ViewModel
        val database = MotorepDatabase.getDatabase(applicationContext)
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
        viewModel = ViewModelProvider(this, factory)[Cliente_Viewmodel::class.java]




        val id = intent.getIntExtra("id", 0)
        val nombre = intent.getStringExtra("nombre")
        val Apellido = intent.getStringExtra("apellido")
        val cedula = intent.getStringExtra("cedula")
        val telefono = intent.getStringExtra("telefono")

        // Asignar los valores a los campos de la pantalla (UI)
        binding.edtNombre.setText(nombre)
        binding.edtApellido.setText(Apellido)
        binding.edtCedula.setText(cedula)
        binding.edtTelefono.setText(telefono)


        // --- AQUÍ DEBE IR EL LISTENER ---
        binding.btnActualizarCliente.setOnClickListener {

            val nuevoNombre = binding.edtNombre.text.toString()
            val nuevoApellido = binding.edtApellido.text.toString()
            val nuevaCedula = binding.edtCedula.text.toString()
            val nuevaTelefono = binding.edtTelefono.text.toString()

            // Validación básica de campos vacíos o incorrectos
            if (nuevoNombre.isEmpty() || nuevoApellido.isEmpty() || nuevaCedula.isEmpty() || nuevaTelefono.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Crear un objeto Cliente con los nuevos valores
            val clienteActualizado = Cliente(
                id = id,
                nombre = nuevoNombre,
                apellido = nuevoApellido,
                cedula = nuevaCedula,
                telefono = nuevaTelefono,)
            viewModel.actualizarCliente(clienteActualizado)

            Toast.makeText(
                this, "Cliente actualizado con éxito", Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this, activity_clientes::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

    }
}