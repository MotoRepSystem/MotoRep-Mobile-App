package com.example.motorep_mobile_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.motorep_mobile_app.data.entity.Producto
import com.example.motorep_mobile_app.data.entity.partesDetalle
import com.example.motorep_mobile_app.databinding.ItemProductoBinding

class Producto_Adapter(
    private val onItemClick: (Producto) -> Unit
) : ListAdapter<Producto, Producto_Adapter.ViewHolder>(
    DiffCallback()
) {

    // ViewHolder encargado de representar cada producto
    class ViewHolder(
        private val binding: ItemProductoBinding,
        private val onProductoClick: (Producto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // Vincula los datos del producto con la interfaz
        fun bind(producto: Producto) {
            val partes = producto.partesDetalle()

            binding.txtNombreProducto.text = partes.producto
            binding.txtModelo.text = partes.marca
            binding.txtMoto.text = partes.moto
            binding.txtcantidad.text = producto.cantidad.toString()
            binding.txtPrecioVenta.text = producto.precioVenta


            // Detectar click sobre el producto
            binding.root.setOnClickListener {

                onProductoClick(producto)
            }
        }
    }

    // Crear el ViewHolder
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemProductoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return ViewHolder(
            binding,
            onItemClick
        )
    }

    // Vincular el producto con el ViewHolder
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    // Comparar los productos
    class DiffCallback :
        DiffUtil.ItemCallback<Producto>() {

        // Determinar si es el mismo producto
        override fun areItemsTheSame(
            oldItem: Producto,
            newItem: Producto
        ): Boolean {

            return oldItem.id == newItem.id
        }

        // Determinar si cambiaron los datos
        override fun areContentsTheSame(
            oldItem: Producto,
            newItem: Producto
        ): Boolean {

            return oldItem == newItem
        }
    }
}