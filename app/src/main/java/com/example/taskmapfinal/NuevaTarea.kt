package com.example.taskmapfinal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class NuevaTarea : AppCompatActivity() {

    private lateinit var toolbarNuevaTarea: MaterialToolbar

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.nueva_tarea)

        toolbarNuevaTarea = findViewById(R.id.toolbarNuevaTarea)
        setSupportActionBar(toolbarNuevaTarea)
        toolbarNuevaTarea.setNavigationOnClickListener { finish() }
    }
}
