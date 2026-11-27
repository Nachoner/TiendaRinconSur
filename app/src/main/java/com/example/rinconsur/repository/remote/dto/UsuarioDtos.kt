package com.example.rinconsur.repository.remote.dto

import com.example.rinconsur.repository.model.TipoUsuario
import com.example.rinconsur.repository.model.Usuario
import com.example.rinconsur.repository.model.UsuarioConTipo

// ---------- Lo que se envía ----------

data class LoginRequestDto(val email: String, val clave: String)

data class UsuarioRequestDto(
    val nombre: String,
    val email: String,
    val clave: String?,          // null al editar = mantener la clave actual
    val tipoUsuarioId: Long
)

data class TipoUsuarioRequestDto(val nombre: String)

// ---------- Lo que se recibe ----------

data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val email: String,
    val tipoUsuarioId: Long,
    val tipoUsuarioNombre: String,
    val activo: Boolean
)

data class TipoUsuarioDto(val id: Long, val nombre: String, val base: Boolean)

// Formato de error de todos los microservicios
data class ErrorDto(val mensaje: String?, val errores: Map<String, String>?)

// ---------- Conversión a los modelos que usan las pantallas ----------

// La clave ya no viaja al teléfono: el backend la valida
fun UsuarioDto.toModel() = Usuario(
    id = id,
    nombre = nombre,
    email = email,
    claveHash = "",
    sal = "",
    tipoUsuarioId = tipoUsuarioId,
    activo = activo
)

fun TipoUsuarioDto.toModel() = TipoUsuario(id = id, nombre = nombre)

// La lista de usuarios de la app muestra cada usuario junto con su tipo
fun UsuarioDto.toUsuarioConTipo() = UsuarioConTipo(
    usuario = toModel(),
    tipo = TipoUsuario(id = tipoUsuarioId, nombre = tipoUsuarioNombre)
)