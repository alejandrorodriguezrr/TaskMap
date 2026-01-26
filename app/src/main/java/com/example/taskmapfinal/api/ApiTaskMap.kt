import com.example.taskmapfinal.api.PeticionEtiquetaCrear
import com.example.taskmapfinal.api.PeticionRegistro
import com.example.taskmapfinal.api.PeticionTareaActualizar
import com.example.taskmapfinal.api.PeticionTareaBorrar
import com.example.taskmapfinal.api.PeticionTareaCrear
import com.example.taskmapfinal.api.RespuestaEtiquetaCrear
import com.example.taskmapfinal.api.RespuestaEtiquetasListar
import com.example.taskmapfinal.api.RespuestaLogin
import com.example.taskmapfinal.api.RespuestaRegistro
import com.example.taskmapfinal.api.RespuestaTareaActualizar
import com.example.taskmapfinal.api.RespuestaTareaBorrar
import com.example.taskmapfinal.api.RespuestaTareaCrear
import com.example.taskmapfinal.api.RespuestaTareasListar
import com.example.taskmapfinal.api.SolicitudLogin
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiTaskMap {

    @POST("registro.php")
    suspend fun registrar(@Body solicitud: PeticionRegistro): Response<RespuestaRegistro>

    @POST("usuarios_login.php")
    suspend fun iniciarSesion(@Body solicitud: SolicitudLogin): Response<RespuestaLogin>

    @GET("tareas_listar.php")
    suspend fun listarTareas(
        @Query("id_usuario") idUsuario: Int,
        @Query("estado") estado: String? = null
    ): Response<RespuestaTareasListar>

    @GET("tareas_listar.php")
    suspend fun listarTareas(@Query("id_usuario") idUsuario: Int): Response<RespuestaTareasListar>

    @POST("tareas_crear.php")
    suspend fun crearTarea(@Body solicitud: PeticionTareaCrear): Response<RespuestaTareaCrear>

    @POST("tareas_actualizar.php")
    suspend fun actualizarTarea(
        @Body peticion: PeticionTareaActualizar
    ): retrofit2.Response<RespuestaTareaActualizar>

    @POST("tareas_borrar.php")
    suspend fun borrarTarea(
        @Body peticion: PeticionTareaBorrar
    ): retrofit2.Response<RespuestaTareaBorrar>

    @GET("etiquetas_listar.php")
    suspend fun listarEtiquetas(@Query("id_usuario") idUsuario: Int): Response<RespuestaEtiquetasListar>

    @POST("etiquetas_crear.php")
    suspend fun crearEtiqueta(@Body solicitud: PeticionEtiquetaCrear): Response<RespuestaEtiquetaCrear>

    @GET("tarea_detalle.php")
    suspend fun obtenerDetalleTarea(
        @Query("id_usuario") idUsuario: Int,
        @Query("id_tarea") idTarea: Long
    ): retrofit2.Response<com.example.taskmapfinal.api.RespuestaTareaDetalle>


}
