package com.example.rinconsur.repository

import com.example.rinconsur.repository.model.Usuario
import com.example.rinconsur.repository.model.UsuarioConTipo
import com.example.rinconsur.repository.remote.ApiClient
import com.example.rinconsur.repository.remote.api.UsuariosApi
import com.example.rinconsur.repository.remote.dto.LoginRequestDto
import com.example.rinconsur.repository.remote.dto.UsuarioRequestDto
import com.example.rinconsur.repository.remote.dto.toModel
import com.example.rinconsur.repository.remote.dto.toUsuarioConTipo
import com.example.rinconsur.repository.remote.llamarApi

/**
 * Acceso a ms-usuarios (usuarios y login). Los ViewModels usan esta clase y no
 * Retrofit directamente, así en las pruebas unitarias se puede reemplazar por una falsa.
 */
class UsuarioRepository(
    private val api: UsuariosApi = ApiClient.usuariosApi
) {
    // POST /api/auth/login
    suspend fun login(email: String, clave: String): Result<Usuario> =
        llamarApi { api.login(LoginRequestDto(email.trim().lowercase(), clave)).toModel() }

    // GET /api/usuarios
    suspend fun listar(): Result<List<UsuarioConTipo>> =
        llamarApi { api.listarUsuarios().map { it.toUsuarioConTipo() } }

    // GET /api/usuarios/{id}
    suspend fun buscar(id: Long): Result<Usuario> =
        llamarApi { api.buscarUsuario(id).toModel() }

    // POST /api/usuarios
    suspend fun crear(nombre: String, email: String, clave: String, tipoUsuarioId: Long): Result<Usuario> =
        llamarApi { api.crearUsuario(UsuarioRequestDto(nombre, email, clave, tipoUsuarioId)).toModel() }

    // PUT /api/usuarios/{id}  (clave = null: se mantiene la actual)
    suspend fun actualizar(
        id: Long, nombre: String, email: String, clave: String?, tipoUsuarioId: Long
    ): Result<Usuario> =
        llamarApi { api.actualizarUsuario(id, UsuarioRequestDto(nombre, email, clave, tipoUsuarioId)).toModel() }

    // PATCH /api/usuarios/{id}/activo?activo=...
    suspend fun cambiarActivo(id: Long, activo: Boolean): Result<Usuario> =
        llamarApi { api.cambiarActivo(id, activo).toModel() }
}