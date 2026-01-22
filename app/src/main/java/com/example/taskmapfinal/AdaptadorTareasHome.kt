package com.example.taskmapfinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class AdaptadorTareasHome(
    private val alPulsar: (Tarea) -> Unit,
    private val alMarcarHecha: (Tarea) -> Unit
) : RecyclerView.Adapter<AdaptadorTareasHome.ViewHolder>() {

    private val elementos: MutableList<Tarea> = mutableListOf()

    fun actualizar(nuevaLista: List<Tarea>) {
        elementos.clear()
        elementos.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea_home, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.enlazar(elementos[position], alPulsar, alMarcarHecha)
    }

    override fun getItemCount(): Int = elementos.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val tvHora: TextView = itemView.findViewById(R.id.tvHora)
        private val chipEstado: Chip = itemView.findViewById(R.id.chipEstado)
        private val btnHecha: MaterialButton = itemView.findViewById(R.id.btnHecha)

        fun enlazar(
            tarea: Tarea,
            alPulsar: (Tarea) -> Unit,
            alMarcarHecha: (Tarea) -> Unit
        ) {
            tvTitulo.text = tarea.titulo
            tvHora.text = tarea.vencimientoTexto ?: ""

            when (tarea.estado) {
                EstadoTarea.PENDIENTE -> {
                    chipEstado.text = "Pendiente"
                    chipEstado.setChipBackgroundColorResource(R.color.chip_pendiente)
                    btnHecha.isEnabled = true
                }
                EstadoTarea.EN_PROGRESO -> {
                    chipEstado.text = "En progreso"
                    chipEstado.setChipBackgroundColorResource(R.color.chip_progreso)
                    btnHecha.isEnabled = true
                }
                EstadoTarea.HECHA -> {
                    chipEstado.text = "Hecha"
                    chipEstado.setChipBackgroundColorResource(R.color.chip_hecha)
                    btnHecha.isEnabled = false
                }
            }

            itemView.setOnClickListener { alPulsar(tarea) }
            btnHecha.setOnClickListener { alMarcarHecha(tarea) }
        }
    }
}
