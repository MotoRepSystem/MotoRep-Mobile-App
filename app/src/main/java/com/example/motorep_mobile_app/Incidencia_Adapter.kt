package com.example.motorep_mobile_app.ui.incidencias

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.motorep_mobile_app.data.incidencias.Incidencia
import com.example.motorep_mobile_app.databinding.ItemIncidenciaBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoFecha: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale("es", "NI"))

class IncidenciaAdapter :
    ListAdapter<Incidencia, IncidenciaAdapter.ViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<Incidencia>() {
        override fun areItemsTheSame(a: Incidencia, b: Incidencia) = a.id == b.id
        override fun areContentsTheSame(a: Incidencia, b: Incidencia) = a == b
    }

    class ViewHolder(private val binding: ItemIncidenciaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(i: Incidencia) {
            binding.txtCategoria.text =
                i.detalle?.categoria ?: "Sin categoría"

            binding.txtTipo.text = "Tipo: ${i.tipoIncidencia}"

            binding.txtFecha.text = "Fecha: ${formatearFecha(i.fechaRegistro)}"

            binding.txtOrigen.text = "Origen: ${i.origen?.texto() ?: "—"}"

            binding.txtRegistradoPor.text =
                "Registrada por: ${i.gestion?.empleadoId?.let { "Empleado #$it" } ?: "—"}"

            binding.txtMonto.text = "Monto: ${formatearMonto(i.detalle?.monto)}"

            binding.txtPrioridad.text =
                "Prioridad: ${i.gestion?.prioridad ?: "—"}"

            binding.txtDescripcion.text =
                "Descripción: ${i.detalle?.descripcion.orEmpty()}"
        }

        private fun formatearFecha(iso: String): String =
            try {
                Instant.parse(iso)
                    .atZone(ZoneId.systemDefault())
                    .format(formatoFecha)
            } catch (e: Exception) {
                iso // si el formato falla, mejor mostrar el dato crudo que nada
            }

        private fun formatearMonto(monto: String?): String =
            monto?.toBigDecimalOrNull()
                ?.let { "C\$ %,.2f".format(it) }
                ?: "—"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIncidenciaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}