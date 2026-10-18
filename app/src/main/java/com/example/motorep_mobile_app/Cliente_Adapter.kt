package com.example.motorep_mobile_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.databinding.ItemClienteBinding

// Adapter encargado de mostrar los productos en el RecyclerView
class Cliente_Adapter(
    private val onItemClick: (Cliente) -> Unit
) : ListAdapter<Cliente, Cliente_Adapter.ViewHolder>(
    DiffCallback()
) {

    // ViewHolder encargado de representar cada producto
    class ViewHolder(
        private val binding: ItemClienteBinding,
        private val onClienteClick: (Cliente) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // Vincula los datos del producto con la interfaz
        fun bind(cliente: Cliente) {

            binding.txtNombre.text = cliente.nombre

            binding.txtApellido.text = cliente.apellido

            binding.txtCedula.text = cliente.cedula
            binding.txtTelefono.text = cliente.telefono


            // Detectar click sobre el producto
            binding.root.setOnClickListener {

                onClienteClick(cliente)
            }
        }
    }

    // Crear el ViewHolder
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemClienteBinding.inflate(
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
        DiffUtil.ItemCallback<Cliente>() {

        // Determinar si es el mismo producto
        override fun areItemsTheSame(
            oldItem: Cliente,
            newItem: Cliente
        ): Boolean {

            return oldItem.id == newItem.id
        }

        // Determinar si cambiaron los datos
        override fun areContentsTheSame(
            oldItem: Cliente,
            newItem: Cliente
        ): Boolean {

            return oldItem == newItem
        }
    }
}
