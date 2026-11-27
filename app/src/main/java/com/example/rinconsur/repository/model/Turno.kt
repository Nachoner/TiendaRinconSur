package com.example.rinconsur.repository.model

/** Turno de un operador en una entrada (viene de ms-turnos). */
data class Turno(
    val id: Long = 0,
    val usuarioId: Long,        // operador que hizo el turno
    val accesoId: Int,          // entrada donde se ubica el operador
    val inicio: Long,           // milisegundos (System.currentTimeMillis())
    val fotoRuta: String,       // ruta de la foto de la puerta en el teléfono
    val fin: Long? = null       // null = turno todavía activo
)