package com.example.rinconsur.repository.model

import androidx.room.Embedded
import androidx.room.Relation

data class ResumenAcceso(
    val accesoId: Int,
    val compro: Boolean,
    val grupos: Int,      // cantidad de registros
    val personas: Int     // personas totales, contando acompañantes
)

data class PerfilEtario(
    val rangoEtario: String,
    val total: Int
)

data class PerfilGrupo(
    val acompanantesAdultos: Int,
    val acompanantesNinos: Int,
    val total: Int
)

// Un usuario junto con su tipo, para mostrar o decidir según el tipo
data class UsuarioConTipo(
    @Embedded val usuario: Usuario,
    @Relation(
        parentColumn = "tipoUsuarioId",
        entityColumn = "id"
    )
    val tipo: TipoUsuario
)