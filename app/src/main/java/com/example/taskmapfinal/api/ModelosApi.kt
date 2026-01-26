package com.example.taskmapfinal.api

import com.google.gson.annotations.SerializedName

data class PeticionRegistro(
    val nombre: String,
    val correo: String,
    val contrasena: String
)

data class RespuestaRegistro(
    val ok: Boolean,
    @SerializedName("id_usuario") val id_usuario: Int? = null,
    val error: String? = null
)

data class SolicitudLogin(
    val correo: String,
    val contrasena: String
)

data class RespuestaLogin(
    val ok: Boolean,
    @SerializedName("id_usuario") val id_usuario: Int? = null,
    val error: String? = null
)

data class RespuestaTareasListar(
    val ok: Boolean,
    val tareas: List<TareaApi>? = null,
    val data: DataTareas? = null,
    val error: String? = null
) {
    fun obtenerTareas(): List<TareaApi> = tareas ?: data?.tareas ?: emptyList()
}

data class DataTareas(
    val tareas: List<TareaApi>? = null
)

data class TareaApi(
    @SerializedName("id_tarea") val idTarea: Long? = null,
    @SerializedName("id_usuario") val idUsuario: Long? = null,
    @SerializedName("id_etiqueta") val idEtiqueta: Long? = null,
    val titulo: String? = null,
    val descripcion: String? = null,
    val prioridad: String? = null,
    val estado: String? = null,
    @SerializedName("fecha_vencimiento") val fechaVencimiento: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val direccion: String? = null,
    @SerializedName("fecha_creacion") val fechaCreacion: String? = null
)

data class PeticionTareaCrear(
    @SerializedName("id_usuario") val idUsuario: Int,
    val titulo: String,
    val descripcion: String? = null,
    val prioridad: String = "media",
    val estado: String = "pendiente",
    @SerializedName("fecha_vencimiento") val fechaVencimiento: String? = null,
    @SerializedName("id_etiqueta") val idEtiqueta: Int? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val direccion: String? = null
)

data class RespuestaTareaCrear(
    val ok: Boolean,
    @SerializedName("id_tarea") val idTarea: Long? = null,
    val error: String? = null
)

data class PeticionTareaActualizar(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("id_tarea") val idTarea: Long,
    val titulo: String? = null,
    val descripcion: String? = null,
    val prioridad: String? = null,
    val estado: String? = null,
    @SerializedName("fecha_vencimiento") val fechaVencimiento: String? = null,
    @SerializedName("id_etiqueta") val idEtiqueta: Int? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val direccion: String? = null
)

data class RespuestaTareaActualizar(
    val ok: Boolean,
    val actualizadas: Int? = null,
    val error: String? = null
)

data class PeticionTareaBorrar(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("id_tarea") val idTarea: Long
)

data class RespuestaTareaBorrar(
    val ok: Boolean,
    val borradas: Int? = null,
    val error: String? = null
)

data class EtiquetaApi(
    @SerializedName("id_etiqueta") val idEtiqueta: Long? = null,
    @SerializedName("id_usuario") val idUsuario: Long? = null,
    val nombre: String? = null,
    @SerializedName("fecha_creacion") val fechaCreacion: String? = null
)

data class RespuestaEtiquetasListar(
    val ok: Boolean,
    val etiquetas: List<EtiquetaApi>? = null,
    val error: String? = null
)

data class PeticionEtiquetaCrear(
    @SerializedName("id_usuario") val idUsuario: Int,
    val nombre: String
)

data class RespuestaEtiquetaCrear(
    val ok: Boolean,
    @SerializedName("id_etiqueta") val idEtiqueta: Long? = null,
    val error: String? = null
)

data class RespuestaTareaDetalle(
    val ok: Boolean,
    val tarea: TareaApi? = null,
    val error: String? = null
)
