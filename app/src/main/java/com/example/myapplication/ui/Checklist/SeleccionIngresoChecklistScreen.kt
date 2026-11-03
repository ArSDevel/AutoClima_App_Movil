package com.example.myapplication.ui.checklist

import androidx.compose.runtime.Composable
import com.example.myapplication.R
import com.example.myapplication.ui.components.SeleccionIngresoScreen
import com.example.myapplication.ui.dashboard.DashboardViewModel

// Permite elegir el ingreso antes de abrir su checklist.
// Utiliza un ViewModel propio de esta entrada de navegación.
@Composable
fun SeleccionIngresoChecklistScreen(
    viewModel: DashboardViewModel,
    onVolver: () -> Unit,
    onSeleccionarIngreso: (Long) -> Unit) {
    SeleccionIngresoScreen(
        viewModel = viewModel,
        titulo = R.string.checklist_selector_title,
        textoError = R.string.checklist_selector_error,
        textoReintentar = R.string.checklist_retry,
        textoVacio = R.string.checklist_selector_empty,
        textoSinResultados = R.string.checklist_selector_no_results,
        onVolver = onVolver,
        onSeleccionar = { onSeleccionarIngreso(it.ingresoId) }) }
