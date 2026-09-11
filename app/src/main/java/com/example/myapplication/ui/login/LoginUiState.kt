package com.example.myapplication.ui.login

// stado del formulario y de la sesión actual.
// id: nombre de usuario escrito en el formulario.
// tecnicoId: identificador de Room obtenido al autenticar al técnico.
// La sesión se conserva en memoria mientras viva el ViewModel.
data class LoginUiState(
    val id: String = "",
    val password: String = "",
    val idError: String? = null,
    val passwordError: String? = null,
    val isLoggedIn: Boolean = false,
    val cargando: Boolean = false,
    val tecnicoId: Long? = null,
    val nombreTecnico: String? = null
)