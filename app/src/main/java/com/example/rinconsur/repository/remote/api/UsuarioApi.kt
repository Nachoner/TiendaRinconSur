package com.example.rinconsur.repository.remote.api

import com.example.rinconsur.repository.remote.dto.LoginRequestDto
import com.example.rinconsur.repository.remote.dto.TipoUsuarioDto
import com.example.rinconsur.repository.remote.dto.TipoUsuarioRequestDto
import com.example.rinconsur.repository.remote.dto.UsuarioDto
import com.example.rinconsur.repository.remote.dto.UsuarioRequestDto
import retrofit2.http.*

/** Endpoints de ms-usuarios (puerto 8081). */
interface UsuariosApi {

    // Login
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequestDto): UsuarioDto

    // Usuarios
    @GET("api/usuarios")
    suspend fun listarUsuarios(): List<UsuarioDto>

    @GET("api/usuarios/{id}")
    suspend fun buscarUsuario(@Path("id") id: Long): UsuarioDto

    @POST("api/usuarios")
    suspend fun crearUsuario(@Body req: UsuarioRequestDto): UsuarioDto

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Long, @Body req: UsuarioRequestDto): UsuarioDto

    @PATCH("api/usuarios/{id}/activo")
    suspend fun cambiarActivo(@Path("id") id: Long, @Query("activo") activo: Boolean): UsuarioDto

    // Tipos de usuario
    @GET("api/tipos-usuario")
    suspend fun listarTipos(): List<TipoUsuarioDto>

    @POST("api/tipos-usuario")
    suspend fun crearTipo(@Body req: TipoUsuarioRequestDto): TipoUsuarioDto

    @PUT("api/tipos-usuario/{id}")
    suspend fun actualizarTipo(@Path("id") id: Long, @Body req: TipoUsuarioRequestDto): TipoUsuarioDto

    @DELETE("api/tipos-usuario/{id}")
    suspend fun eliminarTipo(@Path("id") id: Long)
}