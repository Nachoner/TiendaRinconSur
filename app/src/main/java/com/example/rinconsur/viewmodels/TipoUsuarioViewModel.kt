package com.example.rinconsur.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.data.AppDatabase
import com.example.rinconsur.repository.model.TipoUsuario
import com.example.rinconsur.repository.model.TiposBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TipoUsuarioFormState(
    val id: Long = 0,
    val nombre: String = "",
    val nombreError: String? = null
)

class TipoUsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).tipoUsuarioDao()

    // READ
    val tipos: StateFlow<List<TipoUsuario>> = dao.listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado del formulario, con el error del campo
    private val _form = MutableStateFlow(TipoUsuarioFormState())
    val form: StateFlow<TipoUsuarioFormState> = _form.asStateFlow()

    // Mensajes generales (por ejemplo, al eliminar)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

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
            try {
                withContext(Dispatchers.IO) {
                    val tipo = TipoUsuario(id = f.id, nombre = valor)
                    if (f.id == 0L) {
                        dao.insertar(tipo)
                    } else {
                        dao.actualizar(tipo)
                    }
                }
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _form.update { it.copy(nombreError = "El tipo \"$valor\" ya existe") }
            }
        }
    }

    /** DELETE. Falla si algún usuario usa el tipo (FK RESTRICT). */
    fun delete(tipo: TipoUsuario) {
        if (TiposBase.esBase(tipo.id)) {
            _message.value = "Los tipos base (Operador y Supervisor) no se pueden eliminar"
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { dao.eliminar(tipo) }
            } catch (e: SQLiteConstraintException) {
                _message.value = "No se puede eliminar \"${tipo.nombre}\": hay usuarios con ese tipo"
            }
        }
    }
}