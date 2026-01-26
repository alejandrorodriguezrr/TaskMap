package com.example.taskmapfinal

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.util.Locale

class Mapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var toolbarMapa: MaterialToolbar
    private lateinit var btnConfirmarUbicacion: MaterialButton
    private lateinit var btnCancelarMapa: MaterialButton
    private lateinit var tvInfoMapa: TextView
    private lateinit var tvDireccionMapa: TextView

    private var mapa: GoogleMap? = null
    private var marcador: Marker? = null

    private var latSeleccionada: Double? = null
    private var lonSeleccionada: Double? = null
    private var direccionSeleccionada: String? = null

    private var soloVer: Boolean = false

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
            LatLng(40.9701, -5.6635) // Salamanca
        }

        mapa?.moveCamera(CameraUpdateFactory.newLatLngZoom(inicio, 13f))

        if (!latIni.isNaN() && !lonIni.isNaN()) {
            moverMarcador(inicio)
        } else {
            tvDireccionMapa.text = "Dirección: --"
        }

        if (!soloVer) {
            mapa?.setOnMapClickListener { punto ->
                moverMarcador(punto)
            }
        }
    }

    private fun moverMarcador(punto: LatLng) {
        marcador?.remove()
        marcador = mapa?.addMarker(
            MarkerOptions()
                .position(punto)
                .title("Ubicación seleccionada")
        )

        latSeleccionada = punto.latitude
        lonSeleccionada = punto.longitude

        direccionSeleccionada = obtenerDireccion(punto.latitude, punto.longitude)

        tvDireccionMapa.text = "Dirección: " + (direccionSeleccionada ?: "${punto.latitude}, ${punto.longitude}")

        if (!soloVer) {
            btnConfirmarUbicacion.isEnabled = true
            tvInfoMapa.text = "Ubicación seleccionada"
        }
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

    companion object {
        const val EXTRA_LATITUD = "latitud"
        const val EXTRA_LONGITUD = "longitud"
        const val EXTRA_DIRECCION = "direccion"

        const val EXTRA_LATITUD_INICIAL = "latitud_inicial"
        const val EXTRA_LONGITUD_INICIAL = "longitud_inicial"

        const val EXTRA_SOLO_VER = "solo_ver"
    }
}
