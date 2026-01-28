package com.example.taskmapfinal.bd

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DaoTareas {

    @Query("SELECT * FROM tareas_local WHERE idUsuario = :idUsuario ORDER BY fechaVencimiento IS NULL, fechaVencimiento ASC")
    suspend fun obtenerTareasUsuario(idUsuario: Int): List<EntidadTarea>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(lista: List<EntidadTarea>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUna(tarea: EntidadTarea)

    @Query("DELETE FROM tareas_local WHERE idUsuario = :idUsuario")
    suspend fun borrarTodasUsuario(idUsuario: Int)

    @Query("DELETE FROM tareas_local WHERE idUsuario = :idUsuario AND idTarea = :idTarea")
    suspend fun borrarUna(idUsuario: Int, idTarea: Long)
}
