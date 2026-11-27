package com.example.rinconsur.repository.remote.api

import com.example.rinconsur.repository.remote.dto.TurnoDto
import com.example.rinconsur.repository.remote.dto.TurnoRequestDto
import retrofit2.Response
import retrofit2.http.*

/** Endpoints de ms-turnos (puerto 8083). */
interface TurnosApi {

    // 200 con el turno, o 204 (sin cuerpo) si el operador no tiene turno en curso
    @GET("api/turnos/activo")
    suspend fun turnoActivo(@Query("usuarioId") usuarioId: Long): Response<TurnoDto>

    @GET("api/turnos")
    suspend fun listar(@Query("usuarioId") usuarioId: Long?): List<TurnoDto>

    @POST("api/turnos")
    suspend fun iniciar(@Body req: TurnoRequestDto): TurnoDto

    @PUT("api/turnos/{id}/finalizar")
    suspend fun finalizar(@Path("id") id: Long): TurnoDto
}