package com.example.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.repository.DashboardRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observarIngresos()
    }

    fun cambiarBusqueda(nuevoTexto: String) {
        if (nuevoTexto == _uiState.value.textoBusqueda) { return }
        _uiState.update { estadoActual -> estadoActual.copy(textoBusqueda = nuevoTexto) }
        observarIngresos()
    }

    fun cambiarEstado(nuevoEstado: EstadoIngreso?) {
        if (nuevoEstado == _uiState.value.estadoSeleccionado) { return }
        _uiState.update { estadoActual -> estadoActual.copy(estadoSeleccionado = nuevoEstado) }
        observarIngresos()
    }

    fun reintentar() { observarIngresos() }

    private fun observarIngresos() {
        observacion?.cancel()

        // Capturamos los dos criterios para esta consulta.
        val textoBusqueda = _uiState.value.textoBusqueda
        val estadoSeleccionado = _uiState.value.estadoSeleccionado

        _uiState.update { estadoActual -> estadoActual.copy(cargando = true, hayError = false) }

        observacion = viewModelScope.launch {
            try {
                repository.observarIngresos(
                    textoBusqueda = textoBusqueda,
                    estadoFiltro = estadoSeleccionado
                ).collect { ingresos ->
                    _uiState.update { estadoActual ->
                        estadoActual.copy(ingresos = ingresos, cargando = false, hayError = false)
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { estadoActual ->
                    estadoActual.copy(ingresos = emptyList(), cargando = false, hayError = true)
                }
            }
        }
    }
}