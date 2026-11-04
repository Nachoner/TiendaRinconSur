package com.example.rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.data.AppDatabase
import com.example.rinconsur.repository.model.PerfilGrupo
import com.example.rinconsur.repository.model.Registro
import com.example.rinconsur.repository.model.ResumenAcceso
import com.example.rinconsur.repository.model.Turno
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.random.Random

/** Período del dashboard. dias = null significa todo el historial. */
data class Periodo(val etiqueta: String, val dias: Int?)

/** Métricas de un acceso. Un grupo es un registro: una persona y sus acompañantes. */
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
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val registroDao = db.registroDao()
    private val turnoDao = db.turnoDao()
    private val usuarioDao = db.usuarioDao()

    val periodos = listOf(
        Periodo("Hoy", 1),
        Periodo("7 días", 7),
        Periodo("30 días", 30),
        Periodo("Todo", null)
    )

    // Posición del período elegido (parte en "7 días")
    private val _periodo = MutableStateFlow(1)
    val periodo: StateFlow<Int> = _periodo.asStateFlow()

    private val rango: Flow<Pair<Long, Long>> = _periodo.map { calcularRango(periodos[it]) }

    // Salidas con y sin bolsa por cada acceso
    val accesos: StateFlow<List<AccesoMetricas>> = rango
        .flatMapLatest { (desde, hasta) -> registroDao.resumenPorAcceso(desde, hasta) }
        .map { filas -> armarAccesos(filas) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), armarAccesos(emptyList()))

    // Perfil etario de quienes salen sin bolsa (solo transitan)
    val perfilSinBolsa: StateFlow<List<BarraDato>> = rango
        .flatMapLatest { (desde, hasta) -> registroDao.perfilEtario(desde, hasta, false) }
        .map { filas ->
            Validaciones.RANGOS_ETARIOS.map { rango ->
                BarraDato(rango, filas.firstOrNull { it.rangoEtario == rango }?.total ?: 0)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cómo salen quienes no compran: solos, con adultos, con niños o ambos
    val composicionSinBolsa: StateFlow<List<BarraDato>> = rango
        .flatMapLatest { (desde, hasta) -> registroDao.composicionGrupos(desde, hasta, false) }
        .map { filas -> armarComposicion(filas) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ¿Ya hay datos de ejemplo cargados?
    val hayEjemplo: StateFlow<Boolean> = turnoDao.contarPorFoto(MARCA)
        .map { it > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun clearMessage() { _mensaje.value = null }

    fun seleccionarPeriodo(posicion: Int) { _periodo.value = posicion }

    /** Genera turnos y registros ficticios de los últimos 14 días. */
    fun cargarEjemplo() {
        viewModelScope.launch {
            _cargando.value = true
            val resultado = withContext(Dispatchers.IO) { insertarEjemplo() }
            _mensaje.value = resultado
            _cargando.value = false
        }
    }

    /** Borra solo los datos de ejemplo. Los registros reales no se tocan. */
    fun borrarEjemplo() {
        viewModelScope.launch {
            _cargando.value = true
            withContext(Dispatchers.IO) { turnoDao.eliminarPorFoto(MARCA) }
            _mensaje.value = "Datos de ejemplo eliminados"
            _cargando.value = false
        }
    }

    private fun calcularRango(p: Periodo): Pair<Long, Long> {
        val dias = p.dias ?: return Pair(0L, Long.MAX_VALUE)
        val desde = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -(dias - 1))
        }.timeInMillis
        return Pair(desde, Long.MAX_VALUE)
    }

    private fun armarAccesos(filas: List<ResumenAcceso>): List<AccesoMetricas> =
        Validaciones.ACCESOS.map { id ->
            val delAcceso = filas.filter { it.accesoId == id }
            AccesoMetricas(
                accesoId = id,
                gruposConBolsa = delAcceso.filter { it.compro }.sumOf { it.grupos },
                gruposSinBolsa = delAcceso.filter { !it.compro }.sumOf { it.grupos },
                personas = delAcceso.sumOf { it.personas }
            )
        }

    private fun armarComposicion(filas: List<PerfilGrupo>): List<BarraDato> {
        fun suma(condicion: (PerfilGrupo) -> Boolean) = filas.filter(condicion).sumOf { it.total }
        return listOf(
            BarraDato("Solos", suma { it.acompanantesAdultos == 0 && it.acompanantesNinos == 0 }),
            BarraDato("Con adultos", suma { it.acompanantesAdultos > 0 && it.acompanantesNinos == 0 }),
            BarraDato("Con niños", suma { it.acompanantesAdultos == 0 && it.acompanantesNinos > 0 }),
            BarraDato("Con adultos y niños", suma { it.acompanantesAdultos > 0 && it.acompanantesNinos > 0 })
        )
    }

    // Datos ficticios: un turno por acceso y por día, con una mezcla distinta en cada acceso
    private suspend fun insertarEjemplo(): String {
        if (turnoDao.contarPorFoto(MARCA).first() > 0) return "Los datos de ejemplo ya están cargados"
        val operador = usuarioDao.buscarPorEmail("operador@duoc.cl")?.usuario
            ?: return "No se encontró el usuario operador de prueba"

        val azar = Random(42)
        val ahora = System.currentTimeMillis()
        val registros = mutableListOf<Registro>()

        for (diasAtras in 0 until 14) {
            val diaBase = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -diasAtras)
            }.timeInMillis
            val inicio = minOf(diaBase, ahora - 6 * HORA)
            val fin = inicio + 6 * HORA

            for (acceso in Validaciones.ACCESOS) {
                val turnoId = turnoDao.insertar(
                    Turno(
                        usuarioId = operador.id,
                        accesoId = acceso,
                        inicio = inicio,
                        fotoRuta = MARCA,
                        fin = fin
                    )
                )
                repeat(azar.nextInt(10, 23)) {
                    val compro = azar.nextDouble() < PROB_COMPRA[acceso - 1]
                    val pesosRango = if (compro) PESOS_CON_BOLSA else PESOS_SIN_BOLSA
                    registros += Registro(
                        turnoId = turnoId,
                        accesoId = acceso,
                        fechaHora = azar.nextLong(inicio, fin),
                        rangoEtario = elegir(azar, Validaciones.RANGOS_ETARIOS.zip(pesosRango)),
                        acompanantesAdultos = elegir(azar, if (compro) ADULTOS_CON_BOLSA else ADULTOS_SIN_BOLSA),
                        acompanantesNinos = elegir(azar, if (compro) NINOS_CON_BOLSA else NINOS_SIN_BOLSA),
                        compro = compro
                    )
                }
            }
        }
        registroDao.insertarTodos(registros)
        return "Se cargaron ${registros.size} salidas de ejemplo"
    }

    // Elige un valor al azar según su peso
    private fun <T> elegir(azar: Random, opciones: List<Pair<T, Int>>): T {
        var punto = azar.nextInt(opciones.sumOf { it.second })
        for ((valor, peso) in opciones) {
            if (punto < peso) return valor
            punto -= peso
        }
        return opciones.last().first
    }

    companion object {
        private const val MARCA = "ejemplo"
        private const val HORA = 60L * 60L * 1000L

        // Probabilidad de salir con bolsa en los accesos 1, 2, 3 y 4
        private val PROB_COMPRA = listOf(0.55, 0.20, 0.45, 0.25)

        // Pesos por rango etario, en el orden de Validaciones.RANGOS_ETARIOS
        private val PESOS_SIN_BOLSA = listOf(10, 28, 25, 18, 12, 7)
        private val PESOS_CON_BOLSA = listOf(3, 12, 25, 30, 20, 10)

        private val ADULTOS_CON_BOLSA = listOf(0 to 50, 1 to 35, 2 to 15)
        private val ADULTOS_SIN_BOLSA = listOf(0 to 70, 1 to 25, 2 to 5)
        private val NINOS_CON_BOLSA = listOf(0 to 55, 1 to 25, 2 to 15, 3 to 5)
        private val NINOS_SIN_BOLSA = listOf(0 to 80, 1 to 15, 2 to 5)
    }
}