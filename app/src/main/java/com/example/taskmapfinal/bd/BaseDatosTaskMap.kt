package com.example.taskmapfinal.bd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EntidadTarea::class],
    version = 1,
    exportSchema = false
)
abstract class BaseDatosTaskMap : RoomDatabase() {

    abstract fun daoTareas(): DaoTareas

    companion object {
        @Volatile private var instancia: BaseDatosTaskMap? = null

        fun obtenerInstancia(contexto: Context): BaseDatosTaskMap {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    BaseDatosTaskMap::class.java,
                    "taskmap_local.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
