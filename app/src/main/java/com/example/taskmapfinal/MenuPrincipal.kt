package com.example.taskmapfinal

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmapfinal.api.ClienteApi
import com.example.taskmapfinal.api.PeticionTareaActualizar
import com.example.taskmapfinal.api.TareaApi
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.io.IOException


class MenuPrincipal : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggleMenu: ActionBarDrawerToggle

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
    private var idUsuario: Int = 0

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)

        // Importante: evita que el contenido se meta debajo de la status bar
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.parseColor("#2563EB")

        setContentView(R.layout.menu_principal)

        iniciarVistas()
        configurarToolbarYDrawer()
        configurarRecycler()
        iniciarListeners()

        idUsuario = obtenerIdUsuario()
        if (idUsuario <= 0) {
            Toast.makeText(this, "No hay sesión iniciada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarTareasServidor()
    }

    override fun onResume() {
        super.onResume()
        if (idUsuario > 0) cargarTareasServidor()
    }

    private fun iniciarVistas() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

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

    private fun configurarToolbarYDrawer() {
        setSupportActionBar(toolbarPrincipal)

        toggleMenu = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbarPrincipal,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )

        drawerLayout.addDrawerListener(toggleMenu)
        toggleMenu.syncState()

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_tareas -> {
                    abrirPantallaPorNombre("ListaTareas", mapOf(ListaTareas.EXTRA_ID_USUARIO to idUsuario))
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_mapa -> {
                    abrirPantallaPorNombre("Mapa")
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_nueva_tarea -> {
                    val intent = Intent(this, NuevaTarea::class.java)
                    intent.putExtra(NuevaTarea.EXTRA_ID_USUARIO, idUsuario)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_cerrar_sesion -> {
                    cerrarSesion()
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    private fun configurarRecycler() {
        adaptador = AdaptadorTareasHome(
            { tarea ->
                abrirPantallaPorNombre("DetalleTarea", mapOf(DetalleTarea.EXTRA_ID_TAREA to tarea.idTarea))
            },
            { tarea ->
                marcarComoHechaServidor(tarea.idTarea)
            }
        )

        rvProximasTareas.layoutManager = LinearLayoutManager(this)
        rvProximasTareas.adapter = adaptador
    }

    private fun iniciarListeners() {
        btnVerTareas.setOnClickListener {
            abrirPantallaPorNombre("ListaTareas", mapOf(ListaTareas.EXTRA_ID_USUARIO to idUsuario))
        }

        btnVerMapa.setOnClickListener { abrirPantallaPorNombre("Mapa") }

        btnNuevaTarea.setOnClickListener {
            val intent = Intent(this, NuevaTarea::class.java)
            intent.putExtra(NuevaTarea.EXTRA_ID_USUARIO, idUsuario)
            startActivity(intent)
        }
    }

    private fun cargarTareasServidor() {
        tvVacioProximas.visibility = View.VISIBLE
        rvProximasTareas.visibility = View.GONE
        tvVacioProximas.text = "Cargando..."

        lifecycleScope.launch {
            try {
                val respuestaHttp = ClienteApi.api.listarTareas(idUsuario)

                if (!respuestaHttp.isSuccessful) {
                    tvVacioProximas.text = "Error HTTP: ${respuestaHttp.code()}"
                    listaTareas.clear()
                    actualizarResumen()
                    adaptador.actualizar(emptyList())
                    return@launch
                }

                val cuerpo = respuestaHttp.body()
                if (cuerpo == null || !cuerpo.ok) {
                    tvVacioProximas.text = cuerpo?.error ?: "No se pudieron cargar las tareas"
                    listaTareas.clear()
                    actualizarResumen()
                    adaptador.actualizar(emptyList())
                    return@launch
                }

                val listaApi = cuerpo.obtenerTareas()

                listaTareas.clear()
                listaTareas.addAll(listaApi.map { convertirTareaApi(it) })

                actualizarResumen()
                actualizarListaProximas()

            } catch (_: IOException) {
                tvVacioProximas.text = "Error de conexión con el servidor"
                listaTareas.clear()
                actualizarResumen()
                adaptador.actualizar(emptyList())
            } catch (_: Exception) {
                tvVacioProximas.text = "Error inesperado"
                listaTareas.clear()
                actualizarResumen()
                adaptador.actualizar(emptyList())
            }
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

    private fun actualizarListaProximas() {
        val proximas = listaTareas
            .filter { it.estado != EstadoTarea.HECHA }
            .take(5)

        if (proximas.isEmpty()) {
            tvVacioProximas.visibility = View.VISIBLE
            rvProximasTareas.visibility = View.GONE
            tvVacioProximas.text = "No hay tareas próximas"
        } else {
            tvVacioProximas.visibility = View.GONE
            rvProximasTareas.visibility = View.VISIBLE
        }

        adaptador.actualizar(proximas)
    }

    private fun marcarComoHechaServidor(idTarea: Long) {
        lifecycleScope.launch {
            try {
                val respuestaHttp = ClienteApi.api.actualizarTarea(
                    PeticionTareaActualizar(
                        idUsuario = idUsuario,
                        idTarea = idTarea,
                        estado = "hecha"
                    )
                )

                if (!respuestaHttp.isSuccessful) {
                    Toast.makeText(this@MenuPrincipal, "Error HTTP: ${respuestaHttp.code()}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val cuerpo = respuestaHttp.body()
                if (cuerpo != null && cuerpo.ok) {
                    Toast.makeText(this@MenuPrincipal, "Marcada como hecha", Toast.LENGTH_SHORT).show()
                    cargarTareasServidor()
                } else {
                    Toast.makeText(this@MenuPrincipal, cuerpo?.error ?: "No se pudo actualizar", Toast.LENGTH_SHORT).show()
                }

            } catch (_: IOException) {
                Toast.makeText(this@MenuPrincipal, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(this@MenuPrincipal, "Error inesperado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertirTareaApi(t: TareaApi): Tarea {
        val id = (t.idTarea ?: 0L)
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

        val vencimientoTexto = t.fechaVencimiento ?: ""

        return Tarea(
            idTarea = id,
            titulo = titulo,
            descripcion = descripcion,
            prioridad = prioridad,
            estado = estado,
            vencimientoTexto = vencimientoTexto
        )
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
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No existe la pantalla: $nombreClaseSimple", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerIdUsuario(): Int {
        val prefs = getSharedPreferences("sesion_taskmap", MODE_PRIVATE)
        return prefs.getInt("id_usuario", 0)
    }

    private fun cerrarSesion() {
        val prefs = getSharedPreferences("sesion_taskmap", MODE_PRIVATE)
        prefs.edit().clear().apply()

        startActivity(Intent(this, Login::class.java))
        finish()
    }
}
