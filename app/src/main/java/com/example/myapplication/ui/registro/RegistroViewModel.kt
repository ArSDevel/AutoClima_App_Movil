package com.example.myapplication.ui.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.RegistroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistroViewModel(private val repo: RegistroRepository) : ViewModel() {

    private val _registroState = MutableStateFlow<RegistroState>(RegistroState.Idle)
    val registroState: StateFlow<RegistroState> = _registroState

    fun registrarIngreso(
        placa: String,
        nombreCliente: String,
        modelo: String,
        telefonoCliente: String,
        color: String?,
        emailCliente: String?,
        numeroSerie: String?,
        fechaEntrada: Long,
        tecnicoId: Long
    ) {
        if (placa.isBlank() || nombreCliente.isBlank() || modelo.isBlank() || telefonoCliente.isBlank()) {
            _registroState.value = RegistroState.Error("Placa, cliente, modelo y teléfono son obligatorios")
            return
        }

        viewModelScope.launch {
            _registroState.value = RegistroState.Loading
            try {
                val id = repo.registrarIngreso(
                    placa, nombreCliente, modelo, telefonoCliente,
                    color, emailCliente, numeroSerie, fechaEntrada, tecnicoId
                )
                _registroState.value = RegistroState.Success(id)
            } catch (e: Exception) {
                _registroState.value = RegistroState.Error("Error al guardar: ${e.message}")
            }
        }
    }
}