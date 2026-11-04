package com.example.rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.repository.data.AppDatabase
import com.example.rinconsur.repository.data.Seguridad
import com.example.rinconsur.repository.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val email: String = "",
    val clave: String = "",
    val emailError: String? = null,
    val claveError: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val usuarioLogueado: Usuario? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, error = null) }

    fun onClaveChange(value: String) =
        _uiState.update { it.copy(clave = value, claveError = null, error = null) }

    fun login() {
        val state = _uiState.value

        // Validación del formulario: si hay errores, no se consulta la base de datos
        val emailError = Validaciones.validarEmail(state.email)
        val claveError = if (state.clave.isBlank()) "Ingresa la contraseña" else null
        if (emailError != null || claveError != null) {
            _uiState.update { it.copy(emailError = emailError, claveError = claveError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(600) // pequeña pausa para mostrar el indicador de carga

            val encontrado = withContext(Dispatchers.IO) {
                usuarioDao.buscarPorEmail(state.email.trim().lowercase())
            }?.usuario

            // Mismo mensaje si el email no existe, la clave es mala o la cuenta está desactivada
            val ok = encontrado != null && encontrado.activo &&
                    withContext(Dispatchers.Default) {
                        val intento = Seguridad.hash(state.clave, encontrado.sal)
                        Seguridad.iguales(intento, encontrado.claveHash)
                    }

            _uiState.update {
                if (ok) it.copy(isLoading = false, usuarioLogueado = encontrado, clave = "")
                else it.copy(isLoading = false, error = "Credenciales inválidas")
            }
        }
    }

    fun logout() {
        _uiState.value = LoginUiState()
    }
}

