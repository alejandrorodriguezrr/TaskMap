package com.example.taskmapfinal.bd

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tareas_local")
data class EntidadTarea(
    @PrimaryKey val idTarea: Long,
    val idUsuario: Int,
    val titulo: String,
    val descripcion: String?,
    val prioridad: String,
    val estado: String,
    val fechaVencimiento: String?,
    val latitud: Double?,
    val longitud: Double?,
    val direccion: String?,
    val fechaCreacion: String?
)
