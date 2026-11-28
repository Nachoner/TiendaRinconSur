package com.example.rinconsur.repository.model

/** Salida de una persona o grupo (viene de ms-registros). */
data class Registro(
    val id: Long = 0,
    val turnoId: Long,                  // turno en el que se hizo el registro
    val accesoId: Int,                  // entrada (la toma el backend desde el turno)
    val fechaHora: Long,                // milisegundos (System.currentTimeMillis())
    val rangoEtario: String,            // de la persona principal: "18-25", "26-35", etc.
    val acompanantesAdultos: Int = 0,   // otros adultos que salen con ella
    val acompanantesNinos: Int = 0,     // niños que salen con ella
    val compro: Boolean                 // true = sale con bolsa, false = sale sin bolsa (transita)
)

// Personas totales del grupo: la principal + sus acompañantes
val Registro.tamanoGrupo: Int
    get() = 1 + acompanantesAdultos + acompanantesNinos