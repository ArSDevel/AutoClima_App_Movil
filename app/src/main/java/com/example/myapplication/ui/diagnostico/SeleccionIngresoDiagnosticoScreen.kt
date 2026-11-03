package com.example.myapplication.ui.diagnostico

import androidx.compose.runtime.Composable
import com.example.myapplication.R
import com.example.myapplication.ui.components.SeleccionIngresoScreen
import com.example.myapplication.ui.dashboard.DashboardViewModel

// Permite elegir el expediente cuyo diagnóstico se desea abrir.
// La consulta utiliza una instancia propia del ViewModel.
@Composable
fun SeleccionIngresoDiagnosticoScreen(
    viewModel: DashboardViewModel,
    onVolver: () -> Unit,
    onSeleccionarIngreso: (Long) -> Unit) {
    SeleccionIngresoScreen(
        viewModel = viewModel,
        titulo = R.string.diagnostico_selector_title,
        textoError = R.string.diagnostico_selector_error,
        textoReintentar = R.string.diagnostico_retry,
        textoVacio = R.string.diagnostico_selector_empty,
        textoSinResultados = R.string.diagnostico_selector_no_results,
        onVolver = onVolver,
        onSeleccionar = { onSeleccionarIngreso(it.ingresoId) }) }
