package com.example.rinconsur.repository.model

/**
 * Tipo de usuario: Operador, Supervisor, etc. (viene de ms-usuarios)
 * nombre: máximo 30 caracteres y solo letras (se valida en Validaciones).
 */
data class TipoUsuario(
    val id: Long = 0,
    val nombre: String
) {
    companion object {
        const val MAX_LARGO = 30
    }
}

// Tipos base que crea el backend al arrancar. La lógica de la app depende
// de sus ids y no de sus nombres, por eso no se pueden editar ni eliminar.
object TiposBase {
    const val OPERADOR_ID = 1L
    const val SUPERVISOR_ID = 2L

    fun esBase(id: Long): Boolean = id == OPERADOR_ID || id == SUPERVISOR_ID
}