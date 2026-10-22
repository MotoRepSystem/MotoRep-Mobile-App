package com.example.motorep_mobile_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.motorep_mobile_app.data.entity.Proveedor
import com.example.motorep_mobile_app.databinding.ItemProveedorBinding

class Proveedor_Adapter(
    private val onItemClick: (Proveedor) -> Unit
) : ListAdapter<Proveedor, Proveedor_Adapter.ViewHolder>(
    DiffCallback()
) {

    // ViewHolder encargado de representar cada producto
    class ViewHolder(
        private val binding: ItemProveedorBinding,
        private val onProveedorClick: (Proveedor) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // Vincula los datos del producto con la interfaz
        fun bind(proveedor: Proveedor) {

            binding.txtNombre.text = proveedor.nombre
            binding.txtTelefono.text = proveedor.telefono

            // Detectar click sobre el producto
            binding.root.setOnClickListener {
                onProveedorClick(proveedor)
            }
        }
    }

    // Crear el ViewHolder
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemProveedorBinding.inflate(
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
        DiffUtil.ItemCallback<Proveedor>() {

        // Determinar si es el mismo producto
        override fun areItemsTheSame(
            oldItem: Proveedor,
            newItem: Proveedor
        ): Boolean {

            return oldItem.id == newItem.id
        }

        // Determinar si cambiaron los datos
        override fun areContentsTheSame(
            oldItem: Proveedor,
            newItem: Proveedor
        ): Boolean {

            return oldItem == newItem
        }
    }
}
