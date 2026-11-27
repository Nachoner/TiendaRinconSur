package com.example.rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.UsuarioRepository
import com.example.rinconsur.repository.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val clave: String = "",
    val emailError: String? = null,
    val claveError: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val usuarioLogueado: Usuario? = null
)

/**
 * El repositorio entra por el constructor: la app usa el real y
 * las pruebas unitarias pueden pasar uno falso.
 */
class LoginViewModel(
    private val repo: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, error = null) }

    fun onClaveChange(value: String) =
        _uiState.update { it.copy(clave = value, claveError = null, error = null) }

    fun login() {
        val state = _uiState.value

        // Validación del formulario: si hay errores, no se llama al backend
        val emailError = Validaciones.validarEmail(state.email)
        val claveError = if (state.clave.isBlank()) "Ingresa la contraseña" else null
        if (emailError != null || claveError != null) {
            _uiState.update { it.copy(emailError = emailError, claveError = claveError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // POST /api/auth/login en ms-usuarios
            repo.login(state.email, state.clave)
                .onSuccess { usuario ->
                    _uiState.update { it.copy(isLoading = false, usuarioLogueado = usuario, clave = "") }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "No se pudo iniciar sesión")
                    }
                }
        }
    }

    fun logout() {
        _uiState.value = LoginUiState()
    }
}