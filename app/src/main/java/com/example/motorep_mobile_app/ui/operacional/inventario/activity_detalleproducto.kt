package com.example.motorep_mobile_app.ui.operacional.inventario

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.entity.partesDetalle
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.Producto_Repository
import com.example.motorep_mobile_app.databinding.ActivityDetalleproductoBinding
import com.example.motorep_mobile_app.viewmodel.ProductoDetalle_ViewModel
import kotlinx.coroutines.launch

class activity_detalleproducto : AppCompatActivity() {

    companion object { const val EXTRA_ID = "producto_id" }

    private lateinit var binding: ActivityDetalleproductoBinding
    private val productoId by lazy { intent.getIntExtra(EXTRA_ID, -1) }
    private val db: Motorep_Database by lazy {
        Motorep_Database.getDatabase(applicationContext)
    }

    class Factory(
        private val repository: Producto_Repository,
        private val id: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ProductoDetalle_ViewModel::class.java))
            return ProductoDetalle_ViewModel(repository, id) as T
        }
    }

    private val viewModel: ProductoDetalle_ViewModel by viewModels {
        Factory(Producto_Repository(RetrofitClient.create(this), db), productoId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetalleproductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (productoId == -1) { finish(); return }

        var yaMostrado = false
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.producto.collect { p ->
                    if (p == null) {
                        // Si ya había datos y desaparece, la sincronización lo borró
                        if (yaMostrado) finish()
                        return@collect
                    }
                    yaMostrado = true
                    val partes = p.partesDetalle()
                    binding.tvNombreProducto.text = partes.producto
                    binding.tvMarcaProducto.text = "Marca: ${partes.marca}"
                    binding.tvMotoProducto.text = "Moto: ${partes.moto}"
                    binding.tvCantidad.text = "Cantidad: ${p.cantidad}"
                    binding.tvPrecioProducto.text = "Venta: C$ ${p.precioVenta}"
                }
            }
        }
    }
}