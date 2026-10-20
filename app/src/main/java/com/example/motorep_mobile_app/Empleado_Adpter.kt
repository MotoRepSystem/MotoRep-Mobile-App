
package com.example.motorep_mobile_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.motorep_mobile_app.data.entity.Empleado
import com.example.motorep_mobile_app.databinding.ItemClienteBinding

class Empleado_Adpter(
    private val onItemClick: (Empleado) -> Unit
) : ListAdapter<Empleado, Empleado_Adpter.ViewHolder>(DiffCallback()) {

    class ViewHolder(
        private val binding: ItemClienteBinding,
        private val onItemClick: (Empleado) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(empleado: Empleado) {
            binding.txtNombre.text = empleado.nombre
            binding.txtApellido.text = empleado.apellido
            binding.txtCedula.text = empleado.cedula
            binding.txtTelefono.text = empleado.telefono

            // Detecta cuando el usuario selecciona un empleado.
            binding.root.setOnClickListener {
                onItemClick(empleado)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemClienteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Empleado>() {

        override fun areItemsTheSame(
            oldItem: Empleado,
            newItem: Empleado
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Empleado,
            newItem: Empleado
        ): Boolean {
            return oldItem == newItem
        }
    }
}
