package com.example.taskmapfinal

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskmapfinal.api.ClienteApi
import com.example.taskmapfinal.api.TareaApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.util.Locale

class Mapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var toolbarMapa: MaterialToolbar
    private lateinit var btnConfirmarUbicacion: MaterialButton
    private lateinit var btnCancelarMapa: MaterialButton
    private lateinit var tvInfoMapa: TextView
    private lateinit var tvDireccionMapa: TextView

    private var mapa: GoogleMap? = null

    private var marcadorSeleccion: Marker? = null
    private val marcadoresTareas: MutableList<Marker> = mutableListOf()

    private var latSeleccionada: Double? = null
    private var lonSeleccionada: Double? = null
    private var direccionSeleccionada: String? = null

    private var soloVer: Boolean = false
    private var idUsuario: Int = 0

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.mapa)

        toolbarMapa = findViewById(R.id.toolbarMapa)
        btnConfirmarUbicacion = findViewById(R.id.btnConfirmarUbicacion)
        btnCancelarMapa = findViewById(R.id.btnCancelarMapa)
        tvInfoMapa = findViewById(R.id.tvInfoMapa)
        tvDireccionMapa = findViewById(R.id.tvDireccionMapa)

        setSupportActionBar(toolbarMapa)
        toolbarMapa.setNavigationOnClickListener { finish() }

        soloVer = intent.getBooleanExtra(EXTRA_SOLO_VER, false)
        idUsuario = obtenerIdUsuario()

        if (soloVer) {
            btnConfirmarUbicacion.visibility = View.GONE
            btnCancelarMapa.text = "Volver"
            btnCancelarMapa.setOnClickListener { finish() }
            tvInfoMapa.text = "Ubicación de la tarea"
        } else {
            btnConfirmarUbicacion.isEnabled = false
            btnConfirmarUbicacion.setOnClickListener { devolverUbicacion() }
            btnCancelarMapa.setOnClickListener {
                setResult(RESULT_CANCELED)
                finish()
            }
            tvInfoMapa.text = "Selecciona una ubicación"
        }

        val fragmento = supportFragmentManager.findFragmentById(R.id.fragmentMapa) as SupportMapFragment
        fragmento.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap

        val latIni = intent.getDoubleExtra(EXTRA_LATITUD_INICIAL, Double.NaN)
        val lonIni = intent.getDoubleExtra(EXTRA_LONGITUD_INICIAL, Double.NaN)

        val inicio = if (!latIni.isNaN() && !lonIni.isNaN()) {
            LatLng(latIni, lonIni)
        } else {
            LatLng(40.9701, -5.6635)
        }

        mapa?.moveCamera(CameraUpdateFactory.newLatLngZoom(inicio, 13f))

        // Si me pasan coords iniciales, pongo marcador de selección ahí
        if (!latIni.isNaN() && !lonIni.isNaN()) {
            moverMarcadorSeleccion(inicio)
        } else {
            tvDireccionMapa.text = "Dirección: --"
        }

        // Si NO es solo ver, al entrar cargamos los “simbolitos” de tareas sin completar
        if (!soloVer) {
            cargarMarcadoresTareasSinCompletar()
        }

        // Solo permitir elegir punto cuando NO es solo ver
        if (!soloVer) {
            mapa?.setOnMapClickListener { punto ->
                moverMarcadorSeleccion(punto)
            }
        }

        // Si pulsas en un marcador de tarea, abre su detalle
        mapa?.setOnInfoWindowClickListener { marker ->
            val idTarea = marker.tag as? Long ?: return@setOnInfoWindowClickListener
            val intent = Intent(this, DetalleTarea::class.java)
            intent.putExtra(DetalleTarea.EXTRA_ID_TAREA, idTarea)
            intent.putExtra(EXTRA_ID_USUARIO, idUsuario)
            startActivity(intent)
        }
    }

    private fun moverMarcadorSeleccion(punto: LatLng) {
        marcadorSeleccion?.remove()
        marcadorSeleccion = mapa?.addMarker(
            MarkerOptions()
                .position(punto)
                .title("Ubicación seleccionada")
        )

        latSeleccionada = punto.latitude
        lonSeleccionada = punto.longitude

        direccionSeleccionada = obtenerDireccion(punto.latitude, punto.longitude)

        tvInfoMapa.text = "Ubicación seleccionada"
        tvDireccionMapa.text = "Dirección: " + (direccionSeleccionada ?: "${punto.latitude}, ${punto.longitude}")

        if (!soloVer) {
            btnConfirmarUbicacion.isEnabled = true
        }
    }

    private fun cargarMarcadoresTareasSinCompletar() {
        if (idUsuario <= 0) return

        lifecycleScope.launch {
            try {
                val respuestaHttp = ClienteApi.api.listarTareas(idUsuario)

                if (!respuestaHttp.isSuccessful) return@launch
                val cuerpo = respuestaHttp.body() ?: return@launch
                if (!cuerpo.ok) return@launch

                val tareas: List<TareaApi> = cuerpo.obtenerTareas()

                val tareasConUbicacion = tareas
                    .filter { (it.estado ?: "").lowercase() != "hecha" }
                    .filter { it.latitud != null && it.longitud != null }

                limpiarMarcadoresTareas()

                for (t in tareasConUbicacion) {
                    val lat = t.latitud ?: continue
                    val lon = t.longitud ?: continue
                    val titulo = t.titulo?.takeIf { it.isNotBlank() } ?: "Tarea"
                    val estado = t.estado ?: "pendiente"
                    val prioridad = t.prioridad ?: "media"
                    val idTarea = t.idTarea ?: continue

                    val marcador = mapa?.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lon))
                            .title(titulo)
                            .snippet("Estado: $estado | Prioridad: $prioridad")
                    )

                    if (marcador != null) {
                        marcador.tag = idTarea
                        marcadoresTareas.add(marcador)
                    }
                }

            } catch (_: Exception) {
                // Si falla el servidor, no pasa nada: el mapa sigue funcionando
            }
        }
    }

    private fun limpiarMarcadoresTareas() {
        for (m in marcadoresTareas) m.remove()
        marcadoresTareas.clear()
    }

    private fun obtenerDireccion(lat: Double, lon: Double): String? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val lista = geocoder.getFromLocation(lat, lon, 1)
            if (lista != null && lista.isNotEmpty()) lista[0].getAddressLine(0) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun devolverUbicacion() {
        val lat = latSeleccionada
        val lon = lonSeleccionada

        if (lat == null || lon == null) {
            Toast.makeText(this, "Selecciona una ubicación en el mapa", Toast.LENGTH_SHORT).show()
            return
        }

        intent.putExtra(EXTRA_LATITUD, lat)
        intent.putExtra(EXTRA_LONGITUD, lon)
        intent.putExtra(EXTRA_DIRECCION, direccionSeleccionada ?: "")
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun obtenerIdUsuario(): Int {
        val extra = intent.getIntExtra(EXTRA_ID_USUARIO, 0)
        if (extra > 0) return extra

        val prefs = getSharedPreferences(PREFS_SESION, MODE_PRIVATE)
        return prefs.getInt(CLAVE_ID_USUARIO, 0)
    }

    companion object {
        const val EXTRA_LATITUD = "latitud"
        const val EXTRA_LONGITUD = "longitud"
        const val EXTRA_DIRECCION = "direccion"

        const val EXTRA_LATITUD_INICIAL = "latitud_inicial"
        const val EXTRA_LONGITUD_INICIAL = "longitud_inicial"

        const val EXTRA_SOLO_VER = "solo_ver"

        const val EXTRA_ID_USUARIO = "id_usuario"
        private const val PREFS_SESION = "sesion_taskmap"
        private const val CLAVE_ID_USUARIO = "id_usuario"
    }
}
