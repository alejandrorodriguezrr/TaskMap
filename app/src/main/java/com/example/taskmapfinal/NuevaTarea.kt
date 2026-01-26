package com.example.taskmapfinal

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskmapfinal.api.ClienteApi
import com.example.taskmapfinal.api.PeticionTareaCrear
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import java.io.IOException

class NuevaTarea : AppCompatActivity() {

    private lateinit var toolbarNuevaTarea: MaterialToolbar
    private var guardando: Boolean = false

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.nueva_tarea)

        toolbarNuevaTarea = findViewById(R.id.toolbarNuevaTarea)
        setSupportActionBar(toolbarNuevaTarea)
        toolbarNuevaTarea.setNavigationOnClickListener { finish() }

        configurarAccionGuardar()
    }

    private fun configurarAccionGuardar() {
        toolbarNuevaTarea.menu.clear()

        val itemGuardar: MenuItem = toolbarNuevaTarea.menu.add("Guardar")
        itemGuardar.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        itemGuardar.setOnMenuItemClickListener {
            if (!guardando) {
                mostrarDialogoNuevaTarea()
            }
            true
        }
    }

    private fun mostrarDialogoNuevaTarea() {
        val contenedorTitulo = TextInputLayout(this)
        contenedorTitulo.hint = "Título"

        val etTitulo = TextInputEditText(this)
        contenedorTitulo.addView(etTitulo)

        val contenedorDescripcion = TextInputLayout(this)
        contenedorDescripcion.hint = "Descripción (opcional)"

        val etDescripcion = TextInputEditText(this)
        contenedorDescripcion.addView(etDescripcion)

        val spinnerPrioridad = Spinner(this)
        val opcionesPrioridad = listOf("baja", "media", "alta")
        spinnerPrioridad.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            opcionesPrioridad
        )
        spinnerPrioridad.setSelection(1)

        val spinnerEstado = Spinner(this)
        val opcionesEstado = listOf("pendiente", "en_progreso", "hecha")
        spinnerEstado.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            opcionesEstado
        )
        spinnerEstado.setSelection(0)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)

            addView(contenedorTitulo)
            addView(contenedorDescripcion)

            val tvPrioridad = android.widget.TextView(this@NuevaTarea).apply {
                text = "Prioridad"
                setPadding(0, padding / 2, 0, 0)
            }
            addView(tvPrioridad)
            addView(spinnerPrioridad)

            val tvEstado = android.widget.TextView(this@NuevaTarea).apply {
                text = "Estado"
                setPadding(0, padding / 2, 0, 0)
            }
            addView(tvEstado)
            addView(spinnerEstado)
        }

        AlertDialog.Builder(this)
            .setTitle("Nueva tarea")
            .setView(layout)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = etTitulo.text?.toString()?.trim().orEmpty()
                val descripcion = etDescripcion.text?.toString()?.trim().orEmpty()
                val prioridad = spinnerPrioridad.selectedItem.toString()
                val estado = spinnerEstado.selectedItem.toString()

                if (titulo.isBlank()) {
                    Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                crearTareaServidor(
                    titulo = titulo,
                    descripcion = if (descripcion.isBlank()) null else descripcion,
                    prioridad = prioridad,
                    estado = estado
                )
            }
            .show()
    }

    private fun crearTareaServidor(
        titulo: String,
        descripcion: String?,
        prioridad: String,
        estado: String
    ) {
        val idUsuario = obtenerIdUsuario()
        if (idUsuario <= 0) {
            Toast.makeText(this, "No hay sesión iniciada", Toast.LENGTH_SHORT).show()
            return
        }

        guardando = true

        lifecycleScope.launch {
            try {
                val respuestaHttp = ClienteApi.api.crearTarea(
                    PeticionTareaCrear(
                        idUsuario = idUsuario,
                        titulo = titulo,
                        descripcion = descripcion,
                        prioridad = prioridad,
                        estado = estado,
                        fechaVencimiento = null,
                        idEtiqueta = null,
                        latitud = null,
                        longitud = null,
                        direccion = null
                    )
                )

                guardando = false

                if (respuestaHttp.isSuccessful) {
                    val cuerpo = respuestaHttp.body()
                    if (cuerpo != null && cuerpo.ok) {
                        Toast.makeText(this@NuevaTarea, "Tarea creada", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@NuevaTarea,
                            cuerpo?.error ?: "No se pudo crear la tarea",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val mensaje = when (respuestaHttp.code()) {
                        400 -> "Solicitud incorrecta"
                        404 -> "No existe tareas_crear.php en el servidor"
                        422 -> "Datos inválidos"
                        500 -> "Error interno del servidor"
                        else -> "Error HTTP: ${respuestaHttp.code()}"
                    }
                    Toast.makeText(this@NuevaTarea, mensaje, Toast.LENGTH_SHORT).show()
                }

            } catch (e: IOException) {
                guardando = false
                Toast.makeText(this@NuevaTarea, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                guardando = false
                Toast.makeText(this@NuevaTarea, "Error inesperado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerIdUsuario(): Int {
        val prefs = getSharedPreferences("sesion_taskmap", MODE_PRIVATE)
        return prefs.getInt("id_usuario", 0)
    }

}
