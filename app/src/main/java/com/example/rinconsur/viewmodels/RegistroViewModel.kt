package com.example.rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.RegistroRepository
import com.example.rinconsur.repository.model.Registro
import com.example.rinconsur.repository.model.Turno
import com.example.rinconsur.repository.remote.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroFormState(
    val id: Long = 0,
    val rangoEtario: String? = null,
    val adultos: String = "0",
    val ninos: String = "0",
    val compro: Boolean? = null,
    val rangoError: String? = null,
    val adultosError: String? = null,
    val ninosError: String? = null,
    val comproError: String? = null
)

class RegistroViewModel(
    private val repo: RegistroRepository = RegistroRepository()
) : ViewModel() {

    // Turno activo del operador (lo informa AppNavigation)
    private var turnoId: Long? = null

    // READ: registros del turno activo (vienen de ms-registros)
    private val _registros = MutableStateFlow<List<Registro>>(emptyList())
    val registros: StateFlow<List<Registro>> = _registros.asStateFlow()

    private val _form = MutableStateFlow(RegistroFormState())
    val form: StateFlow<RegistroFormState> = _form.asStateFlow()

    // true mientras se guarda (evita guardar dos veces con doble toque)
    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    // Mensajes generales (errores de red, permisos, etc.)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    fun setTurno(id: Long?) {
        turnoId = id
        _registros.value = emptyList()
        if (id != null) recargar()
    }

    // GET /api/registros?turnoId=...
    fun recargar() {
        val id = turnoId ?: return
        viewModelScope.launch {
            repo.delTurno(id)
                .onSuccess { _registros.value = it }
                .onFailure { _message.value = it.message }
        }
    }

    fun nuevo() { _form.value = RegistroFormState() }

    // Carga un registro para editarlo, solo si pertenece al turno activo
    fun cargar(id: Long) {
        viewModelScope.launch {
            repo.buscar(id)
                .onSuccess { r ->
                    if (r.turnoId != turnoId) {
                        _message.value = "Solo puedes editar registros de tu turno activo"
                        return@onSuccess
                    }
                    _form.value = RegistroFormState(
                        id = r.id,
                        rangoEtario = r.rangoEtario,
                        adultos = r.acompanantesAdultos.toString(),
                        ninos = r.acompanantesNinos.toString(),
                        compro = r.compro
                    )
                }
                .onFailure { _message.value = it.message }
        }
    }

    fun onRangoChange(rango: String) =
        _form.update { it.copy(rangoEtario = rango, rangoError = null) }

    fun onAdultosChange(value: String) =
        _form.update { it.copy(adultos = Validaciones.soloDigitos(value), adultosError = null) }

    fun onNinosChange(value: String) =
        _form.update { it.copy(ninos = Validaciones.soloDigitos(value), ninosError = null) }

    fun onComproChange(value: Boolean) =
        _form.update { it.copy(compro = value, comproError = null) }

    /** CREATE (id = 0) o UPDATE (id > 0). El backend toma la entrada desde el turno. */
    fun guardar(turno: Turno, onSuccess: () -> Unit) {
        val f = _form.value

        val rangoError = Validaciones.validarRango(f.rangoEtario)
        val adultosError = Validaciones.validarCantidad(f.adultos)
        val ninosError = Validaciones.validarCantidad(f.ninos)
        val comproError = Validaciones.validarCompro(f.compro)

        if (rangoError != null || adultosError != null || ninosError != null || comproError != null) {
            _form.update {
                it.copy(
                    rangoError = rangoError,
                    adultosError = adultosError,
                    ninosError = ninosError,
                    comproError = comproError
                )
            }
            return
        }
        if (_guardando.value) return

        val adultos = f.adultos.trim().toInt()
        val ninos = f.ninos.trim().toInt()

        viewModelScope.launch {
            _guardando.value = true
            val resultado = if (f.id == 0L) {
                repo.crear(turno.id, f.rangoEtario!!, adultos, ninos, f.compro!!)
            } else {
                repo.actualizar(f.id, turno.id, f.rangoEtario!!, adultos, ninos, f.compro!!)
            }
            _guardando.value = false

            resultado
                .onSuccess {
                    _form.value = RegistroFormState()
                    recargar()
                    onSuccess()
                }
                .onFailure { mostrarError(it) }
        }
    }

    /** DELETE: solo registros del turno activo. */
    fun eliminar(registro: Registro) {
        if (registro.turnoId != turnoId) {
            _message.value = "Solo puedes eliminar registros de tu turno activo"
            return
        }
        viewModelScope.launch {
            repo.eliminar(registro.id)
                .onSuccess { _registros.update { lista -> lista.filterNot { it.id == registro.id } } }
                .onFailure { _message.value = it.message }
        }
    }

    // Si el backend devolvió errores por campo, se muestran bajo cada campo
    private fun mostrarError(e: Throwable) {
        val errores = (e as? ApiException)?.errores.orEmpty()
        if (errores.isEmpty()) {
            _message.value = e.message ?: "No se pudo guardar"
            return
        }
        _form.update {
            it.copy(
                rangoError = errores["rangoEtario"],
                adultosError = errores["acompanantesAdultos"],
                ninosError = errores["acompanantesNinos"],
                comproError = errores["compro"]
            )
        }
    }
}