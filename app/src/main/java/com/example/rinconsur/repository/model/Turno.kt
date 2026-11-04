package com.example.rinconsur.repository.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "turnos",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"]
        )
    ],
    indices = [Index("usuarioId")]
)
data class Turno(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,        // operador que hizo el turno
    val accesoId: Int,          // 1 a 4: acceso donde se ubica el operador
    val inicio: Long,           // System.currentTimeMillis()
    val fotoRuta: String,       // ruta de la foto de la puerta, tomada al iniciar el turno
    val fin: Long? = null       // null = turno todavía activo
)