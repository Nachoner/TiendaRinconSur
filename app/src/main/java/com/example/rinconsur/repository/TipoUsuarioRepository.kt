package com.example.rinconsur.repository

import com.example.rinconsur.repository.model.TipoUsuario
import com.example.rinconsur.repository.remote.ApiClient
import com.example.rinconsur.repository.remote.api.UsuariosApi
import com.example.rinconsur.repository.remote.dto.TipoUsuarioRequestDto
import com.example.rinconsur.repository.remote.dto.toModel
import com.example.rinconsur.repository.remote.llamarApi

/** Acceso a los tipos de usuario (también viven en ms-usuarios). */
class TipoUsuarioRepository(
    private val api: UsuariosApi = ApiClient.usuariosApi
) {
    // GET /api/tipos-usuario
    suspend fun listar(): Result<List<TipoUsuario>> =
        llamarApi { api.listarTipos().map { it.toModel() } }

    // POST /api/tipos-usuario
    suspend fun crear(nombre: String): Result<TipoUsuario> =
        llamarApi { api.crearTipo(TipoUsuarioRequestDto(nombre)).toModel() }

    // PUT /api/tipos-usuario/{id}
    suspend fun actualizar(id: Long, nombre: String): Result<TipoUsuario> =
        llamarApi { api.actualizarTipo(id, TipoUsuarioRequestDto(nombre)).toModel() }

    // DELETE /api/tipos-usuario/{id}
    suspend fun eliminar(id: Long): Result<Unit> =
        llamarApi { api.eliminarTipo(id) }
}