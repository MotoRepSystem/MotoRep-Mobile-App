package com.example.motorep_mobile_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.databinding.ItemClienteBinding

class Cliente_Adapter(private val onItemClick: (Cliente) -> Unit) : ListAdapter<Cliente, Cliente_Adapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(private val binding: ItemClienteBinding, private val onClienteClick: (Cliente) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cliente: Cliente, onClienteClick: (Cliente) -> Unit) {

            // Le asignamos al elemento Nombre del recycler el atributo nombre del cliente.
            // Aquí nos permite unir la entidad con el recycler
            binding.txtNombre.text = cliente.nombre
            binding.txtApellido.text = cliente.apellido
            binding.txtCedula.text = cliente.cedula
            binding.txtTelefono.text = cliente.telefono
            binding.root.setOnClickListener { onClienteClick(cliente)}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClienteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        // Nos retorna un ViewHolder
        return ViewHolder(binding, onItemClick)
    }

    // Esta función nos permite colocar los datos que traemos de la entidad y los colocamos en un ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class DiffCallback : DiffUtil.ItemCallback<Cliente>() {
        override fun areItemsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
            return oldItem == newItem
        }
    }
}
