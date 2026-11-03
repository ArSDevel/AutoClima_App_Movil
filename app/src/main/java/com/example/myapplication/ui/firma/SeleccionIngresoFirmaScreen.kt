package com.example.myapplication.ui.firma

import androidx.compose.runtime.Composable
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.ui.components.SeleccionIngresoScreen
import com.example.myapplication.ui.dashboard.DashboardViewModel

// Lista los ingresos que están por firmarse o entregarse. Al elegir uno abre
// la firma si falta y la entrega si ya está firmado.
@Composable
fun SeleccionIngresoFirmaScreen(
    viewModel: DashboardViewModel,
    onVolver: () -> Unit,
    onFirmar: (Long) -> Unit,
    onEntregar: (Long) -> Unit) {
    SeleccionIngresoScreen(
        viewModel = viewModel,
        titulo = R.string.firma_selector_title,
        textoError = R.string.diagnostico_selector_error,
        textoReintentar = R.string.diagnostico_retry,
        textoVacio = R.string.firma_selector_empty,
        textoSinResultados = R.string.diagnostico_selector_no_results,
        onVolver = onVolver,
        filtro = {
            EstadoIngreso.desdeValor(it.estado).let { e ->
                e == EstadoIngreso.LISTO_PARA_FIRMA || e == EstadoIngreso.LISTO_PARA_ENTREGA } },
        onSeleccionar = { ingreso ->
            if (EstadoIngreso.desdeValor(ingreso.estado) == EstadoIngreso.LISTO_PARA_FIRMA) {
                onFirmar(ingreso.ingresoId)
            } else {
                onEntregar(ingreso.ingresoId) } }) }
