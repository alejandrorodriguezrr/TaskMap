package com.example.taskmapfinal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar

class Mapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var toolbarMapa: MaterialToolbar
    private var mapa: GoogleMap? = null

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.mapa)

        toolbarMapa = findViewById(R.id.toolbarMapa)
        setSupportActionBar(toolbarMapa)
        toolbarMapa.setNavigationOnClickListener { finish() }

        val fragmento = supportFragmentManager.findFragmentById(R.id.fragmentMapa) as SupportMapFragment
        fragmento.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap

        // Ejemplo: Salamanca
        val salamanca = LatLng(40.9701, -5.6635)
        mapa?.addMarker(MarkerOptions().position(salamanca).title("Salamanca"))
        mapa?.moveCamera(CameraUpdateFactory.newLatLngZoom(salamanca, 13f))
    }
}
