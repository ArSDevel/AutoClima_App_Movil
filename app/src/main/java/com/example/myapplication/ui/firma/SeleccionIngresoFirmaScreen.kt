package com.example.myapplication.ui.firma

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.components.TarjetaSeleccionIngreso
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.dashboard.components.BuscadorIngreso

// Lista los ingresos que están por firmarse o entregarse. Al elegir uno abre
// la firma si falta y la entrega si ya está firmado.
@Composable
fun SeleccionIngresoFirmaScreen(
    viewModel: DashboardViewModel,
    onVolver: () -> Unit,
    onFirmar: (Long) -> Unit,
    onEntregar: (Long) -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val pendientes = estado.ingresos.filter {
        EstadoIngreso.desdeValor(it.estado).let { e -> e == EstadoIngreso.LISTO_PARA_FIRMA || e == EstadoIngreso.LISTO_PARA_ENTREGA } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { EncabezadoAutoClimas(titulo = stringResource(R.string.firma_selector_title), onVolver = onVolver) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier.widthIn(max = 840.dp).fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BuscadorIngreso(textoBusqueda = estado.textoBusqueda, onBusquedaChange = viewModel::cambiarBusqueda)
                when {
                    estado.cargando -> Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator() }
                    estado.hayError -> Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.diagnostico_selector_error))
                        Button(onClick = viewModel::reintentar) { Text(stringResource(R.string.diagnostico_retry)) } }
                    pendientes.isEmpty() -> Text(
                        text = stringResource(if (estado.textoBusqueda.isBlank()) R.string.firma_selector_empty
                        else R.string.diagnostico_selector_no_results),
                        modifier = Modifier.padding(vertical = 24.dp), style = MaterialTheme.typography.bodyLarge)
                    else -> LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = pendientes, key = { it.ingresoId }) { ingreso ->
                            TarjetaSeleccionIngreso(
                                ingreso = ingreso,
                                onClick = {
                                    if (EstadoIngreso.desdeValor(ingreso.estado) == EstadoIngreso.LISTO_PARA_FIRMA) {
                                        onFirmar(ingreso.ingresoId)
                                    } else {
                                        onEntregar(ingreso.ingresoId) } }) } } } } } } }
