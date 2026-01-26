package com.example.taskmapfinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskmapfinal.api.ClienteApi
import com.example.taskmapfinal.api.PeticionTareaActualizar
import com.example.taskmapfinal.api.PeticionTareaBorrar
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DetalleTarea : AppCompatActivity() {

    private lateinit var toolbarDetalle: MaterialToolbar

    private lateinit var tvTituloDetalle: TextView
    private lateinit var chipPrioridadDetalle: Chip
    private lateinit var chipEstadoDetalle: Chip
    private lateinit var tvDescripcionDetalle: TextView
    private lateinit var tvVencimientoDetalle: TextView
    private lateinit var tvUbicacionDetalle: TextView

    private lateinit var btnVerMapaDetalle: MaterialButton
    private lateinit var btnMarcarCompletada: MaterialButton
    private lateinit var btnEditarTarea: MaterialButton
    private lateinit var btnEliminarTarea: MaterialButton

    private var idUsuario: Int = 0
    private var idTarea: Long = 0L

    private var latitudActual: Double? = null
    private var longitudActual: Double? = null
    private var direccionActual: String? = null

    private val formatoSalidaUi = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.detalle_tarea)

        enlazarVistas()
        configurarToolbar()
        configurarEventos()

        idUsuario = obtenerIdUsuario()
        idTarea = intent.getLongExtra(EXTRA_ID_TAREA, 0L)

        if (idUsuario <= 0 || idTarea <= 0L) {
            Toast.makeText(this, "No hay sesión iniciada (id_usuario) o falta id_tarea", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarDetalleServidor()
    }

    private fun enlazarVistas() {
        toolbarDetalle = findViewById(R.id.toolbarDetalle)

        tvTituloDetalle = findViewById(R.id.tvTituloDetalle)
        chipPrioridadDetalle = findViewById(R.id.chipPrioridadDetalle)
        chipEstadoDetalle = findViewById(R.id.chipEstadoDetalle)
        tvDescripcionDetalle = findViewById(R.id.tvDescripcionDetalle)
        tvVencimientoDetalle = findViewById(R.id.tvVencimientoDetalle)
        tvUbicacionDetalle = findViewById(R.id.tvUbicacionDetalle)

        btnVerMapaDetalle = findViewById(R.id.btnVerMapaDetalle)
        btnMarcarCompletada = findViewById(R.id.btnMarcarCompletada)
        btnEditarTarea = findViewById(R.id.btnEditarTarea)
        btnEliminarTarea = findViewById(R.id.btnEliminarTarea)
    }

    private fun configurarToolbar() {
        setSupportActionBar(toolbarDetalle)
        toolbarDetalle.setNavigationOnClickListener { finish() }
    }

    private fun configurarEventos() {
        btnVerMapaDetalle.setOnClickListener { abrirMapaSiSePuede() }

        btnMarcarCompletada.setOnClickListener {
            actualizarEstado("hecha")
        }

        btnEliminarTarea.setOnClickListener {
            confirmarEliminar()
        }

        btnEditarTarea.setOnClickListener {
            val intent = Intent(this, NuevaTarea::class.java)
            intent.putExtra(NuevaTarea.EXTRA_ID_TAREA, idTarea)
            startActivity(intent)
        }
    }

    private fun cargarDetalleServidor() {
        mostrarCargando(true)

        lifecycleScope.launch {
            try {
                val respuestaHttp = ClienteApi.api.obtenerDetalleTarea(idUsuario, idTarea)

                if (!respuestaHttp.isSuccessful) {
                    mostrarError("Error HTTP: ${respuestaHttp.code()}")
                    return@launch
                }

                val cuerpo = respuestaHttp.body()
                if (cuerpo == null || !cuerpo.ok || cuerpo.tarea == null) {
                    mostrarError(cuerpo?.error ?: "No se pudo cargar el detalle")
                    return@launch
                }

                val t = cuerpo.tarea

                tvTituloDetalle.text = (t.titulo ?: "").ifBlank { "Sin título" }
                tvDescripcionDetalle.text = (t.descripcion ?: "").ifBlank { "Sin descripción" }

                val prioridad = (t.prioridad ?: "media").trim().lowercase()
                chipPrioridadDetalle.text = "Prioridad: " + when (prioridad) {
                    "baja" -> "Baja"
                    "alta" -> "Alta"
                    else -> "Media"
                }

                val estado = (t.estado ?: "pendiente").trim().lowercase()
                chipEstadoDetalle.text = when (estado) {
                    "en_progreso" -> "En progreso"
                    "hecha" -> "Hecha"
                    else -> "Pendiente"
                }

                val venc = t.fechaVencimiento
                tvVencimientoDetalle.text = formatearFechaVencimiento(venc)

                latitudActual = t.latitud
                longitudActual = t.longitud
                direccionActual = t.direccion

                tvUbicacionDetalle.text = when {
                    !direccionActual.isNullOrBlank() -> direccionActual!!
                    latitudActual != null && longitudActual != null -> "${latitudActual}, ${longitudActual}"
                    else -> "Sin ubicación"
                }

                mostrarCargando(false)

            } catch (_: IOException) {
                mostrarError("Error de conexión con el servidor")
            } catch (_: Exception) {
                mostrarError("Error inesperado")
            }
        }
    }

    private fun actualizarEstado(nuevoEstado: String) {
        btnMarcarCompletada.isEnabled = false

        lifecycleScope.launch {
            try {
                val respuestaHttp = ClienteApi.api.actualizarTarea(
                    PeticionTareaActualizar(
                        idUsuario = idUsuario,
                        idTarea = idTarea,
                        estado = nuevoEstado
                    )
                )

                btnMarcarCompletada.isEnabled = true

                if (!respuestaHttp.isSuccessful) {
                    Toast.makeText(this@DetalleTarea, "Error HTTP: ${respuestaHttp.code()}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val cuerpo = respuestaHttp.body()
                if (cuerpo != null && cuerpo.ok) {
                    Toast.makeText(this@DetalleTarea, "Tarea actualizada", Toast.LENGTH_SHORT).show()
                    cargarDetalleServidor()
                } else {
                    Toast.makeText(this@DetalleTarea, cuerpo?.error ?: "No se pudo actualizar", Toast.LENGTH_SHORT).show()
                }

            } catch (_: IOException) {
                btnMarcarCompletada.isEnabled = true
                Toast.makeText(this@DetalleTarea, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                btnMarcarCompletada.isEnabled = true
                Toast.makeText(this@DetalleTarea, "Error inesperado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar tarea")
            .setMessage("¿Seguro que quieres eliminar esta tarea?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarTarea() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarTarea() {
        btnEliminarTarea.isEnabled = false

        lifecycleScope.launch {
            try {
                val respuestaHttp = ClienteApi.api.borrarTarea(
                    PeticionTareaBorrar(
                        idUsuario = idUsuario,
                        idTarea = idTarea
                    )
                )

                btnEliminarTarea.isEnabled = true

                if (!respuestaHttp.isSuccessful) {
                    Toast.makeText(this@DetalleTarea, "Error HTTP: ${respuestaHttp.code()}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val cuerpo = respuestaHttp.body()
                if (cuerpo != null && cuerpo.ok && (cuerpo.borradas ?: 0) > 0) {
                    Toast.makeText(this@DetalleTarea, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@DetalleTarea, cuerpo?.error ?: "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                }

            } catch (_: IOException) {
                btnEliminarTarea.isEnabled = true
                Toast.makeText(this@DetalleTarea, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                btnEliminarTarea.isEnabled = true
                Toast.makeText(this@DetalleTarea, "Error inesperado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun abrirMapaSiSePuede() {
        val lat = latitudActual
        val lon = longitudActual

        if (lat == null || lon == null) {
            Toast.makeText(this, "Esta tarea no tiene coordenadas", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "No hay app de mapas disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatearFechaVencimiento(valor: String?): String {
        if (valor.isNullOrBlank()) return "Sin vencimiento"
        return try {
            // MySQL suele venir "yyyy-MM-dd HH:mm:ss"
            val dt = LocalDateTime.parse(valor.replace(" ", "T"))
            dt.format(formatoSalidaUi)
        } catch (_: Exception) {
            valor
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        val vis = if (cargando) View.INVISIBLE else View.VISIBLE
        tvTituloDetalle.visibility = vis
        tvDescripcionDetalle.visibility = vis
        chipPrioridadDetalle.visibility = vis
        chipEstadoDetalle.visibility = vis
        tvVencimientoDetalle.visibility = vis
        tvUbicacionDetalle.visibility = vis
    }

    private fun mostrarError(mensaje: String) {
        mostrarCargando(false)
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun obtenerIdUsuario(): Int {
        val prefs = getSharedPreferences("sesion_taskmap", MODE_PRIVATE)
        return prefs.getInt("id_usuario", 0)
    }

    companion object {
        const val EXTRA_ID_TAREA = "id_tarea"
    }
}
