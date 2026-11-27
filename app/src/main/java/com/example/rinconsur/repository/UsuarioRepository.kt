package com.example.rinconsur.repository

import com.example.rinconsur.repository.model.Usuario
import com.example.rinconsur.repository.remote.ApiClient
import com.example.rinconsur.repository.remote.api.UsuariosApi
import com.example.rinconsur.repository.remote.dto.LoginRequestDto
import com.example.rinconsur.repository.remote.dto.toModel
import com.example.rinconsur.repository.remote.llamarApi

/**
 * Acceso a ms-usuarios. Los ViewModels usan esta clase y no Retrofit directamente,
 * así en las pruebas unitarias se puede reemplazar por una falsa.
 */
class UsuarioRepository(
    private val api: UsuariosApi = ApiClient.usuariosApi
) {
    suspend fun login(email: String, clave: String): Result<Usuario> =
        llamarApi { api.login(LoginRequestDto(email.trim().lowercase(), clave)).toModel() }
}