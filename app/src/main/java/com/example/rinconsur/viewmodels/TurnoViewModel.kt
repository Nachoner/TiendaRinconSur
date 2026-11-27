package com.example.rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.EntradaRepository
import com.example.rinconsur.repository.TurnoRepository
import com.example.rinconsur.repository.model.Entrada
import com.example.rinconsur.repository.model.Turno
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TurnoFormState(
    val accesoId: Int? = null,
    val fotoRuta: String? = null,
    val accesoError: String? = null,
    val fotoError: String? = null
)

class TurnoViewModel(
    private val turnoRepo: TurnoRepository = TurnoRepository(),
    private val entradaRepo: EntradaRepository = EntradaRepository()
) : ViewModel() {

    // Usuario que tiene la sesión iniciada (lo informa AppNavigation tras el login)
    private var usuarioId: Long? = null

    // Turno en curso del operador, o null si no tiene (viene de ms-turnos)
    private val _turnoActivo = MutableStateFlow<Turno?>(null)
    val turnoActivo: StateFlow<Turno?> = _turnoActivo.asStateFlow()

    // Entradas disponibles para iniciar turno (vienen de ms-entradas)
    private val _entradas = MutableStateFlow<List<Entrada>>(emptyList())
    val entradas: StateFlow<List<Entrada>> = _entradas.asStateFlow()

    // true mientras se inicia o finaliza un turno
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _form = MutableStateFlow(TurnoFormState())
    val form: StateFlow<TurnoFormState> = _form.asStateFlow()

    // Mensajes de error del backend o de la red
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    fun setUsuario(id: Long?) {
        usuarioId = id
        _turnoActivo.value = null
        if (id != null) {
            cargarTurnoActivo()
            cargarEntradas()
        }
    }

    // READ: GET /api/turnos/activo?usuarioId=...
    fun cargarTurnoActivo() {
        val id = usuarioId ?: return
        viewModelScope.launch {
            turnoRepo.turnoActivo(id)
                .onSuccess { _turnoActivo.value = it }
                .onFailure { _message.value = it.message }
        }
    }

    // READ: GET /api/entradas?soloActivas=true
    fun cargarEntradas() {
        viewModelScope.launch {
            entradaRepo.listar(soloActivas = true)
                .onSuccess { _entradas.value = it }
                .onFailure { _message.value = it.message }
        }
    }

    fun nuevo() { _form.value = TurnoFormState() }

    fun onAccesoChange(acceso: Int) =
        _form.update { it.copy(accesoId = acceso, accesoError = null) }

    fun onFotoChange(ruta: String?) =
        _form.update { it.copy(fotoRuta = ruta, fotoError = null) }

    /** CREATE: POST /api/turnos. Exige entrada y foto. */
    fun iniciar(onSuccess: () -> Unit) {
        val f = _form.value
        val validos = _entradas.value.map { it.id.toInt() }
        val accesoError = Validaciones.validarAcceso(f.accesoId, validos)
        val fotoError = Validaciones.validarFoto(f.fotoRuta)
        if (accesoError != null || fotoError != null) {
            _form.update { it.copy(accesoError = accesoError, fotoError = fotoError) }
            return
        }

        val idUsuario = usuarioId ?: return
        if (_turnoActivo.value != null || _cargando.value) return

        viewModelScope.launch {
            _cargando.value = true
            turnoRepo.iniciar(idUsuario, f.accesoId!!.toLong(), f.fotoRuta!!)
                .onSuccess { turno ->
                    _turnoActivo.value = turno
                    _form.value = TurnoFormState()
                    onSuccess()
                }
                .onFailure { _message.value = it.message }
            _cargando.value = false
        }
    }

    /** UPDATE: PUT /api/turnos/{id}/finalizar */
    fun finalizar(turno: Turno, onSuccess: () -> Unit = {}) {
        if (_cargando.value) return
        viewModelScope.launch {
            _cargando.value = true
            turnoRepo.finalizar(turno.id)
                .onSuccess {
                    _turnoActivo.value = null
                    onSuccess()
                }
                .onFailure { _message.value = it.message }
            _cargando.value = false
        }
    }
}