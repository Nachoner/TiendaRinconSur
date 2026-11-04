package com.example.rinconsur.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.data.AppDatabase
import com.example.rinconsur.repository.data.Seguridad
import com.example.rinconsur.repository.model.TiposBase
import com.example.rinconsur.repository.model.Usuario
import com.example.rinconsur.repository.model.UsuarioConTipo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UsuarioFormState(
    val id: Long = 0,
    val nombre: String = "",
    val email: String = "",
    val clave: String = "",
    val tipoUsuarioId: Long = TiposBase.OPERADOR_ID,
    val nombreError: String? = null,
    val emailError: String? = null,
    val claveError: String? = null
)

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()

    // READ
    val usuarios: StateFlow<List<UsuarioConTipo>> = usuarioDao.listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado del formulario, con un error por campo
    private val _form = MutableStateFlow(UsuarioFormState())
    val form: StateFlow<UsuarioFormState> = _form.asStateFlow()

    // Mensajes generales (por ejemplo, al desactivar una cuenta)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    fun nuevo() { _form.value = UsuarioFormState() }

    // Carga un usuario para editarlo. La clave queda vacía: si no se escribe, se conserva la anterior.
    fun cargar(id: Long) {
        viewModelScope.launch {
            val u = withContext(Dispatchers.IO) { usuarioDao.buscarPorId(id) }
            if (u != null) {
                _form.value = UsuarioFormState(
                    id = u.id,
                    nombre = u.nombre,
                    email = u.email,
                    tipoUsuarioId = u.tipoUsuarioId
                )
            }
        }
    }

    fun onNombreChange(value: String) =
        _form.update { it.copy(nombre = value, nombreError = null) }

    fun onEmailChange(value: String) =
        _form.update { it.copy(email = value, emailError = null) }

    fun onClaveChange(value: String) =
        _form.update { it.copy(clave = value, claveError = null) }

    fun onTipoChange(tipoId: Long) =
        _form.update { it.copy(tipoUsuarioId = tipoId) }

    /** CREATE (id = 0) o UPDATE (id > 0). */
    fun guardar(onSuccess: () -> Unit) {
        val f = _form.value
        val nombre = f.nombre.trim()
        val email = f.email.trim().lowercase()

        val nombreError = Validaciones.validarNombre(nombre)
        val emailError = Validaciones.validarEmail(email)
        // Al editar, la clave es opcional; si se escribe, debe cumplir las reglas
        val claveError = if (f.id == 0L || f.clave.isNotEmpty()) Validaciones.validarClave(f.clave) else null

        if (nombreError != null || emailError != null || claveError != null) {
            _form.update {
                it.copy(nombreError = nombreError, emailError = emailError, claveError = claveError)
            }
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (f.id == 0L) {
                        val sal = Seguridad.nuevaSal()
                        usuarioDao.insertar(
                            Usuario(
                                nombre = nombre,
                                email = email,
                                claveHash = Seguridad.hash(f.clave, sal),
                                sal = sal,
                                tipoUsuarioId = f.tipoUsuarioId
                            )
                        )
                    } else {
                        val actual = usuarioDao.buscarPorId(f.id)
                        if (actual != null) {
                            val base = actual.copy(
                                nombre = nombre,
                                email = email,
                                tipoUsuarioId = f.tipoUsuarioId
                            )
                            val actualizado = if (f.clave.isEmpty()) {
                                base
                            } else {
                                val sal = Seguridad.nuevaSal()
                                base.copy(claveHash = Seguridad.hash(f.clave, sal), sal = sal)
                            }
                            usuarioDao.actualizar(actualizado)
                        }
                    }
                }
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _form.update { it.copy(emailError = "Ya existe un usuario con ese email") }
            }
        }
    }

    // Se desactiva en vez de eliminar, porque los turnos apuntan al usuario
    fun cambiarActivo(usuario: Usuario, activo: Boolean, usuarioActualId: Long?) {
        if (!activo && usuario.id == usuarioActualId) {
            _message.value = "No puedes desactivar tu propia cuenta"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            usuarioDao.cambiarActivo(usuario.id, activo)
        }
    }
}