package com.example.taskmapfinal.Tareas

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.taskmapfinal.Mapa
import com.example.taskmapfinal.R
import com.example.taskmapfinal.api.ClienteApi
import com.example.taskmapfinal.api.PeticionTareaActualizar
import com.example.taskmapfinal.api.PeticionTareaCrear
import com.example.taskmapfinal.api.TareaApi
import com.example.taskmapfinal.util.NotificacionesTaskMap
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class NuevaTarea : AppCompatActivity() {

    private lateinit var toolbarNuevaTarea: MaterialToolbar

    private lateinit var tilTituloTarea: TextInputLayout
    private lateinit var etTituloTarea: TextInputEditText

    private lateinit var tilDescripcionTarea: TextInputLayout
    private lateinit var etDescripcionTarea: TextInputEditText

    private lateinit var tilPrioridadTarea: TextInputLayout
    private lateinit var actPrioridadTarea: MaterialAutoCompleteTextView

    private lateinit var tilEstadoTarea: TextInputLayout
    private lateinit var actEstadoTarea: MaterialAutoCompleteTextView

    private lateinit var tilFechaTarea: TextInputLayout
    private lateinit var etFechaTarea: TextInputEditText

    private lateinit var tilHoraTarea: TextInputLayout
    private lateinit var etHoraTarea: TextInputEditText

    private lateinit var tvUbicacionSeleccionada: TextView
    private lateinit var btnCambiarEnMapa: MaterialButton

    private lateinit var btnGuardarTarea: MaterialButton
    private lateinit var btnCancelarTarea: MaterialButton

    private val formatoFechaUi = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val formatoHoraUi = DateTimeFormatter.ofPattern("HH:mm")
    private val formatoFechaVencimientoApi = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private var guardando = false
    private var cargando = false

    private var idUsuario: Int = 0
    private var idTarea: Long = 0L
    private var modoEdicion: Boolean = false

    private var latitud: Double? = null
    private var longitud: Double? = null
    private var direccion: String? = null

    private val lanzadorPermisoNotificaciones = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Si lo concede, perfecto. Si no, simplemente no se mostrará la notificación.
    }

    private fun tienePermisoNotificaciones(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun pedirPermisoNotificacionesSiHaceFalta() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !tienePermisoNotificaciones()) {
            lanzadorPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.nueva_tarea)

        enlazarVistas()
        configurarToolbar()
        configurarDesplegables()
        configurarFechaHora()
        configurarBotones()

        pedirPermisoNotificacionesSiHaceFalta()

        idUsuario = obtenerIdUsuario()
        if (idUsuario <= 0) {
            Toast.makeText(this, "No hay sesión iniciada (id_usuario)", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        idTarea = intent.getLongExtra(EXTRA_ID_TAREA, 0L)
        modoEdicion = idTarea > 0L

        if (modoEdicion) {
            toolbarNuevaTarea.title = "Editar tarea"
            btnGuardarTarea.text = "Guardar cambios"
            cargarDetalleParaEditar()
        } else {
            toolbarNuevaTarea.title = "Nueva tarea"
            btnGuardarTarea.text = "Guardar"
        }
    }

    private fun enlazarVistas() {
        toolbarNuevaTarea = findViewById(R.id.toolbarNuevaTarea)

        tilTituloTarea = findViewById(R.id.tilTituloTarea)
        etTituloTarea = findViewById(R.id.etTituloTarea)

        tilDescripcionTarea = findViewById(R.id.tilDescripcionTarea)
        etDescripcionTarea = findViewById(R.id.etDescripcionTarea)

        tilPrioridadTarea = findViewById(R.id.tilPrioridadTarea)
        actPrioridadTarea = findViewById(R.id.actPrioridadTarea)

        tilEstadoTarea = findViewById(R.id.tilEstadoTarea)
        actEstadoTarea = findViewById(R.id.actEstadoTarea)

        tilFechaTarea = findViewById(R.id.tilFechaTarea)
        etFechaTarea = findViewById(R.id.etFechaTarea)

        tilHoraTarea = findViewById(R.id.tilHoraTarea)
        etHoraTarea = findViewById(R.id.etHoraTarea)

        tvUbicacionSeleccionada = findViewById(R.id.tvUbicacionSeleccionada)
        btnCambiarEnMapa = findViewById(R.id.btnCambiarEnMapa)

        btnGuardarTarea = findViewById(R.id.btnGuardarTarea)
        btnCancelarTarea = findViewById(R.id.btnCancelarTarea)
    }

    private fun configurarToolbar() {
        setSupportActionBar(toolbarNuevaTarea)
        toolbarNuevaTarea.setNavigationOnClickListener { finish() }
    }

    private fun configurarDesplegables() {
        val prioridadesUi = listOf("Baja", "Media", "Alta")
        actPrioridadTarea.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, prioridadesUi)
        )
        actPrioridadTarea.setText("Media", false)

        val estadosUi = listOf("Pendiente", "En progreso", "Hecha")
        actEstadoTarea.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, estadosUi)
        )
        actEstadoTarea.setText("Pendiente", false)
    }

    private fun configurarFechaHora() {
        if (etFechaTarea.text.isNullOrBlank()) {
            etFechaTarea.setText(LocalDate.now().format(formatoFechaUi))
        }
        if (etHoraTarea.text.isNullOrBlank()) {
            etHoraTarea.setText(LocalTime.now().withSecond(0).withNano(0).format(formatoHoraUi))
        }

        etFechaTarea.setOnClickListener { abrirSelectorFecha() }
        tilFechaTarea.setOnClickListener { abrirSelectorFecha() }

        etHoraTarea.setOnClickListener { abrirSelectorHora() }
        tilHoraTarea.setOnClickListener { abrirSelectorHora() }
    }

    private fun configurarBotones() {
        btnCancelarTarea.setOnClickListener { finish() }

        btnCambiarEnMapa.setOnClickListener {
            val intent = Intent(this, Mapa::class.java)

            if (latitud != null && longitud != null) {
                intent.putExtra(Mapa.EXTRA_LATITUD_INICIAL, latitud!!)
                intent.putExtra(Mapa.EXTRA_LONGITUD_INICIAL, longitud!!)
            }

            lanzadorMapa.launch(intent)
        }

        btnGuardarTarea.setOnClickListener {
            if (guardando || cargando) return@setOnClickListener
            if (modoEdicion) actualizarTareaServidor() else crearTareaServidor()
        }
    }

    private val lanzadorMapa = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val data = resultado.data ?: return@registerForActivityResult

            latitud = data.getDoubleExtra(Mapa.EXTRA_LATITUD, 0.0)
            longitud = data.getDoubleExtra(Mapa.EXTRA_LONGITUD, 0.0)
            direccion = data.getStringExtra(Mapa.EXTRA_DIRECCION)

            tvUbicacionSeleccionada.text = when {
                !direccion.isNullOrBlank() -> direccion
                else -> "$latitud, $longitud"
            }
        }
    }

    private fun cargarDetalleParaEditar() {
        establecerEstadoUi(true)
        cargando = true

        lifecycleScope.launch {
            try {
                val resp = ClienteApi.api.obtenerDetalleTarea(idUsuario, idTarea)

                if (!resp.isSuccessful) {
                    Toast.makeText(this@NuevaTarea, "Error HTTP: ${resp.code()}", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val cuerpo = resp.body()
                if (cuerpo == null || !cuerpo.ok || cuerpo.tarea == null) {
                    Toast.makeText(this@NuevaTarea, cuerpo?.error ?: "No se pudo cargar la tarea", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                rellenarFormulario(cuerpo.tarea)

            } catch (_: IOException) {
                Toast.makeText(this@NuevaTarea, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
                finish()
            } catch (_: Exception) {
                Toast.makeText(this@NuevaTarea, "Error inesperado", Toast.LENGTH_SHORT).show()
                finish()
            } finally {
                cargando = false
                establecerEstadoUi(false)
            }
        }
    }

    private fun rellenarFormulario(t: TareaApi) {
        etTituloTarea.setText(t.titulo ?: "")
        etDescripcionTarea.setText(t.descripcion ?: "")

        actPrioridadTarea.setText(
            when ((t.prioridad ?: "media").trim().lowercase()) {
                "baja" -> "Baja"
                "alta" -> "Alta"
                else -> "Media"
            },
            false
        )

        actEstadoTarea.setText(
            when ((t.estado ?: "pendiente").trim().lowercase()) {
                "en_progreso", "en progreso", "progreso" -> "En progreso"
                "hecha", "hecho", "completada", "completado" -> "Hecha"
                else -> "Pendiente"
            },
            false
        )

        val fv = t.fechaVencimiento
        if (!fv.isNullOrBlank()) {
            try {
                val dt = LocalDateTime.parse(fv.replace(" ", "T"))
                etFechaTarea.setText(dt.toLocalDate().format(formatoFechaUi))
                etHoraTarea.setText(dt.toLocalTime().format(formatoHoraUi))
            } catch (_: Exception) {
            }
        }

        latitud = t.latitud
        longitud = t.longitud
        direccion = t.direccion

        tvUbicacionSeleccionada.text = when {
            !direccion.isNullOrBlank() -> direccion
            latitud != null && longitud != null -> "$latitud, $longitud"
            else -> "Sin ubicación"
        }
    }

    private fun crearTareaServidor() {
        limpiarErrores()

        val titulo = etTituloTarea.text?.toString()?.trim().orEmpty()
        val descripcion = etDescripcionTarea.text?.toString()?.trim().orEmpty()

        if (titulo.isBlank()) {
            tilTituloTarea.error = "El título es obligatorio"
            return
        }

        val prioridadApi = when (actPrioridadTarea.text?.toString()?.trim()?.lowercase()) {
            "baja" -> "baja"
            "alta" -> "alta"
            else -> "media"
        }

        val estadoApi = when (actEstadoTarea.text?.toString()?.trim()?.lowercase()) {
            "en progreso", "en_progreso", "progreso" -> "en_progreso"
            "hecha", "hecho", "completada", "completado" -> "hecha"
            else -> "pendiente"
        }

        val fechaVencimientoApi = construirFechaVencimientoApi()

        guardando = true
        establecerEstadoUi(true)

        lifecycleScope.launch {
            try {
                val resp = ClienteApi.api.crearTarea(
                    PeticionTareaCrear(
                        idUsuario = idUsuario,
                        titulo = titulo,
                        descripcion = if (descripcion.isBlank()) null else descripcion,
                        prioridad = prioridadApi,
                        estado = estadoApi,
                        fechaVencimiento = fechaVencimientoApi,
                        idEtiqueta = null,
                        latitud = latitud,
                        longitud = longitud,
                        direccion = direccion
                    )
                )

                if (!resp.isSuccessful) {
                    Toast.makeText(this@NuevaTarea, "Error HTTP: ${resp.code()}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val cuerpo = resp.body()
                if (cuerpo != null && cuerpo.ok) {
                    Toast.makeText(this@NuevaTarea, "Tarea creada", Toast.LENGTH_SHORT).show()

                    // Notificación tipo WhatsApp (heads-up)
                    if (tienePermisoNotificaciones()) {
                        NotificacionesTaskMap.mostrarTareaCreada(this@NuevaTarea, titulo)
                    }

                    finish()
                } else {
                    Toast.makeText(this@NuevaTarea, cuerpo?.error ?: "No se pudo crear la tarea", Toast.LENGTH_SHORT).show()
                }

            } catch (_: IOException) {
                Toast.makeText(this@NuevaTarea, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(this@NuevaTarea, "Error inesperado", Toast.LENGTH_SHORT).show()
            } finally {
                guardando = false
                establecerEstadoUi(false)
            }
        }
    }

    private fun actualizarTareaServidor() {
        limpiarErrores()

        val titulo = etTituloTarea.text?.toString()?.trim().orEmpty()
        val descripcion = etDescripcionTarea.text?.toString()?.trim().orEmpty()

        if (titulo.isBlank()) {
            tilTituloTarea.error = "El título es obligatorio"
            return
        }

        val prioridadApi = when (actPrioridadTarea.text?.toString()?.trim()?.lowercase()) {
            "baja" -> "baja"
            "alta" -> "alta"
            else -> "media"
        }

        val estadoApi = when (actEstadoTarea.text?.toString()?.trim()?.lowercase()) {
            "en progreso", "en_progreso", "progreso" -> "en_progreso"
            "hecha", "hecho", "completada", "completado" -> "hecha"
            else -> "pendiente"
        }

        val fechaVencimientoApi = construirFechaVencimientoApi()

        guardando = true
        establecerEstadoUi(true)

        lifecycleScope.launch {
            try {
                val resp = ClienteApi.api.actualizarTarea(
                    PeticionTareaActualizar(
                        idUsuario = idUsuario,
                        idTarea = idTarea,
                        titulo = titulo,
                        descripcion = if (descripcion.isBlank()) null else descripcion,
                        prioridad = prioridadApi,
                        estado = estadoApi,
                        fechaVencimiento = fechaVencimientoApi,
                        idEtiqueta = null,
                        latitud = latitud,
                        longitud = longitud,
                        direccion = direccion
                    )
                )

                if (!resp.isSuccessful) {
                    Toast.makeText(this@NuevaTarea, "Error HTTP: ${resp.code()}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val cuerpo = resp.body()
                if (cuerpo != null && cuerpo.ok) {
                    Toast.makeText(this@NuevaTarea, "Cambios guardados", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@NuevaTarea, cuerpo?.error ?: "No se pudo actualizar", Toast.LENGTH_SHORT).show()
                }

            } catch (_: IOException) {
                Toast.makeText(this@NuevaTarea, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(this@NuevaTarea, "Error inesperado", Toast.LENGTH_SHORT).show()
            } finally {
                guardando = false
                establecerEstadoUi(false)
            }
        }
    }

    private fun abrirSelectorFecha() {
        val cal = Calendar.getInstance()
        val dlg = DatePickerDialog(
            this,
            { _, year, month, day ->
                val fecha = LocalDate.of(year, month + 1, day)
                etFechaTarea.setText(fecha.format(formatoFechaUi))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dlg.show()
    }

    private fun abrirSelectorHora() {
        val cal = Calendar.getInstance()
        val dlg = TimePickerDialog(
            this,
            { _, hour, minute ->
                val hora = LocalTime.of(hour, minute)
                etHoraTarea.setText(hora.format(formatoHoraUi))
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        )
        dlg.show()
    }

    private fun construirFechaVencimientoApi(): String? {
        val fechaTxt = etFechaTarea.text?.toString()?.trim().orEmpty()
        val horaTxt = etHoraTarea.text?.toString()?.trim().orEmpty()

        if (fechaTxt.isBlank() || horaTxt.isBlank()) return null

        return try {
            val fecha = LocalDate.parse(fechaTxt, formatoFechaUi)
            val hora = LocalTime.parse(horaTxt, formatoHoraUi)
            LocalDateTime.of(fecha, hora).format(formatoFechaVencimientoApi)
        } catch (_: Exception) {
            null
        }
    }

    private fun limpiarErrores() {
        tilTituloTarea.error = null
        tilDescripcionTarea.error = null
        tilPrioridadTarea.error = null
        tilEstadoTarea.error = null
        tilFechaTarea.error = null
        tilHoraTarea.error = null
    }

    private fun establecerEstadoUi(bloqueado: Boolean) {
        btnGuardarTarea.isEnabled = !bloqueado
        btnCancelarTarea.isEnabled = !bloqueado
        btnCambiarEnMapa.isEnabled = !bloqueado

        etTituloTarea.isEnabled = !bloqueado
        etDescripcionTarea.isEnabled = !bloqueado
        actPrioridadTarea.isEnabled = !bloqueado
        actEstadoTarea.isEnabled = !bloqueado
        etFechaTarea.isEnabled = !bloqueado
        etHoraTarea.isEnabled = !bloqueado

        etFechaTarea.isClickable = !bloqueado
        etHoraTarea.isClickable = !bloqueado
    }

    private fun obtenerIdUsuario(): Int {
        val extra = intent.getIntExtra(EXTRA_ID_USUARIO, 0)
        if (extra > 0) return extra

        val prefs = getSharedPreferences(PREFS_SESION, MODE_PRIVATE)
        return prefs.getInt(CLAVE_ID_USUARIO, 0)
    }

    companion object {
        const val EXTRA_ID_USUARIO = "id_usuario"
        const val EXTRA_ID_TAREA = "id_tarea"

        private const val PREFS_SESION = "sesion_taskmap"
        private const val CLAVE_ID_USUARIO = "id_usuario"
    }
}