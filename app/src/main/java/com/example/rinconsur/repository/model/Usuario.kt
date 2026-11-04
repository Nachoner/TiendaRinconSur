package com.example.rinconsur.repository.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(
            entity = TipoUsuario::class,
            parentColumns = ["id"],
            childColumns = ["tipoUsuarioId"],
            onDelete = ForeignKey.RESTRICT, // no se puede borrar un tipo en uso
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["email"], unique = true),
        Index("tipoUsuarioId")
    ]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,         // nombre para mostrar, ej. "Ana Pérez"
    val email: String,          // se usa para el login (único)
    val claveHash: String,      // la clave nunca se guarda en texto plano
    val sal: String,            // valor aleatorio usado para generar el hash
    val tipoUsuarioId: Long,    // FK -> tipos_usuario.id
    val activo: Boolean = true  // false = cuenta desactivada, no puede iniciar sesión
)

val Usuario.esOperador: Boolean get() = tipoUsuarioId == TiposBase.OPERADOR_ID
val Usuario.esSupervisor: Boolean get() = tipoUsuarioId == TiposBase.SUPERVISOR_ID