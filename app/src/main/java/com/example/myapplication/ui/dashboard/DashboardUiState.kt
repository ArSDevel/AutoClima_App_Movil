package com.example.myapplication.ui.dashboard

import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen

// Estado del listado principal del dashboard.
data class DashboardUiState(
    // Búsqueda por placa, serie, modelo o cliente.
    val textoBusqueda: String = "",
    // null significa mostrar todos los estados.
    val estadoSeleccionado: EstadoIngreso? = null,
    // Periodo aplicado a la fecha de entrada.
    val fechaSeleccionada: FiltroFechaIngreso =
        FiltroFechaIngreso.TODAS,
    // Orden del listado.
    val ordenSeleccionado: OrdenIngreso =
        OrdenIngreso.RECIENTES,
    // Resultados del listado con los filtros actuales.
    val ingresos: List<IngresoResumen> = emptyList(),
    // Estado de la consulta.
    val cargando: Boolean = true,
    val hayError: Boolean = false
) {
    // Cambiar el orden no se considera un filtro.
    val hayFiltrosActivos: Boolean
        get() = textoBusqueda.isNotBlank() || estadoSeleccionado != null ||
                fechaSeleccionada != FiltroFechaIngreso.TODAS

    // Cantidad del listado filtrado.
    // Se muestra cuando la consulta termina sin errores.
    val cantidadResultados: Int
        get() = ingresos.size
}