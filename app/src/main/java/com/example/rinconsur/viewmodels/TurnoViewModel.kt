package com.example.rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.data.AppDatabase
import com.example.rinconsur.repository.model.Turno
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TurnoFormState(
    val accesoId: Int? = null,
    val fotoRuta: String? = null,
    val accesoError: String? = null,
    val fotoError: String? = null
)

class TurnoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).turnoDao()

    // Usuario que tiene la sesión iniciada (lo informa la pantalla tras el login)
    private val usuarioId = MutableStateFlow<Long?>(null)

    // Turno activo del usuario (fin = null), o null si no tiene
    @OptIn(ExperimentalCoroutinesApi::class)
    val turnoActivo: StateFlow<Turno?> = usuarioId
        .flatMapLatest { id -> if (id == null) flowOf(null) else dao.turnoActivo(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Historial de todos los turnos (lo ve el supervisor)
    val historial: StateFlow<List<Turno>> = dao.historial()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(TurnoFormState())
    val form: StateFlow<TurnoFormState> = _form.asStateFlow()

    fun setUsuario(id: Long?) { usuarioId.value = id }

    fun nuevo() { _form.value = TurnoFormState() }

    fun onAccesoChange(acceso: Int) =
        _form.update { it.copy(accesoId = acceso, accesoError = null) }

    fun onFotoChange(ruta: String?) =
        _form.update { it.copy(fotoRuta = ruta, fotoError = null) }

    /** CREATE: inicia el turno. Exige acceso y foto. */
    fun iniciar(onSuccess: () -> Unit) {
        val f = _form.value
        val accesoError = Validaciones.validarAcceso(f.accesoId)
        val fotoError = Validaciones.validarFoto(f.fotoRuta)
        if (accesoError != null || fotoError != null) {
            _form.update { it.copy(accesoError = accesoError, fotoError = fotoError) }
            return
        }

        val idUsuario = usuarioId.value ?: return
        if (turnoActivo.value != null) return // ya tiene un turno abierto

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.insertar(
                    Turno(
                        usuarioId = idUsuario,
                        accesoId = f.accesoId!!,
                        inicio = System.currentTimeMillis(),
                        fotoRuta = f.fotoRuta!!
                    )
                )
            }
            _form.value = TurnoFormState()
            onSuccess()
        }
    }

    /** UPDATE: cierra el turno guardando la hora de fin. */
    fun finalizar(turno: Turno, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.finalizar(turno.id, System.currentTimeMillis())
            }
            onSuccess()
        }
    }
}