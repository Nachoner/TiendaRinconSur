package com.example.rinconsur.repository

import com.example.rinconsur.repository.model.Turno
import com.example.rinconsur.repository.remote.ApiClient
import com.example.rinconsur.repository.remote.api.TurnosApi
import com.example.rinconsur.repository.remote.dto.TurnoRequestDto
import com.example.rinconsur.repository.remote.dto.toModel
import com.example.rinconsur.repository.remote.llamarApi
import retrofit2.HttpException

/** Acceso a ms-turnos. */
class TurnoRepository(
    private val api: TurnosApi = ApiClient.turnosApi
) {
    // null si el operador no tiene turno en curso (el backend responde 204)
    suspend fun turnoActivo(usuarioId: Long): Result<Turno?> = llamarApi {
        val respuesta = api.turnoActivo(usuarioId)
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
        respuesta.body()?.toModel()
    }

    suspend fun iniciar(usuarioId: Long, entradaId: Long, fotoRuta: String): Result<Turno> =
        llamarApi { api.iniciar(TurnoRequestDto(usuarioId, entradaId, fotoRuta)).toModel() }

    suspend fun finalizar(turnoId: Long): Result<Turno> =
        llamarApi { api.finalizar(turnoId).toModel() }
}