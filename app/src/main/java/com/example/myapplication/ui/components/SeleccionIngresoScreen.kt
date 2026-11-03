package com.example.myapplication.ui.components

import androidx.annotation.StringRes
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.dashboard.components.BuscadorIngreso

// Lista con buscador para elegir un ingreso antes de abrir uno de sus módulos (checklist, evidencias, diagnóstico o firma).
// Cada módulo aporta solo sus textos, qué ingresos ofrece y qué hacer al elegir uno.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionIngresoScreen(
    viewModel: DashboardViewModel,
    @StringRes titulo: Int,
    @StringRes textoError: Int,
    @StringRes textoReintentar: Int,
    @StringRes textoVacio: Int,
    @StringRes textoSinResultados: Int,
    onVolver: () -> Unit,
    // Ingresos que se ofrecen; por defecto todos los de la lista.
    filtro: (IngresoResumen) -> Boolean = { true },
    onSeleccionar: (IngresoResumen) -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val ingresos = estado.ingresos.filter(filtro)

    Scaffold(
        topBar = {
            EncabezadoAutoClimas(
                titulo = stringResource(titulo),
                onVolver = onVolver) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BuscadorIngreso(
                    textoBusqueda = estado.textoBusqueda,
                    onBusquedaChange = viewModel::cambiarBusqueda)

                when {
                    estado.cargando -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator() } }

                    estado.hayError -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(textoError))
                            Button(onClick = viewModel::reintentar) {
                                Text(stringResource(textoReintentar)) } } }

                    ingresos.isEmpty() -> {
                        Text(
                            text = stringResource(
                                if (estado.textoBusqueda.isBlank()) textoVacio else textoSinResultados),
                            modifier = Modifier.padding(vertical = 24.dp),
                            style = MaterialTheme.typography.bodyLarge) }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(
                                items = ingresos,
                                key = { it.ingresoId }
                            ) { ingreso ->
                                TarjetaSeleccionIngreso(
                                    ingreso = ingreso,
                                    onClick = { onSeleccionar(ingreso) }) } } } } } } } }
