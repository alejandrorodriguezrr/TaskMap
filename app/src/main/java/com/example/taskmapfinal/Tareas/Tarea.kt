package com.example.taskmapfinal.Tareas

import com.example.taskmapfinal.Prioridad

data class Tarea(
    val idTarea: Long,
    val titulo: String,
    val descripcion: String?,
    val prioridad: Prioridad,
    var estado: EstadoTarea,
    val vencimientoTexto: String?
)