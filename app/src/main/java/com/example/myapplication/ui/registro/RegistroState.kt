package com.example.myapplication.ui.registro

data class RegistroUiState(
    val placa: String = "",
    val modelo: String = "",
    val color: String = "",
    val numeroSerie: String = "",
    val nombreCliente: String = "",
    val telefonoCliente: String = "",
    val emailCliente: String = "",
    val fechaEntrada: String = "",
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val registroExitoso: Boolean = false,
    val idRegistrado: Long? = null
)

sealed class RegistroState {
    data object Idle : RegistroState()
    data object Loading : RegistroState()
    data class Success(val ingresoId: Long) : RegistroState()
    data class Error(val mensaje: String) : RegistroState()
}
