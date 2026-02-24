package com.example.taskmapfinal.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taskmapfinal.R
import kotlin.random.Random

object NotificacionesTaskMap {

    private const val CANAL_ID = "taskmap_canal_tareas"
    private const val CANAL_NOMBRE = "Tareas"
    private const val CANAL_DESCRIPCION = "Notificaciones de tareas de TaskMap"

    private fun crearCanalSiHaceFalta(contexto: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CANAL_ID) != null) return

        val canal = NotificationChannel(
            CANAL_ID,
            CANAL_NOMBRE,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CANAL_DESCRIPCION
            enableVibration(true)
            enableLights(true)
        }

        manager.createNotificationChannel(canal)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun mostrarTareaCreada(contexto: Context, tituloTarea: String) {
        crearCanalSiHaceFalta(contexto)

        val texto = "Tarea creada: $tituloTarea"

        val notificacion = NotificationCompat.Builder(contexto, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion_taskmap)
            .setContentTitle("TaskMap")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(contexto).notify(Random.nextInt(), notificacion)
    }
}