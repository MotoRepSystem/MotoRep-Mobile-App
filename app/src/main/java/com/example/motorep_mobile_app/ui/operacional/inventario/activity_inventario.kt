package com.example.motorep_mobile_app.ui.operacional.inventario

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motorep_mobile_app.Producto_Adapter
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Producto_Repository
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.example.motorep_mobile_app.viewmodel.Productos_ViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class activity_inventario : AppCompatActivity() {

    private lateinit var adapter: Producto_Adapter
    private val db: Motorep_Database by lazy {
        Motorep_Database.getDatabase(applicationContext)
    }


    class Factory(
        private val repository: Producto_Repository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(Productos_ViewModel::class.java))
            return Productos_ViewModel(repository) as T
        }
    }

    private val viewModel: Productos_ViewModel by viewModels {
        Factory(Producto_Repository(RetrofitClient.create(this), db))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inventario)

        MenuLateral(this).configurar()

        adapter = Producto_Adapter { producto ->
            startActivity(
                Intent(this, activity_detalleproducto::class.java)
                    .putExtra(activity_detalleproducto.EXTRA_ID, producto.id)
            )
        }
        val recycler = findViewById<RecyclerView>(R.id.reciclerviewProductos)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val raiz = findViewById<View>(android.R.id.content)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.inventario.collect { adapter.submitList(it) } }
                launch {
                    viewModel.error.collect { msg ->
                        msg?.let {
                            Snackbar.make(raiz, it, Snackbar.LENGTH_LONG).show()
                            viewModel.limpiarError()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refrescar()
    }
}