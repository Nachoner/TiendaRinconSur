package com.example.rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.UsuarioRepository
import com.example.rinconsur.repository.model.TiposBase
import com.example.rinconsur.repository.model.Usuario
import com.example.rinconsur.repository.model.UsuarioConTipo
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

@OptIn(ExperimentalCoroutinesApi::class)
class UsuarioViewModel(
    private val repo: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    // Mensajes generales (errores de red, desactivar cuenta, etc.)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    // Cada vez que cambia este número, se vuelve a pedir la lista al backend
    private val recarga = MutableStateFlow(0)

    // READ: GET /api/usuarios. Se carga al abrir la pantalla y tras cada cambio.
    val usuarios: StateFlow<List<UsuarioConTipo>> = recarga
        .mapLatest {
            repo.listar().getOrElse { e ->
                _message.value = e.message
                emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recargar() = recarga.update { it + 1 }

    // Estado del formulario, con un error por campo
    private val _form = MutableStateFlow(UsuarioFormState())
    val form: StateFlow<UsuarioFormState> = _form.asStateFlow()

    // true mientras se guarda (evita guardar dos veces con doble toque)
    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    fun nuevo() { _form.value = UsuarioFormState() }

    // Carga un usuario para editarlo. La clave queda vacía: si no se escribe, se conserva la anterior.
    fun cargar(id: Long) {
        viewModelScope.launch {
            repo.buscar(id)
                .onSuccess { u ->
                    _form.value = UsuarioFormState(
                        id = u.id,
                        nombre = u.nombre,
                        email = u.email,
                        tipoUsuarioId = u.tipoUsuarioId
                    )
                }
                .onFailure { _message.value = it.message }
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
        if (_guardando.value) return

        viewModelScope.launch {
            _guardando.value = true
            val resultado = if (f.id == 0L) {
                repo.crear(nombre, email, f.clave, f.tipoUsuarioId)
            } else {
                repo.actualizar(f.id, nombre, email, f.clave.ifEmpty { null }, f.tipoUsuarioId)
            }
            _guardando.value = false

            resultado
                .onSuccess {
                    recargar()
                    onSuccess()
                }
                .onFailure { mostrarError(it) }
        }
    }

    // Se desactiva en vez de eliminar, porque los turnos apuntan al usuario
    fun cambiarActivo(usuario: Usuario, activo: Boolean, usuarioActualId: Long?) {
        if (!activo && usuario.id == usuarioActualId) {
            _message.value = "No puedes desactivar tu propia cuenta"
            return
        }
        viewModelScope.launch {
            repo.cambiarActivo(usuario.id, activo)
                .onSuccess { recargar() }
                .onFailure { _message.value = it.message }
        }
    }

    // Lleva cada error del backend al campo que corresponde
    private fun mostrarError(e: Throwable) {
        val api = e as? ApiException
        when {
            // 409: el email ya está registrado
            api?.codigo == 409 ->
                _form.update { it.copy(emailError = e.message) }

            // 400 con errores por campo (validaciones del backend)
            api != null && api.errores.isNotEmpty() ->
                _form.update {
                    it.copy(
                        nombreError = api.errores["nombre"],
                        emailError = api.errores["email"]
                    )
                }

            else -> _message.value = e.message ?: "No se pudo guardar"
        }
    }
}