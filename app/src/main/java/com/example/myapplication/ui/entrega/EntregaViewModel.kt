package com.example.myapplication.ui.entrega

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.data.local.model.ReglasIngreso
import com.example.myapplication.data.local.model.RequisitosEntrega
import com.example.myapplication.data.local.repository.DashboardRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la pantalla de entrega.
data class EntregaUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val ingreso: IngresoResumen? = null,
    val requisitos: RequisitosEntrega = RequisitosEntrega(checklist = false, diagnostico = false, firma = false),
    val nota: String = "",
    val guardando: Boolean = false,
    val error: String? = null,
    val entregado: Boolean = false) {
    val estadoActual: EstadoIngreso?
        get() = ingreso?.let { EstadoIngreso.desdeValor(it.estado) }

    // Solo se entrega un ingreso firmado que todavía no se entregó.
    val puedeEntregar: Boolean
        get() = estadoActual?.let { ReglasIngreso.entregar(it) } == true && requisitos.completos &&
                !guardando && !entregado }

// Carga lo que falta antes de entregar y confirma la entrega con una nota opcional.
class EntregaViewModel(
    private val ingresoId: Long,
    private val repository: DashboardRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EntregaUiState())
    val uiState: StateFlow<EntregaUiState> = _uiState.asStateFlow()

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                // Los requisitos se consultan una vez al abrir; el estado del ingreso sí se observa.
                val requisitos = repository.obtenerRequisitosEntrega(ingresoId)
                repository.observarIngresoPorId(ingresoId).collect { ingreso ->
                    _uiState.update { it.copy(cargando = false, ingreso = ingreso, requisitos = requisitos,
                        errorCarga = if (ingreso == null) "El ingreso ya no existe." else null) } }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(cargando = false, errorCarga = "No fue posible cargar el ingreso.") } } } }

    fun cambiarNota(texto: String) {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(nota = texto.take(MAXIMO_NOTA), error = null) } }

    fun confirmar(tecnicoId: Long) {
        val actual = _uiState.value
        if (!actual.puedeEntregar) return
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                // La nota queda como motivo del cambio de estado en el historial.
                repository.cambiarEstado(ingresoId, EstadoIngreso.ENTREGADO,
                    actual.nota.trim().ifEmpty { DashboardRepository.NOTA_ENTREGA_PREDETERMINADA }, tecnicoId)
                _uiState.update { it.copy(guardando = false, entregado = true) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update { it.copy(guardando = false,
                    error = error.message ?: "No fue posible confirmar la entrega.") } } } }

    fun consumirExito() { _uiState.update { it.copy(entregado = false) } }

    private companion object {
        const val MAXIMO_NOTA = 300 } }
