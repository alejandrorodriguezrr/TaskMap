package com.example.taskmapfinal

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmapfinal.api.ClienteApi
import com.example.taskmapfinal.api.TareaApi
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.IOException

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

    private var filtroPrioridad: String = "Todas"
    private var filtroFecha: String = "Todas"
    private var filtroOrden: String = "Fecha"

    private var idUsuario: Int = 0

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.lista_tareas)

        iniciarVistas()
        configurarToolbar()
        configurarRecycler()
        configurarEventos()

        idUsuario = obtenerIdUsuario()
        if (idUsuario <= 0) {
            Toast.makeText(this, "No hay sesión iniciada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarTareasServidor()
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarListaTareas.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        toolbarListaTareas.setOnClickListener {
            abrirFiltrosOrdenar()
        }
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

        fabNuevaTareaLista.setOnLongClickListener {
            abrirFiltrosOrdenar()
            true
        }
    }

    private fun cargarTareasServidor() {
        tvVacioLista.visibility = View.VISIBLE
        rvTareas.visibility = View.GONE
        tvVacioLista.text = "Cargando tareas..."

        lifecycleScope.launch {
            try {
                val respuestaHttp = ClienteApi.api.listarTareas(idUsuario)

                if (respuestaHttp.isSuccessful) {
                    val cuerpo = respuestaHttp.body()

                    if (cuerpo != null && cuerpo.ok) {
                        val listaApi = cuerpo.tareas ?: emptyList()

                        tareasCompletas.clear()
                        tareasCompletas.addAll(listaApi.map { convertirTareaApi(it) })

                        aplicarFiltros()
                    } else {
                        val mensaje = cuerpo?.error ?: "No se pudieron cargar las tareas"
                        mostrarErrorLista(mensaje)
                    }
                } else {
                    val mensaje = when (respuestaHttp.code()) {
                        400 -> "Solicitud incorrecta (id_usuario inválido)"
                        404 -> "No se encuentra tareas_listar.php en el servidor"
                        500 -> "Error interno del servidor"
                        else -> "Error HTTP: ${respuestaHttp.code()}"
                    }
                    mostrarErrorLista(mensaje)
                }

            } catch (e: IOException) {
                mostrarErrorLista("Error de conexión con el servidor")
            } catch (e: Exception) {
                mostrarErrorLista("Error inesperado")
            }
        }
    }

    private fun mostrarErrorLista(mensaje: String) {
        tareasCompletas.clear()
        adaptador.actualizar(emptyList())
        rvTareas.visibility = View.GONE
        tvVacioLista.visibility = View.VISIBLE
        tvVacioLista.text = mensaje
    }

    private fun convertirTareaApi(t: com.example.taskmapfinal.api.TareaApi): Tarea {

        val id = (t.idTarea ?: 0).toLong()
        val titulo = t.titulo?.trim().takeUnless { it.isNullOrBlank() } ?: "Sin título"
        val descripcion = t.descripcion

        val estado = when ((t.estado ?: "").trim().lowercase()) {
            "pendiente", "pendientes" -> EstadoTarea.PENDIENTE
            "en_progreso", "en progreso", "progreso" -> EstadoTarea.EN_PROGRESO
            "hecha", "hecho", "completada", "completado" -> EstadoTarea.HECHA
            else -> EstadoTarea.PENDIENTE
        }

        val prioridad = when ((t.prioridad ?: "").trim().lowercase()) {
            "baja" -> Prioridad.BAJA
            "media" -> Prioridad.MEDIA
            "alta" -> Prioridad.ALTA
            else -> Prioridad.MEDIA
        }

        val fechaTexto = t.fechaVencimiento ?: t.fechaCreacion ?: ""

        // IMPORTANTE: parámetros posicionales (como en tus datos de ejemplo)
        return Tarea(
            id,
            titulo,
            descripcion,
            prioridad,
            estado,
            fechaTexto
        )
    }


    private fun aplicarFiltros() {
        var filtradas = tareasCompletas
            .filter { it.estado == estadoSeleccionado }
            .filter {
                if (textoBusqueda.isBlank()) true
                else it.titulo.contains(textoBusqueda, ignoreCase = true) ||
                        (it.descripcion?.contains(textoBusqueda, ignoreCase = true) == true)
            }

        filtradas = filtradas.filter { tarea ->
            when (filtroPrioridad) {
                "Baja" -> tarea.prioridad == Prioridad.BAJA
                "Media" -> tarea.prioridad == Prioridad.MEDIA
                "Alta" -> tarea.prioridad == Prioridad.ALTA
                else -> true
            }
        }

        filtradas = when (filtroOrden) {
            "Título" -> filtradas.sortedBy { it.titulo.lowercase() }
            "Prioridad" -> filtradas.sortedByDescending {
                when (it.prioridad) {
                    Prioridad.BAJA -> 1
                    Prioridad.MEDIA -> 2
                    Prioridad.ALTA -> 3
                }
            }
            else -> filtradas
        }

        if (filtradas.isEmpty()) {
            tvVacioLista.visibility = View.VISIBLE
            rvTareas.visibility = View.GONE
            tvVacioLista.text = "No hay tareas"
        } else {
            tvVacioLista.visibility = View.GONE
            rvTareas.visibility = View.VISIBLE
        }

        adaptador.actualizar(filtradas)
    }

    private fun abrirFiltrosOrdenar() {
        val intent = Intent(this, FiltrosOrdenar::class.java).apply {
            putExtra(FiltrosOrdenar.EXTRA_PRIORIDAD, filtroPrioridad)
            putExtra(FiltrosOrdenar.EXTRA_FECHA, filtroFecha)
            putExtra(FiltrosOrdenar.EXTRA_ORDEN, filtroOrden)
        }
        startActivityForResult(intent, CODIGO_FILTROS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CODIGO_FILTROS && resultCode == Activity.RESULT_OK && data != null) {
            filtroPrioridad = data.getStringExtra(FiltrosOrdenar.EXTRA_PRIORIDAD) ?: "Todas"
            filtroFecha = data.getStringExtra(FiltrosOrdenar.EXTRA_FECHA) ?: "Todas"
            filtroOrden = data.getStringExtra(FiltrosOrdenar.EXTRA_ORDEN) ?: "Fecha"

            Toast.makeText(
                this,
                "Filtros: $filtroPrioridad | $filtroFecha | $filtroOrden",
                Toast.LENGTH_SHORT
            ).show()

            aplicarFiltros()
        }
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

    private fun obtenerIdUsuario(): Int {
        // 1) si lo pasas por Intent (recomendado)
        val extra = intent.getIntExtra(EXTRA_ID_USUARIO, 0)
        if (extra > 0) return extra

        // 2) si lo guardas en SharedPreferences
        val prefs = getSharedPreferences(PREFS_SESION, MODE_PRIVATE)
        return prefs.getInt(CLAVE_ID_USUARIO, 0)
    }

    companion object {
        private const val CODIGO_FILTROS = 200

        const val EXTRA_ID_USUARIO = "id_usuario"

        private const val PREFS_SESION = "sesion_taskmap"
        private const val CLAVE_ID_USUARIO = "id_usuario"
    }

    override fun onResume() {
        super.onResume()
        if (idUsuario > 0) cargarTareasServidor()
    }

}
