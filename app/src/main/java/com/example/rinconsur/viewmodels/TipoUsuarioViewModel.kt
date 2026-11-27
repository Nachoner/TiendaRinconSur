package com.example.rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.TipoUsuarioRepository
import com.example.rinconsur.repository.model.TipoUsuario
import com.example.rinconsur.repository.model.TiposBase
import com.example.rinconsur.repository.remote.ApiException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TipoUsuarioFormState(
    val id: Long = 0,
    val nombre: String = "",
    val nombreError: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class TipoUsuarioViewModel(
    private val repo: TipoUsuarioRepository = TipoUsuarioRepository()
) : ViewModel() {

    // Mensajes generales (por ejemplo, al eliminar)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    // Cada vez que cambia este número, se vuelve a pedir la lista al backend
    private val recarga = MutableStateFlow(0)

    // READ: GET /api/tipos-usuario
    val tipos: StateFlow<List<TipoUsuario>> = recarga
        .mapLatest {
            repo.listar().getOrElse { e ->
                _message.value = e.message
                emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recargar() = recarga.update { it + 1 }

    // Estado del formulario, con el error del campo
    private val _form = MutableStateFlow(TipoUsuarioFormState())
    val form: StateFlow<TipoUsuarioFormState> = _form.asStateFlow()

    fun nuevo() { _form.value = TipoUsuarioFormState() }

    fun editar(tipo: TipoUsuario) {
        _form.value = TipoUsuarioFormState(id = tipo.id, nombre = tipo.nombre)
    }

    fun onNombreChange(value: String) =
        _form.update { it.copy(nombre = value, nombreError = null) }

    /** CREATE (id = 0) o UPDATE (id > 0). */
    fun guardar(onSuccess: () -> Unit) {
        val f = _form.value
        val valor = f.nombre.trim()

        val error = Validaciones.validarTipoUsuario(valor)
            ?: if (f.id != 0L && TiposBase.esBase(f.id)) {
                "Los tipos base (Operador y Supervisor) no se pueden modificar"
            } else null
        if (error != null) {
            _form.update { it.copy(nombreError = error) }
            return
        }

        viewModelScope.launch {
            val resultado = if (f.id == 0L) repo.crear(valor) else repo.actualizar(f.id, valor)
            resultado
                .onSuccess {
                    recargar()
                    onSuccess()
                }
                .onFailure { e ->
                    val api = e as? ApiException
                    if (api?.codigo != null) {
                        // 409 (ya existe) o 400 (validación): se muestra bajo el campo
                        _form.update { it.copy(nombreError = api.errores["nombre"] ?: e.message) }
                    } else {
                        _message.value = e.message
                    }
                }
        }
    }

    /** DELETE. El backend lo rechaza si algún usuario usa el tipo. */
    fun delete(tipo: TipoUsuario) {
        if (TiposBase.esBase(tipo.id)) {
            _message.value = "Los tipos base (Operador y Supervisor) no se pueden eliminar"
            return
        }
        viewModelScope.launch {
            repo.eliminar(tipo.id)
                .onSuccess { recargar() }
                .onFailure { _message.value = it.message }
        }
    }
}