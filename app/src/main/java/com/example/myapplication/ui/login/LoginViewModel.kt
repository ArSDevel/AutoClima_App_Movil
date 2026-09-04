package com.example.myapplication.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.util.PasswordHasher // Asegúrate de que el nombre coincida con tu encriptador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel que maneja la lógica de inicio de sesión conectada a Room Database.
 */
class LoginViewModel(private val tecnicoDao: TecnicoDao) : ViewModel() {

    // Estado interno mutable
    private val _uiState = MutableStateFlow(LoginUiState())
    // Estado expuesto como StateFlow de solo lectura para la vista
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el valor del campo ID (Usuario) en el estado y limpia los errores activos al escribir.
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
     * Valida los campos ingresados e inicia la sesión validando contra Room Database.
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

        // Si existen errores locales de texto vacío, actualiza el estado y detén el flujo
        if (hayError) {
            _uiState.update { estadoActual ->
                estadoActual.copy(
                    idError = errorId,
                    passwordError = errorPassword
                )
            }
            return
        }

        // Iniciamos la consulta en segundo plano mediante una Corrutina
        viewModelScope.launch {
            try {
                // 1. Buscar si el técnico existe por su nombre de usuario
                val tecnico = tecnicoDao.buscarPorUsuario(idActual)

                if (tecnico != null) {
                    // 2. Si existe, verificamos la contraseña ingresada contra el Hash guardado
                    // NOTA: Si tu función en util se llama de otra forma (ej. checkPassword), adáptala aquí
                    val esPasswordCorrecto = PasswordHasher.hash(passwordActual) == tecnico.passwordHash

                    if (esPasswordCorrecto) {
                        // Login exitoso
                        _uiState.update { estadoActual ->
                            estadoActual.copy(
                                idError = null,
                                passwordError = null,
                                isLoggedIn = true
                            )
                        }
                    } else {
                        // Contraseña incorrecta
                        _uiState.update { estadoActual ->
                            estadoActual.copy(passwordError = "Contraseña incorrecta")
                        }
                    }
                } else {
                    // El usuario no existe en la base de datos
                    _uiState.update { estadoActual ->
                        estadoActual.copy(idError = "El usuario no existe")
                    }
                }
            } catch (e: Exception) {
                // Manejo de errores de base de datos por si Room falla
                _uiState.update { estadoActual ->
                    estadoActual.copy(idError = "Error al conectar con la base de datos")
                }
            }
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
