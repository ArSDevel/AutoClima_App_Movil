package com.example.myapplication.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.util.PasswordHasher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//ViewModel que maneja la lógica de inicio de sesión conectada a Room Database.
// Conserva el identificador y el nombre del técnico autenticado
// para utilizarlos en las operaciones de la aplicación.
class LoginViewModel(
    private val tecnicoDao: TecnicoDao
) : ViewModel() {

    // Estado interno mutable.
    private val _uiState = MutableStateFlow(LoginUiState())
    // Estado expuesto como StateFlow de solo lectura para la vista.
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    // Permite cancelar una autenticación pendiente al cerrar sesión.
    private var loginJob: Job? = null

    // Actualiza el valor del campo ID (Usuario) en el estado
    // y limpia los errores activos al escribir.
    fun onIdChanged(nuevoId: String) {
        _uiState.update { estadoActual ->
            if (estadoActual.cargando || estadoActual.isLoggedIn) {
                estadoActual
            } else {
                estadoActual.copy(id = nuevoId, idError = null)
            }
        }
    }

    // Actualiza el valor del campo Contraseña en el estado
    // y limpia los errores activos al escribir
    fun onPasswordChanged(nuevaPassword: String) {
        _uiState.update { estadoActual ->
            if (estadoActual.cargando || estadoActual.isLoggedIn) {
                estadoActual
            } else {
                estadoActual.copy(password = nuevaPassword, passwordError = null)
            }
        }
    }

    // Valida los campos ingresados e inicia la sesión validando contra Room Database.
    fun login() {
        val estadoActual = _uiState.value
        // Evita consultas duplicadas o iniciar una sesión que ya está abierta.
        if (estadoActual.cargando || estadoActual.isLoggedIn) return
        val idActual = estadoActual.id.trim()
        // Conserva la contraseña tal como fue escrita, incluidos sus espacios.
        val passwordActual = estadoActual.password
        // Validación: El ID no puede estar vacío.
        val errorId = if (idActual.isEmpty()) {
            "El usuario no puede estar vacío"
        } else { null }

        // Validación: La contraseña no puede estar vacía.
        val errorPassword = if (passwordActual.isEmpty()) {
            "La contraseña no puede estar vacía"
        } else { null }

        // Si existen errores locales de texto vacío,
        // actualiza el estado y detén el flujo.
        if (errorId != null || errorPassword != null) {
            _uiState.update { it.copy(idError = errorId, passwordError = errorPassword) }
            return
        }

        // Marca la operación antes de lanzar la corrutina
        // para evitar varios intentos simultáneos.
        _uiState.update {
            it.copy(cargando = true, idError = null,
                passwordError = null, tecnicoId = null,
                nombreTecnico = null, isLoggedIn = false)
        }

        // Iniciamos la consulta mediante una corrutina.
        // Room ejecuta la consulta suspendida fuera del hilo principal.
        loginJob = viewModelScope.launch {
            try {
                // 1. Buscar si el técnico existe por su nombre de usuario.
                val tecnico = tecnicoDao.buscarPorUsuario(idActual)
                // El usuario no existe en la base de datos.
                if (tecnico == null) {
                    _uiState.update {
                        it.copy(cargando = false, idError = "El usuario no existe")
                    }
                    return@launch
                }

                // 2. Si existe, verificamos la contraseña ingresada
                // contra el hash guardado.
                // El cálculo se realiza fuera del hilo principal.
                val esPasswordCorrecto = withContext(Dispatchers.Default) {
                    PasswordHasher.hash(passwordActual) == tecnico.passwordHash
                }

                // Contraseña incorrecta.
                if (!esPasswordCorrecto) {
                    _uiState.update { it.copy(cargando = false, passwordError = "Contraseña incorrecta") }
                    return@launch
                }

                // 3. Comprueba que el técnico tenga un identificador válido.
                check(tecnico.tecnicoId > 0) { "El técnico no tiene un identificador válido." }

                // Login exitoso.
                // Guarda los datos del técnico y limpia la contraseña
                // en la misma actualización que abre la sesión.
                _uiState.update {
                    it.copy(id = tecnico.usuario, password = "",
                        idError = null, passwordError = null,
                        cargando = false, tecnicoId = tecnico.tecnicoId,
                        nombreTecnico = tecnico.nombre, isLoggedIn = true
                    )
                }
            } catch (cancelacion: CancellationException) {
                // Respeta la cancelación de la corrutina.
                throw cancelacion
            } catch (_: Exception) {
                // Manejo de errores de base de datos o autenticación.
                _uiState.update {
                    it.copy(
                        cargando = false,
                        isLoggedIn = false,
                        tecnicoId = null,
                        nombreTecnico = null,
                        idError = "No fue posible iniciar sesión. Intenta nuevamente."
                    )
                }
            }
        }
    }

    // Restablece el estado de la sesión al presionar "Cerrar Sesión".
    // También cancela una autenticación pendiente y limpia
    // las credenciales, los datos del técnico y los errores.
    fun resetLoginState() {
        loginJob?.cancel()
        loginJob = null
        _uiState.value = LoginUiState()
    }
}