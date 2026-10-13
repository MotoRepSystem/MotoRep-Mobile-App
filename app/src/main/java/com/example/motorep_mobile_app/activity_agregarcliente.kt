package com.example.motorep_mobile_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import com.example.motorep_mobile_app.viewmodel.Cliente_Viewmodel
import com.example.motorep_mobile_app.data.database.MotorepDatabase
import com.google.android.material.navigation.NavigationView

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
        val edtCedula = findViewById<EditText>(R.id.edtCedula)
        val edtCorreo = findViewById<EditText>(R.id.edtCorreo)
        val edtTelefono = findViewById<EditText>(R.id.edtTelefono)

        val database = MotorepDatabase.getDatabase(this)
        val dao = database.clienteDao()
        val repository = Cliente_Repository(dao)

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
            val nombre = edtNombre.text.toString()
            val cedula = edtCedula.text.toString()
            val correo = edtCorreo.text.toString()
            val telefono = edtTelefono.text.toString()

            val cliente = Cliente(nombre = nombre, cedula = cedula, correo = correo, telefono = telefono)

            viewModel.guardarCliente(cliente)

            Toast.makeText(this, "Cliente guardado correctamente", Toast.LENGTH_SHORT).show()

            edtNombre.text.clear()
            edtCedula.text.clear()
            edtCorreo.text.clear()
            edtTelefono.text.clear()
        }


    }
}