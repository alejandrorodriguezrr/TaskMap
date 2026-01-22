package com.example.taskmapfinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class AdaptadorListaTareas(
    private val onClick: (Tarea) -> Unit
) : RecyclerView.Adapter<AdaptadorListaTareas.ViewHolder>() {

    private val elementos: MutableList<Tarea> = mutableListOf()

    fun actualizar(nuevaLista: List<Tarea>) {
        elementos.clear()
        elementos.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea_lista, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.enlazar(elementos[position], onClick)
    }

    override fun getItemCount(): Int = elementos.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val chipEstado: Chip = itemView.findViewById(R.id.chipEstado)
        private val chipPrioridad: Chip = itemView.findViewById(R.id.chipPrioridad)
        private val tvVencimiento: TextView = itemView.findViewById(R.id.tvVencimiento)

        fun enlazar(tarea: Tarea, onClick: (Tarea) -> Unit) {
            tvTitulo.text = tarea.titulo
            tvDescripcion.text = tarea.descripcion ?: ""
            tvVencimiento.text = if (tarea.vencimientoTexto.isNullOrBlank()) "Vence: --/--/----" else "Vence: ${tarea.vencimientoTexto}"

            when (tarea.estado) {
                EstadoTarea.PENDIENTE -> {
                    chipEstado.text = "Pendiente"
                    chipEstado.setChipBackgroundColorResource(R.color.chip_pendiente)
                }
                EstadoTarea.EN_PROGRESO -> {
                    chipEstado.text = "En progreso"
                    chipEstado.setChipBackgroundColorResource(R.color.chip_progreso)
                }
                EstadoTarea.HECHA -> {
                    chipEstado.text = "Hecha"
                    chipEstado.setChipBackgroundColorResource(R.color.chip_hecha)
                }
            }

            when (tarea.prioridad) {
                Prioridad.BAJA -> {
                    chipPrioridad.text = "Baja"
                    chipPrioridad.setChipBackgroundColorResource(R.color.prioridad_baja)
                }
                Prioridad.MEDIA -> {
                    chipPrioridad.text = "Media"
                    chipPrioridad.setChipBackgroundColorResource(R.color.prioridad_media)
                }
                Prioridad.ALTA -> {
                    chipPrioridad.text = "Alta"
                    chipPrioridad.setChipBackgroundColorResource(R.color.prioridad_alta)
                }
            }

            itemView.setOnClickListener { onClick(tarea) }
        }
    }
}
