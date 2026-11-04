package com.example.rinconsur.repository.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tipo de usuario: Operador, Supervisor, etc.
 * nombre: máximo 30 caracteres y solo letras (se valida en Validaciones).
 */
@Entity(
    tableName = "tipos_usuario",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class TipoUsuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
) {
    companion object {
        const val MAX_LARGO = 30
    }
}

// Tipos base que crea la base de datos al abrirse. La lógica de la app depende
// de sus ids y no de sus nombres, por eso no se pueden editar ni eliminar.
object TiposBase {
    const val OPERADOR_ID = 1L
    const val SUPERVISOR_ID = 2L

    fun esBase(id: Long): Boolean = id == OPERADOR_ID || id == SUPERVISOR_ID
}