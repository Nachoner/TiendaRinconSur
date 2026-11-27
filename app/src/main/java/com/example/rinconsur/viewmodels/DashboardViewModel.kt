package com.example.rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.EntradaRepository
import com.example.rinconsur.repository.RegistroRepository
import com.example.rinconsur.repository.model.Entrada
import com.example.rinconsur.repository.remote.dto.MetricasDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

/** Período del dashboard. dias = null significa todo el historial. */
data class Periodo(val etiqueta: String, val dias: Int?)

/** Métricas de una entrada. Un grupo es un registro: una persona y sus acompañantes. */
data class AccesoMetricas(
    val accesoId: Int,
    val gruposConBolsa: Int = 0,
    val gruposSinBolsa: Int = 0,
    val personas: Int = 0
) {
    val grupos: Int get() = gruposConBolsa + gruposSinBolsa
    val fraccionConBolsa: Float get() = if (grupos == 0) 0f else gruposConBolsa.toFloat() / grupos
}

/** Una barra de un gráfico: texto y cantidad. */
data class BarraDato(val etiqueta: String, val cantidad: Int)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val registroRepo: RegistroRepository = RegistroRepository(),
    private val entradaRepo: EntradaRepository = EntradaRepository()
) : ViewModel() {

    /** Todo lo que muestra el dashboard, cargado de una vez desde el backend. */
    private data class DatosDashboard(
        val accesos: List<AccesoMetricas> = emptyList(),
        val perfil: List<BarraDato> = emptyList(),
        val composicion: List<BarraDato> = emptyList(),
        val hayEjemplo: Boolean = false
    )

    val periodos = listOf(
        Periodo("Hoy", 1),
        Periodo("7 días", 7),
        Periodo("30 días", 30),
        Periodo("Todo", null)
    )

    // Posición del período elegido (parte en "7 días")
    private val _periodo = MutableStateFlow(1)
    val periodo: StateFlow<Int> = _periodo.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun clearMessage() { _mensaje.value = null }

    // Cada vez que cambia el período o este número, se vuelven a pedir las métricas
    private val recarga = MutableStateFlow(0)

    private fun recargar() = recarga.update { it + 1 }

    // Se carga al abrir el dashboard (o el Home del supervisor) y al cambiar de período
    private val datos: StateFlow<DatosDashboard> = combine(_periodo, recarga) { posicion, _ -> posicion }
        .mapLatest { posicion -> cargarDatos(periodos[posicion]) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DatosDashboard())

    // Salidas con y sin bolsa por cada entrada
    val accesos: StateFlow<List<AccesoMetricas>> = datos.map { it.accesos }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Perfil etario de quienes salen sin bolsa (solo transitan)
    val perfilSinBolsa: StateFlow<List<BarraDato>> = datos.map { it.perfil }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cómo salen quienes no compran: solos, con adultos, con niños o ambos
    val composicionSinBolsa: StateFlow<List<BarraDato>> = datos.map { it.composicion }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ¿Ya hay datos de ejemplo cargados?
    val hayEjemplo: StateFlow<Boolean> = datos.map { it.hayEjemplo }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun seleccionarPeriodo(posicion: Int) { _periodo.value = posicion }

    /** Genera salidas ficticias de los últimos 14 días (en ms-registros). */
    fun cargarEjemplo() {
        viewModelScope.launch {
            _cargando.value = true
            registroRepo.cargarEjemplo()
                .onSuccess {
                    _mensaje.value = it
                    recargar()
                }
                .onFailure { _mensaje.value = it.message }
            _cargando.value = false
        }
    }

    /** Borra solo los datos de ejemplo. Los registros reales no se tocan. */
    fun borrarEjemplo() {
        viewModelScope.launch {
            _cargando.value = true
            registroRepo.borrarEjemplo()
                .onSuccess {
                    _mensaje.value = it
                    recargar()
                }
                .onFailure { _mensaje.value = it.message }
            _cargando.value = false
        }
    }

    // ---------- Carga desde el backend ----------

    private suspend fun cargarDatos(p: Periodo): DatosDashboard {
        // 1. Todas las entradas (también las inactivas, por el historial).
        //    Además guarda sus nombres para las tarjetas.
        val entradas = entradaRepo.listar(soloActivas = false).getOrElse { emptyList() }

        // 2. Métricas del período, calculadas por ms-registros
        return registroRepo.metricas(desde = calcularDesde(p)).fold(
            onSuccess = { m ->
                DatosDashboard(
                    accesos = armarAccesos(m, entradas),
                    perfil = m.perfilSinBolsa.map { BarraDato(it.etiqueta, it.cantidad) },
                    composicion = m.composicionSinBolsa.map { BarraDato(it.etiqueta, it.cantidad) },
                    hayEjemplo = m.hayEjemplo
                )
            },
            onFailure = { e ->
                _mensaje.value = e.message
                DatosDashboard()
            }
        )
    }

    // Una tarjeta por entrada, aunque no tenga salidas en el período
    private fun armarAccesos(m: MetricasDto, entradas: List<Entrada>): List<AccesoMetricas> {
        val ids = (entradas.map { it.id } + m.porEntrada.map { it.entradaId }).distinct().sorted()
        return ids.map { id ->
            val fila = m.porEntrada.firstOrNull { it.entradaId == id }
            AccesoMetricas(
                accesoId = id.toInt(),
                gruposConBolsa = fila?.gruposConBolsa ?: 0,
                gruposSinBolsa = fila?.gruposSinBolsa ?: 0,
                personas = fila?.personas ?: 0
            )
        }
    }

    // Inicio del período en milisegundos: hoy a las 00:00 menos (dias - 1)
    private fun calcularDesde(p: Periodo): Long {
        val dias = p.dias ?: return 0L
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -(dias - 1))
        }.timeInMillis
    }
}