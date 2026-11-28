package com.example.rinconsur.repository.model

/**
 * Usuario de la app (viene de ms-usuarios).
 * La clave no está aquí: el backend la guarda con hash y la valida en el login.
 */
data class Usuario(
    val id: Long = 0,
    val nombre: String,         // nombre para mostrar, ej. "Ana Pérez"
    val email: String,          // se usa para el login (único)
    val tipoUsuarioId: Long,    // id del tipo de usuario
    val activo: Boolean = true  // false = cuenta desactivada, no puede iniciar sesión
)

val Usuario.esOperador: Boolean get() = tipoUsuarioId == TiposBase.OPERADOR_ID
val Usuario.esSupervisor: Boolean get() = tipoUsuarioId == TiposBase.SUPERVISOR_ID