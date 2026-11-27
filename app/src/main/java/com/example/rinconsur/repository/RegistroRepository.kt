package com.example.rinconsur.repository

import com.example.rinconsur.repository.model.Registro
import com.example.rinconsur.repository.remote.ApiClient
import com.example.rinconsur.repository.remote.api.RegistrosApi
import com.example.rinconsur.repository.remote.dto.MetricasDto
import com.example.rinconsur.repository.remote.dto.RegistroRequestDto
import com.example.rinconsur.repository.remote.dto.toModel
import com.example.rinconsur.repository.remote.llamarApi

/** Acceso a ms-registros. */
class RegistroRepository(
    private val api: RegistrosApi = ApiClient.registrosApi
) {
    // ---------- CRUD ----------

    suspend fun delTurno(turnoId: Long): Result<List<Registro>> =
        llamarApi { api.listar(turnoId).map { it.toModel() } }

    suspend fun buscar(id: Long): Result<Registro> =
        llamarApi { api.buscar(id).toModel() }

    suspend fun crear(
        turnoId: Long, rango: String, adultos: Int, ninos: Int, compro: Boolean
    ): Result<Registro> =
        llamarApi { api.crear(RegistroRequestDto(turnoId, rango, adultos, ninos, compro)).toModel() }

    suspend fun actualizar(
        id: Long, turnoId: Long, rango: String, adultos: Int, ninos: Int, compro: Boolean
    ): Result<Registro> =
        llamarApi { api.actualizar(id, RegistroRequestDto(turnoId, rango, adultos, ninos, compro)).toModel() }

    suspend fun eliminar(id: Long): Result<Unit> =
        llamarApi { api.eliminar(id) }

    // ---------- Dashboard ----------

    // GET /api/registros/metricas?desde=...
    suspend fun metricas(desde: Long): Result<MetricasDto> =
        llamarApi { api.metricas(desde) }

    // POST /api/registros/ejemplo  -> mensaje para mostrar
    suspend fun cargarEjemplo(): Result<String> =
        llamarApi { api.cargarEjemplo().mensaje }

    // DELETE /api/registros/ejemplo -> mensaje para mostrar
    suspend fun borrarEjemplo(): Result<String> =
        llamarApi { api.borrarEjemplo().mensaje }
}