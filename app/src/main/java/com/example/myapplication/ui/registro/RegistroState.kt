package com.example.myapplication.ui.registro

sealed class RegistroState {
    object Idle : RegistroState()
    object Loading : RegistroState()
    data class Success(val ingresoId: Long) : RegistroState()
    data class Error(val mensaje: String) : RegistroState()
}