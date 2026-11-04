package com.example.rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.data.AppDatabase
import com.example.rinconsur.repository.model.Registro
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

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).registroDao()

    // Turno activo del operador (lo informa la pantalla)
    private val turnoId = MutableStateFlow<Long?>(null)

    // READ: registros del turno activo
    @OptIn(ExperimentalCoroutinesApi::class)
    val registros: StateFlow<List<Registro>> = turnoId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else dao.registrosDelTurno(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(RegistroFormState())
    val form: StateFlow<RegistroFormState> = _form.asStateFlow()

    // Mensajes generales (por ejemplo, un permiso denegado)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    fun setTurno(id: Long?) { turnoId.value = id }

    fun nuevo() { _form.value = RegistroFormState() }

    // Carga un registro para editarlo, solo si pertenece al turno activo
    fun cargar(id: Long) {
        viewModelScope.launch {
            val r = withContext(Dispatchers.IO) { dao.buscarPorId(id) }
            if (r == null || r.turnoId != turnoId.value) {
                _message.value = "Solo puedes editar registros de tu turno activo"
                return@launch
            }
            _form.value = RegistroFormState(
                id = r.id,
                rangoEtario = r.rangoEtario,
                adultos = r.acompanantesAdultos.toString(),
                ninos = r.acompanantesNinos.toString(),
                compro = r.compro
            )
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

    /** CREATE (id = 0) o UPDATE (id > 0). El turno entrega el acceso. */
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

        val adultos = f.adultos.trim().toInt()
        val ninos = f.ninos.trim().toInt()

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (f.id == 0L) {
                    dao.insertar(
                        Registro(
                            turnoId = turno.id,
                            accesoId = turno.accesoId,
                            fechaHora = System.currentTimeMillis(),
                            rangoEtario = f.rangoEtario!!,
                            acompanantesAdultos = adultos,
                            acompanantesNinos = ninos,
                            compro = f.compro!!
                        )
                    )
                } else {
                    val actual = dao.buscarPorId(f.id)
                    // Se conservan el turno, el acceso y la fecha del registro original
                    if (actual != null && actual.turnoId == turno.id) {
                        dao.actualizar(
                            actual.copy(
                                rangoEtario = f.rangoEtario!!,
                                acompanantesAdultos = adultos,
                                acompanantesNinos = ninos,
                                compro = f.compro!!
                            )
                        )
                    }
                }
            }
            _form.value = RegistroFormState()
            onSuccess()
        }
    }

    /** DELETE: solo registros del turno activo. */
    fun eliminar(registro: Registro) {
        if (registro.turnoId != turnoId.value) {
            _message.value = "Solo puedes eliminar registros de tu turno activo"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            dao.eliminar(registro)
        }
    }
}