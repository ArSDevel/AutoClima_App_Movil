package com.example.myapplication.ui.dashboard

//Periodos disponibles para filtrar por fecha de entrada.
// Los límites se calcularán según la zona horaria del dispositivo.
enum class FiltroFechaIngreso {
    // No limita la fecha de entrada.
    TODAS,
    // Desde el inicio de hoy hasta el inicio de mañana.
    HOY,
    // Hoy y los seis días anteriores.
    ULTIMOS_SIETE_DIAS,
    // Desde el primer día del mes hasta el inicio del siguiente.
    ESTE_MES
}

//Orden del listado.
enum class OrdenIngreso {
    RECIENTES,
    ANTIGUOS,
    PLACA_ASCENDENTE
}