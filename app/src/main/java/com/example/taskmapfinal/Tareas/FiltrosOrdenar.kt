package com.example.taskmapfinal.Tareas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmapfinal.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class FiltrosOrdenar : AppCompatActivity() {

    private lateinit var toolbarFiltros: MaterialToolbar

    private lateinit var actPrioridad: MaterialAutoCompleteTextView
    private lateinit var actFecha: MaterialAutoCompleteTextView
    private lateinit var actOrden: MaterialAutoCompleteTextView

    private lateinit var btnAplicarFiltros: MaterialButton
    private lateinit var btnLimpiarFiltros: MaterialButton

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.filtros_ordenar)

        iniciarVistas()
        configurarToolbar()
        cargarOpciones()
        cargarValoresIniciales()
        configurarEventos()
    }

    private fun iniciarVistas() {
        toolbarFiltros = findViewById(R.id.toolbarFiltros)

        actPrioridad = findViewById(R.id.actPrioridad)
        actFecha = findViewById(R.id.actFecha)
        actOrden = findViewById(R.id.actOrden)

        btnAplicarFiltros = findViewById(R.id.btnAplicarFiltros)
        btnLimpiarFiltros = findViewById(R.id.btnLimpiarFiltros)
    }

    private fun configurarToolbar() {
        setSupportActionBar(toolbarFiltros)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbarFiltros.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun cargarOpciones() {
        actPrioridad.setSimpleItems(arrayOf("Todas", "Baja", "Media", "Alta"))
        actFecha.setSimpleItems(arrayOf("Todas", "Hoy", "Esta semana", "Este mes"))
        actOrden.setSimpleItems(arrayOf("Fecha", "Prioridad", "Título"))
    }

    private fun cargarValoresIniciales() {
        // Si te llegan valores desde la lista, los mostramos al abrir
        val prioridad = intent.getStringExtra(EXTRA_PRIORIDAD) ?: "Todas"
        val fecha = intent.getStringExtra(EXTRA_FECHA) ?: "Todas"
        val orden = intent.getStringExtra(EXTRA_ORDEN) ?: "Fecha"

        actPrioridad.setText(prioridad, false)
        actFecha.setText(fecha, false)
        actOrden.setText(orden, false)
    }

    private fun configurarEventos() {
        btnLimpiarFiltros.setOnClickListener {
            actPrioridad.setText("Todas", false)
            actFecha.setText("Todas", false)
            actOrden.setText("Fecha", false)
        }

        btnAplicarFiltros.setOnClickListener {
            val prioridad = actPrioridad.text?.toString()?.trim().orEmpty().ifEmpty { "Todas" }
            val fecha = actFecha.text?.toString()?.trim().orEmpty().ifEmpty { "Todas" }
            val orden = actOrden.text?.toString()?.trim().orEmpty().ifEmpty { "Fecha" }

            val datos = Intent().apply {
                putExtra(EXTRA_PRIORIDAD, prioridad)
                putExtra(EXTRA_FECHA, fecha)
                putExtra(EXTRA_ORDEN, orden)
            }

            setResult(RESULT_OK, datos)
            finish()
        }
    }

    companion object {
        const val EXTRA_PRIORIDAD = "f_prioridad"
        const val EXTRA_FECHA = "f_fecha"
        const val EXTRA_ORDEN = "f_orden"
    }
}