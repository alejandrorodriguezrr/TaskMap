package com.example.taskmapfinal

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class ListaTareas : AppCompatActivity() {

    private lateinit var toolbarListaTareas: MaterialToolbar
    private lateinit var etBuscar: TextInputEditText
    private lateinit var grupoEstados: MaterialButtonToggleGroup
    private lateinit var btnPendientes: MaterialButton
    private lateinit var btnEnProgreso: MaterialButton
    private lateinit var btnHechas: MaterialButton

    private lateinit var rvTareas: RecyclerView
    private lateinit var tvVacioLista: TextView
    private lateinit var fabNuevaTareaLista: ExtendedFloatingActionButton

    private lateinit var adaptador: AdaptadorListaTareas

    private val tareasCompletas: MutableList<Tarea> = mutableListOf()
    private var estadoSeleccionado: EstadoTarea = EstadoTarea.PENDIENTE
    private var textoBusqueda: String = ""

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.lista_tareas)

        iniciarVistas()
        configurarToolbar()
        configurarRecycler()
        configurarEventos()

        cargarDatosEjemplo()
        aplicarFiltros()
    }

    private fun iniciarVistas() {
        toolbarListaTareas = findViewById(R.id.toolbarListaTareas)
        etBuscar = findViewById(R.id.etBuscar)

        grupoEstados = findViewById(R.id.grupoEstados)
        btnPendientes = findViewById(R.id.btnPendientes)
        btnEnProgreso = findViewById(R.id.btnEnProgreso)
        btnHechas = findViewById(R.id.btnHechas)

        rvTareas = findViewById(R.id.rvTareas)
        tvVacioLista = findViewById(R.id.tvVacioLista)
        fabNuevaTareaLista = findViewById(R.id.fabNuevaTareaLista)
    }

    private fun configurarToolbar() {
        setSupportActionBar(toolbarListaTareas)
        toolbarListaTareas.setNavigationOnClickListener { finish() }
    }

    private fun configurarRecycler() {
        adaptador = AdaptadorListaTareas(
            onClick = { tarea ->
                Toast.makeText(this, "Abrir detalle: ${tarea.titulo}", Toast.LENGTH_SHORT).show()
            }
        )
        rvTareas.layoutManager = LinearLayoutManager(this)
        rvTareas.adapter = adaptador
    }

    private fun configurarEventos() {
        grupoEstados.check(R.id.btnPendientes)

        grupoEstados.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            estadoSeleccionado = when (checkedId) {
                R.id.btnPendientes -> EstadoTarea.PENDIENTE
                R.id.btnEnProgreso -> EstadoTarea.EN_PROGRESO
                else -> EstadoTarea.HECHA
            }
            aplicarFiltros()
        }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textoBusqueda = s?.toString()?.trim().orEmpty()
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fabNuevaTareaLista.setOnClickListener {
            abrirPantallaPorNombre("NuevaTarea")
        }
    }

    private fun aplicarFiltros() {
        val filtradas = tareasCompletas
            .filter { it.estado == estadoSeleccionado }
            .filter {
                if (textoBusqueda.isBlank()) true
                else it.titulo.contains(textoBusqueda, ignoreCase = true) ||
                        (it.descripcion?.contains(textoBusqueda, ignoreCase = true) == true)
            }

        if (filtradas.isEmpty()) {
            tvVacioLista.visibility = android.view.View.VISIBLE
            rvTareas.visibility = android.view.View.GONE
        } else {
            tvVacioLista.visibility = android.view.View.GONE
            rvTareas.visibility = android.view.View.VISIBLE
        }

        adaptador.actualizar(filtradas)
    }

    private fun abrirPantallaPorNombre(nombreClaseSimple: String) {
        val nombreCompleto = "$packageName.$nombreClaseSimple"
        val intent = Intent().setClassName(this, nombreCompleto)

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No existe la pantalla: $nombreClaseSimple", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosEjemplo() {
        tareasCompletas.clear()

        tareasCompletas.add(Tarea(1L, "Comprar pan", "Ir a la panadería", Prioridad.MEDIA, EstadoTarea.PENDIENTE, "Hoy 18:30"))
        tareasCompletas.add(Tarea(2L, "Estudiar Android", "Repasar layouts y RecyclerView", Prioridad.ALTA, EstadoTarea.EN_PROGRESO, "Mañana 20:00"))
        tareasCompletas.add(Tarea(3L, "Entregar memoria", "PDF final del proyecto", Prioridad.ALTA, EstadoTarea.HECHA, "Ayer 23:59"))
    }
}
