package com.example.rinconsur.repository.remote.api

import com.example.rinconsur.repository.remote.dto.EntradaDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de ms-entradas (puerto 8082). */
interface EntradasApi {

    @GET("api/entradas")
    suspend fun listar(@Query("soloActivas") soloActivas: Boolean): List<EntradaDto>

    @GET("api/entradas/{id}")
    suspend fun buscar(@Path("id") id: Long): EntradaDto
}