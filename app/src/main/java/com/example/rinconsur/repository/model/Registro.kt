package com.example.rinconsur.repository.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registros",
    foreignKeys = [
        ForeignKey(
            entity = Turno::class,
            parentColumns = ["id"],
            childColumns = ["turnoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("turnoId")]
)
data class Registro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val turnoId: Long,                  // turno en el que se hizo el registro
    val accesoId: Int,                  // 1 a 4
    val fechaHora: Long,                // System.currentTimeMillis()
    val rangoEtario: String,            // de la persona principal: "18-25", "26-35", etc.
    val acompanantesAdultos: Int = 0,   // otros adultos que salen con ella
    val acompanantesNinos: Int = 0,     // niños que salen con ella
    val compro: Boolean                 // true = sale con bolsa, false = sale sin bolsa (transita)
)

// Personas totales del grupo: la principal + sus acompañantes
val Registro.tamanoGrupo: Int
    get() = 1 + acompanantesAdultos + acompanantesNinos