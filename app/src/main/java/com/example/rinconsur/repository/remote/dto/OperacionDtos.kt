package com.example.rinconsur.repository.remote.dto

import com.example.rinconsur.repository.model.Entrada
import com.example.rinconsur.repository.model.Registro
import com.example.rinconsur.repository.model.Turno

// ---------- Entradas (ms-entradas) ----------

data class EntradaDto(val id: Long, val nombre: String, val activa: Boolean)

fun EntradaDto.toModel() = Entrada(id = id, nombre = nombre, activa = activa)

// ---------- Turnos (ms-turnos) ----------

data class TurnoRequestDto(
    val usuarioId: Long,
    val entradaId: Long,
    val fotoRuta: String
)

data class TurnoDto(
    val id: Long,
    val usuarioId: Long,
    val entradaId: Long,
    val inicio: Long,
    val fotoRuta: String,
    val fin: Long?,
    val activo: Boolean
)

// La app sigue usando "accesoId" (Int) en sus pantallas
fun TurnoDto.toModel() = Turno(
    id = id,
    usuarioId = usuarioId,
    accesoId = entradaId.toInt(),
    inicio = inicio,
    fotoRuta = fotoRuta,
    fin = fin
)

// ---------- Registros (ms-registros) ----------

data class RegistroRequestDto(
    val turnoId: Long,
    val rangoEtario: String,
    val acompanantesAdultos: Int,
    val acompanantesNinos: Int,
    val compro: Boolean
)

data class RegistroDto(
    val id: Long,
    val turnoId: Long?,          // null solo en datos de ejemplo
    val entradaId: Long,
    val fechaHora: Long,
    val rangoEtario: String,
    val acompanantesAdultos: Int,
    val acompanantesNinos: Int,
    val compro: Boolean,
    val tamanoGrupo: Int
)

fun RegistroDto.toModel() = Registro(
    id = id,
    turnoId = turnoId ?: 0L,
    accesoId = entradaId.toInt(),
    fechaHora = fechaHora,
    rangoEtario = rangoEtario,
    acompanantesAdultos = acompanantesAdultos,
    acompanantesNinos = acompanantesNinos,
    compro = compro
)

// ---------- Métricas del dashboard (ms-registros) ----------

data class MetricasDto(
    val porEntrada: List<EntradaMetricasDto>,
    val perfilSinBolsa: List<BarraDatoDto>,
    val composicionSinBolsa: List<BarraDatoDto>,
    val hayEjemplo: Boolean
)

data class EntradaMetricasDto(
    val entradaId: Long,
    val gruposConBolsa: Int,
    val gruposSinBolsa: Int,
    val personas: Int
)

data class BarraDatoDto(val etiqueta: String, val cantidad: Int)

// Respuesta simple {"mensaje": "..."} (datos de ejemplo)
data class MensajeDto(val mensaje: String)