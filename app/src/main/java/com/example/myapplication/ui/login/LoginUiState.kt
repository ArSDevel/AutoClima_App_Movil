package com.example.myapplication.ui.login

/**
 * Estado inmutable de la pantalla de inicio de sesión.
 *
 * @property id Valor ingresado en el campo de ID de usuario.
 * @property password Valor ingresado en el campo de contraseña.
 * @property idError Mensaje de error para el campo de ID (null si no hay error).
 * @property passwordError Mensaje de error para el campo de contraseña (null si no hay error).
 * @property isLoggedIn Verdadero si el usuario ha iniciado sesión con éxito.
 */
data class LoginUiState(
    val id: String = "",
    val password: String = "",
    val idError: String? = null,
    val passwordError: String? = null,
    val isLoggedIn: Boolean = false
)
