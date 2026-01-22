package com.example.taskmapfinal

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class MenuPrincipal : AppCompatActivity() {

    private lateinit var toolbarPrincipal: MaterialToolbar
    private lateinit var btnVerTareas: MaterialButton
    private lateinit var btnVerMapa: MaterialButton
    private lateinit var btnNuevaTarea: MaterialButton

    private lateinit var chipPendientes: Chip
    private lateinit var chipEnProgreso: Chip
    private lateinit var chipHechas: Chip

    private lateinit var rvProximasTareas: RecyclerView
    private lateinit var tvVacioProximas: TextView

    private lateinit var adaptador: AdaptadorTareasHome
    private val listaTareas: MutableList<Tarea> = mutableListOf()

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.menu_principal)

        iniciarVistas()
        configurarToolbar()
        configurarRecycler()
        iniciarListeners()

        cargarDatosEjemplo()
        actualizarResumen()
        actualizarLista()
    }

    private fun iniciarVistas() {
        toolbarPrincipal = findViewById(R.id.toolbarPrincipal)

        btnVerTareas = findViewById(R.id.btnVerTareas)
        btnVerMapa = findViewById(R.id.btnVerMapa)
        btnNuevaTarea = findViewById(R.id.btnNuevaTarea)

        chipPendientes = findViewById(R.id.chipPendientes)
        chipEnProgreso = findViewById(R.id.chipEnProgreso)
        chipHechas = findViewById(R.id.chipHechas)

        rvProximasTareas = findViewById(R.id.rvProximasTareas)
        tvVacioProximas = findViewById(R.id.tvVacioProximas)
    }

    private fun configurarToolbar() {
        setSupportActionBar(toolbarPrincipal)

        toolbarPrincipal.setNavigationOnClickListener {
            Toast.makeText(this, "Menú lateral pendiente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarRecycler() {
        adaptador = AdaptadorTareasHome(
            { tarea ->
                abrirPantallaPorNombre("DetalleTarea", mapOf("id_tarea" to tarea.idTarea))
            },
            { tarea ->
                tarea.estado = EstadoTarea.HECHA
                actualizarResumen()
                actualizarLista()
            }
        )


        rvProximasTareas.layoutManager = LinearLayoutManager(this)
        rvProximasTareas.adapter = adaptador
    }

    private fun iniciarListeners() {
        btnVerTareas.setOnClickListener { abrirPantallaPorNombre("ListaTareas") }
        btnVerMapa.setOnClickListener { abrirPantallaPorNombre("Mapa") }
        btnNuevaTarea.setOnClickListener { abrirPantallaPorNombre("NuevaTarea") }
    }

    private fun abrirPantallaPorNombre(nombreClaseSimple: String, extras: Map<String, Any?> = emptyMap()) {
        val nombreCompleto = "$packageName.$nombreClaseSimple"
        val intent = Intent().setClassName(this, nombreCompleto)

        for ((clave, valor) in extras) {
            when (valor) {
                is Int -> intent.putExtra(clave, valor)
                is Long -> intent.putExtra(clave, valor)
                is String -> intent.putExtra(clave, valor)
                is Boolean -> intent.putExtra(clave, valor)
            }
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No existe la pantalla: $nombreClaseSimple", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarResumen() {
        val pendientes = listaTareas.count { it.estado == EstadoTarea.PENDIENTE }
        val enProgreso = listaTareas.count { it.estado == EstadoTarea.EN_PROGRESO }
        val hechas = listaTareas.count { it.estado == EstadoTarea.HECHA }

        chipPendientes.text = pendientes.toString()
        chipEnProgreso.text = enProgreso.toString()
        chipHechas.text = hechas.toString()
    }

    private fun actualizarLista() {
        if (listaTareas.isEmpty()) {
            tvVacioProximas.visibility = android.view.View.VISIBLE
            rvProximasTareas.visibility = android.view.View.GONE
        } else {
            tvVacioProximas.visibility = android.view.View.GONE
            rvProximasTareas.visibility = android.view.View.VISIBLE
        }

        adaptador.actualizar(listaTareas)
    }

    private fun cargarDatosEjemplo() {
        listaTareas.clear()

        listaTareas.add(
            Tarea(
                idTarea = 1L,
                titulo = "Comprar pan",
                descripcion = "Ir a la panadería",
                prioridad = Prioridad.MEDIA,
                estado = EstadoTarea.PENDIENTE,
                vencimientoTexto = "Hoy 18:30"
            )
        )

        listaTareas.add(
            Tarea(
                idTarea = 2L,
                titulo = "Entregar trabajo",
                descripcion = "Subir el PDF final",
                prioridad = Prioridad.ALTA,
                estado = EstadoTarea.EN_PROGRESO,
                vencimientoTexto = "Hoy 20:00"
            )
        )
    }
}
