package com.example.myapplication.ui.diagnostico

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.ui.dashboard.DashboardViewModel

// Permite elegir el expediente cuyo diagnóstico se desea abrir.
// La consulta utiliza una instancia propia del ViewModel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionIngresoDiagnosticoScreen(
    viewModel: DashboardViewModel,
    onVolver: () -> Unit,
    onSeleccionarIngreso: (Long) -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            R.string.diagnostico_selector_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.diagnostico_back
                            )
                        )
                    }
                }
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
                    .padding(start = 16.dp,
                        end = 16.dp,
                        top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.diagnostico_selector_description
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = estado.textoBusqueda,
                    onValueChange = viewModel::cambiarBusqueda,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text(
                            stringResource(
                                R.string.diagnostico_selector_search
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    shape = MaterialTheme.shapes.medium
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(
                                    R.string.diagnostico_selector_error
                                )
                            )

                            Button(onClick = viewModel::reintentar) {
                                Text(
                                    stringResource(
                                        R.string.diagnostico_retry
                                    )
                                )
                            }
                        }
                    }

                    estado.ingresos.isEmpty() -> {
                        Text(
                            text = stringResource(
                                if (estado.textoBusqueda.isBlank()) {
                                    R.string.diagnostico_selector_empty
                                } else {
                                    R.string.diagnostico_selector_no_results
                                }
                            ),
                            modifier = Modifier.padding(vertical = 24.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = estado.ingresos,
                                key = { it.ingresoId }
                            ) { ingreso ->
                                IngresoParaDiagnostico(
                                    ingreso = ingreso,
                                    onSeleccionar = {
                                        onSeleccionarIngreso(ingreso.ingresoId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Muestra los datos necesarios para identificar el ingreso.
// El número distingue diferentes visitas del mismo vehículo.
@Composable
private fun IngresoParaDiagnostico(
    ingreso: IngresoResumen,
    onSeleccionar: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        contentColor = colors.onSurface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = ingreso.placa,
                style = MaterialTheme.typography.titleLarge,
                color = colors.primary
            )

            Text(
                text = ingreso.modelo,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = ingreso.nombreCliente,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Text(
                text = stringResource(
                    R.string.diagnostico_data_ingreso,
                    ingreso.ingresoId
                ),
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant
            )

            Button(
                onClick = onSeleccionar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        R.string.diagnostico_selector_open
                    )
                )
            }
        }
    }
}