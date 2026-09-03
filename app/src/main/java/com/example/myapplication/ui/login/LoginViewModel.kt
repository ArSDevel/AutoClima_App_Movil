package com.example.myapplication.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel que maneja la lógica básica del formulario de inicio de sesión
 * y las validaciones simples en memoria.
 */
class LoginViewModel : ViewModel() {

    // Estado interno mutable
    private val _uiState = MutableStateFlow(LoginUiState())
    // Estado expuesto como StateFlow de solo lectura para la vista
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el valor del campo ID en el estado y limpia los errores activos al escribir.
     */
    fun onIdChanged(nuevoId: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                id = nuevoId,
                idError = if (nuevoId.isNotBlank()) null else estadoActual.idError
            )
        }
    }

    /**
     * Actualiza el valor del campo Contraseña en el estado y limpia los errores activos al escribir.
     */
    fun onPasswordChanged(nuevaPassword: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                password = nuevaPassword,
                passwordError = if (nuevaPassword.isNotBlank()) null else estadoActual.passwordError
            )
        }
    }

    /**
     * Valida los campos ingresados e inicia la sesión en memoria si los datos son válidos.
     */
    fun login() {
        val idActual = _uiState.value.id.trim()
        val passwordActual = _uiState.value.password.trim()

        var hayError = false
        var errorId: String? = null
        var errorPassword: String? = null

        // Validación: El ID no puede estar vacío
        if (idActual.isEmpty()) {
            errorId = "El ID no puede estar vacío"
            hayError = true
        }

        // Validación: La contraseña no puede estar vacía
        if (passwordActual.isEmpty()) {
            errorPassword = "La contraseña no puede estar vacía"
            hayError = true
        }

        // Si existen errores, actualiza el estado mostrando los avisos
        if (hayError) {
            _uiState.update { estadoActual ->
                estadoActual.copy(
                    idError = errorId,
                    passwordError = errorPassword
                )
            }
            return
        }

        // Si la validación es correcta, marca inicio de sesión exitoso
        _uiState.update { estadoActual ->
            estadoActual.copy(
                idError = null,
                passwordError = null,
                isLoggedIn = true
            )
        }
    }

    /**
     * Restablece el estado de la sesión al presionar "Cerrar Sesión".
     */
    fun resetLoginState() {
        _uiState.update { estadoActual ->
            estadoActual.copy(isLoggedIn = false)
        }
    }
}
