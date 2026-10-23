package com.example.motorep_mobile_app.ui.operacional.catalogos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.remote.dto.Marca_dto

// Adaptador que muestra las marcas y comunica las acciones seleccionadas.
class MarcaAdapter(
    private var marcas: List<Marca_dto>,
    private val onEditar: (Marca_dto) -> Unit,
    private val onEliminar: (Marca_dto) -> Unit
) : RecyclerView.Adapter<MarcaAdapter.MarcaViewHolder>() {

    // Referencias a los controles de cada registro.
    class MarcaViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val txtNombreMarca: TextView =
            itemView.findViewById(R.id.txtNombreMarca)

        val txtTipoMarca: TextView =
            itemView.findViewById(R.id.txtTipoMarca)

        val btnEditarMarca: Button =
            itemView.findViewById(R.id.btnEditarMarca)

        val btnEliminarMarca: Button =
            itemView.findViewById(R.id.btnEliminarMarca)
    }

    // Crea la vista de cada marca usando item_marca.xml.
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MarcaViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_marca, parent, false)

        return MarcaViewHolder(vista)
    }

    // Muestra los datos y conecta los botones de cada marca.
    override fun onBindViewHolder(
        holder: MarcaViewHolder,
        position: Int
    ) {
        val marca = marcas[position]

        holder.txtNombreMarca.text = marca.nombre
        holder.txtTipoMarca.text = marca.tipoNombre ?: "Sin tipo"

        // Informa a la pantalla qué marca se quiere editar.
        holder.btnEditarMarca.setOnClickListener {
            onEditar(marca)
        }

        // Informa a la pantalla qué marca se quiere eliminar.
        holder.btnEliminarMarca.setOnClickListener {
            onEliminar(marca)
        }
    }

    // Devuelve la cantidad de marcas.
    override fun getItemCount(): Int = marcas.size

    // Actualiza la lista después de cargar, filtrar o modificar datos.
    fun actualizarMarcas(nuevaLista: List<Marca_dto>) {
        marcas = nuevaLista
        notifyDataSetChanged()
    }
}
