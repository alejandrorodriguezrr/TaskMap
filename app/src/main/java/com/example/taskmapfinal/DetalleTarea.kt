package com.example.taskmapfinal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class DetalleTarea : AppCompatActivity() {

    private lateinit var toolbarDetalle: MaterialToolbar

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.detalle_tarea)

        toolbarDetalle = findViewById(R.id.toolbarDetalle)
        setSupportActionBar(toolbarDetalle)
        toolbarDetalle.setNavigationOnClickListener { finish() }
    }
}
