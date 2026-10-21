package com.example.myapplication.ui.firma

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.local.repository.DiagnosticoRepository
import com.example.myapplication.data.local.repository.FirmaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Carga el ingreso, la cotización y la firma, y guarda la firma que dibuja el cliente.
class FirmaViewModel(
    private val ingresoId: Long,
    private val firmaRepository: FirmaRepository,
    private val dashboardRepository: DashboardRepository,
    private val diagnosticoRepository: DiagnosticoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FirmaUiState())
    val uiState: StateFlow<FirmaUiState> = _uiState.asStateFlow()

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                val conceptos = diagnosticoRepository.obtenerPorIngreso(ingresoId)?.conceptos.orEmpty()
                val total = if (conceptos.isEmpty()) null else conceptos.sumOf { it.importeCentavos }
                combine(dashboardRepository.observarIngresoPorId(ingresoId),
                    firmaRepository.observarFirma(ingresoId)) { ingreso, firma -> ingreso to firma
                }.collect { (ingreso, firma) ->
                    _uiState.update { it.copy(cargando = false, ingreso = ingreso, firmaGuardada = firma,
                        totalCentavos = total, conceptos = conceptos, errorCarga = if (ingreso == null) "El ingreso ya no existe." else null) } }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(cargando = false, errorCarga = "No fue posible cargar el ingreso.") } } } }

    // Empieza un trazo nuevo en el punto tocado.
    fun iniciarTrazo(punto: Offset) {
        if (!_uiState.value.puedeFirmar || _uiState.value.guardando) return
        _uiState.update { it.copy(trazos = it.trazos + listOf(listOf(dentro(punto))), error = null) } }

    // Agrega un punto al último trazo.
    fun agregarPunto(punto: Offset) {
        _uiState.update { estado ->
            if (estado.trazos.isEmpty()) return@update estado
            val ultimo = estado.trazos.last() + dentro(punto)
            estado.copy(trazos = estado.trazos.dropLast(1) + listOf(ultimo)) } }

    // Mantiene el punto dentro del recuadro aunque el dedo salga de él.
    private fun dentro(punto: Offset) = Offset(punto.x.coerceIn(0f, 1f), punto.y.coerceIn(0f, 1f))

    fun borrar() {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(trazos = emptyList(), error = null) } }

    fun limpiarError() { _uiState.update { it.copy(error = null) } }

    // La imagen se genera en la pantalla porque depende del tamaño del recuadro.
    fun confirmar(imagenPng: ByteArray, tecnicoId: Long) {
        val actual = _uiState.value
        if (!actual.puedeConfirmar) return
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                firmaRepository.confirmar(ingresoId, imagenPng, tecnicoId)
                _uiState.update { it.copy(guardando = false, firmaConfirmada = true, trazos = emptyList()) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update { it.copy(guardando = false,
                    error = error.message ?: "No fue posible guardar la firma.") } } } }

    // Cierra el aviso de éxito.
    fun consumirExito() { _uiState.update { it.copy(firmaConfirmada = false) } } }
