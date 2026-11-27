package com.example.rinconsur.repository.remote.api

import com.example.rinconsur.repository.remote.dto.MensajeDto
import com.example.rinconsur.repository.remote.dto.MetricasDto
import com.example.rinconsur.repository.remote.dto.RegistroDto
import com.example.rinconsur.repository.remote.dto.RegistroRequestDto
import retrofit2.http.*

/** Endpoints de ms-registros (puerto 8084). */
interface RegistrosApi {

    // ---------- CRUD ----------

    @GET("api/registros")
    suspend fun listar(@Query("turnoId") turnoId: Long?): List<RegistroDto>

    @GET("api/registros/{id}")
    suspend fun buscar(@Path("id") id: Long): RegistroDto

    @POST("api/registros")
    suspend fun crear(@Body req: RegistroRequestDto): RegistroDto

    @PUT("api/registros/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body req: RegistroRequestDto): RegistroDto

    @DELETE("api/registros/{id}")
    suspend fun eliminar(@Path("id") id: Long)

    // ---------- Métricas ----------

    // desde = milisegundos; el backend cuenta hasta "ahora"
    @GET("api/registros/metricas")
    suspend fun metricas(@Query("desde") desde: Long): MetricasDto

    // ---------- Datos de ejemplo ----------

    @POST("api/registros/ejemplo")
    suspend fun cargarEjemplo(): MensajeDto

    @DELETE("api/registros/ejemplo")
    suspend fun borrarEjemplo(): MensajeDto
}