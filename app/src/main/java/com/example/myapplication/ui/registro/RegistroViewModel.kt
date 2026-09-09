package com.example.myapplication.ui.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.RegistroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroViewModel(private val repo: RegistroRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegistroUiState(
            fechaEntrada = obtenerFechaHoy()
        )
    )
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    private fun obtenerFechaHoy(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    fun onPlacaChange(valor: String) {
        _uiState.update { it.copy(placa = valor, mensajeError = null) }
    }

    fun onModeloChange(valor: String) {
        _uiState.update { it.copy(modelo = valor, mensajeError = null) }
    }

    fun onColorChange(valor: String) {
        _uiState.update { it.copy(color = valor, mensajeError = null) }
    }

    fun onNumeroSerieChange(valor: String) {
        _uiState.update { it.copy(numeroSerie = valor, mensajeError = null) }
    }

    fun onNombreClienteChange(valor: String) {
        _uiState.update { it.copy(nombreCliente = valor, mensajeError = null) }
    }

    fun onTelefonoClienteChange(valor: String) {
        _uiState.update { it.copy(telefonoCliente = valor, mensajeError = null) }
    }

    fun onEmailClienteChange(valor: String) {
        _uiState.update { it.copy(emailCliente = valor, mensajeError = null) }
    }

    fun onFechaEntradaChange(valor: String) {
        _uiState.update { it.copy(fechaEntrada = valor, mensajeError = null) }
    }

    fun limpiarPlaca() = _uiState.update { it.copy(placa = "") }
    fun limpiarModelo() = _uiState.update { it.copy(modelo = "") }
    fun limpiarColor() = _uiState.update { it.copy(color = "") }
    fun limpiarNumeroSerie() = _uiState.update { it.copy(numeroSerie = "") }
    fun limpiarNombreCliente() = _uiState.update { it.copy(nombreCliente = "") }
    fun limpiarTelefonoCliente() = _uiState.update { it.copy(telefonoCliente = "") }
    fun limpiarEmailCliente() = _uiState.update { it.copy(emailCliente = "") }
    fun limpiarFechaEntrada() = _uiState.update { it.copy(fechaEntrada = "") }

    fun registrarIngreso(tecnicoId: Long = 1L) {
        val state = _uiState.value

        if (state.placa.isBlank() || state.nombreCliente.isBlank() || state.modelo.isBlank() || state.telefonoCliente.isBlank()) {
            _uiState.update {
                it.copy(mensajeError = "Placa, cliente, modelo y teléfono son obligatorios")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }
            try {
                val fechaMillis = try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.parse(state.fechaEntrada)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                val id = repo.registrarIngreso(
                    placa = state.placa.trim(),
                    nombreCliente = state.nombreCliente.trim(),
                    modelo = state.modelo.trim(),
                    telefonoCliente = state.telefonoCliente.trim(),
                    color = state.color.trim().ifEmpty { null },
                    emailCliente = state.emailCliente.trim().ifEmpty { null },
                    numeroSerie = state.numeroSerie.trim().ifEmpty { null },
                    fechaEntrada = fechaMillis,
                    tecnicoId = tecnicoId
                )

                _uiState.update {
                    it.copy(
                        cargando = false,
                        registroExitoso = true,
                        idRegistrado = id
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "Error al guardar: ${e.message}"
                    )
                }
            }
        }
    }

    fun resetMensajeError() {
        _uiState.update { it.copy(mensajeError = null) }
    }

    fun resetRegistroExitoso() {
        _uiState.update {
            it.copy(
                registroExitoso = false,
                idRegistrado = null,
                placa = "",
                modelo = "",
                color = "",
                numeroSerie = "",
                nombreCliente = "",
                telefonoCliente = "",
                emailCliente = "",
                fechaEntrada = obtenerFechaHoy()
            )
        }
    }
}
