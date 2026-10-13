package com.example.motorep_mobile_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.databinding.ItemClienteBinding

class Cliente_Adapter(private val onItemClick: (Cliente) -> Unit) : ListAdapter<Cliente, Cliente_Adapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemClienteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cliente: Cliente) {
            binding.txtNombre.text = cliente.nombre
            binding.txtCedula.text = cliente.cedula
            binding.txtCorreo.text = cliente.correo
            binding.txtTelefono.text = cliente.telefono

            // Detectar click sobre el Cliente
            binding.root.setOnClickListener {
                onItemClick(cliente)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClienteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
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
