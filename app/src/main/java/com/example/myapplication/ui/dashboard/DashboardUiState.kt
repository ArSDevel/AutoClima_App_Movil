package com.example.myapplication.ui.dashboard

import com.example.myapplication.data.local.model.IngresoResumen

data class DashboardUiState(
    val textoBusqueda: String = "",
    val ingresos: List<IngresoResumen> = emptyList(),
    val cargando: Boolean = true,
    val hayError: Boolean = false
)