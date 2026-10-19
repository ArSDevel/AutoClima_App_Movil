package com.example.myapplication.ui.camara

import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.components.TarjetaSeleccionIngreso
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionIngresoEvidenciasScreen(
    viewModel: DashboardViewModel,
    onVolver: () -> Unit,
    onSeleccionarIngreso: (Long) -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            EncabezadoAutoClimas(
                titulo = stringResource(R.string.evidence_selector_title),
                onVolver = onVolver
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {


                com.example.myapplication.ui.dashboard.components.BuscadorIngreso(
                    textoBusqueda = estado.textoBusqueda,
                    onBusquedaChange = viewModel::cambiarBusqueda
                )

                when {
                    estado.cargando -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    estado.hayError -> {
                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.evidence_selector_error
                                ),
                                color = colors.error
                            )

                            Button(
                                onClick = viewModel::reintentar
                            ) {
                                Text(
                                    stringResource(
                                        R.string.camera_retry
                                    )
                                )
                            }
                        }
                    }

                    estado.ingresos.isEmpty() -> {
                        Text(
                            text = stringResource(
                                if (estado.textoBusqueda.isBlank()) {
                                    R.string.evidence_selector_empty
                                } else {
                                    R.string.evidence_selector_no_results
                                }
                            ),
                            color = colors.onSurfaceVariant
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                bottom = 24.dp
                            ),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = estado.ingresos,
                                key = { ingreso -> ingreso.ingresoId }
                            ) { ingreso ->
                                TarjetaSeleccionIngreso(
                                    ingreso = ingreso,
                                    onClick = { onSeleccionarIngreso(ingreso.ingresoId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}