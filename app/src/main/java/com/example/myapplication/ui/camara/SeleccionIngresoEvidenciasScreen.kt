package com.example.myapplication.ui.camara

import androidx.compose.runtime.Composable
import com.example.myapplication.R
import com.example.myapplication.ui.components.SeleccionIngresoScreen
import com.example.myapplication.ui.dashboard.DashboardViewModel

// Lista para elegir el ingreso cuyas fotos se quieren ver.
@Composable
fun SeleccionIngresoEvidenciasScreen(
    viewModel: DashboardViewModel,
    onVolver: () -> Unit,
    onSeleccionarIngreso: (Long) -> Unit) {
    SeleccionIngresoScreen(
        viewModel = viewModel,
        titulo = R.string.evidence_selector_title,
        textoError = R.string.evidence_selector_error,
        textoReintentar = R.string.camera_retry,
        textoVacio = R.string.evidence_selector_empty,
        textoSinResultados = R.string.evidence_selector_no_results,
        onVolver = onVolver,
        onSeleccionar = { onSeleccionarIngreso(it.ingresoId) }) }
