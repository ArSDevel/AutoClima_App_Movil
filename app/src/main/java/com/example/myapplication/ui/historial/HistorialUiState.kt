package com.example.myapplication.ui.historial

import com.example.myapplication.data.local.model.EventoHistorial

// Periodos disponibles para filtrar el historial.
enum class PeriodoHistorial { TODOS, HOY, SIETE_DIAS, TREINTA_DIAS }

// Estado de la pantalla de historial: filtros, eventos y carga.
data class HistorialUiState(
    val busqueda: String = "",
    val accion: String? = null,
    val periodo: PeriodoHistorial = PeriodoHistorial.TODOS,
    val eventos: List<EventoHistorial> = emptyList(),
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val hayMas: Boolean = false
) {
    val tieneFiltros: Boolean
        get() = busqueda.isNotBlank() || accion != null || periodo != PeriodoHistorial.TODOS }
