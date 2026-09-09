package com.example.myapplication.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.ui.dashboard.components.BuscadorIngreso
import com.example.myapplication.ui.dashboard.components.DashboardDrawer
import com.example.myapplication.ui.dashboard.components.IngresoCard
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle


// Conecta el estado del ViewModel con la interfaz del Dashboard.
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        uiState = uiState,
        onBusquedaChange = viewModel::cambiarBusqueda,
        onReintentar = viewModel::reintentar,
        onLogout = onLogout,
        modifier = modifier,
        accionesDisponibles = listOf(IngresoAccion.EDITAR, IngresoAccion.ELIMINAR)
    )
}

/**
 * Pantalla principal (Dashboard) que se muestra al iniciar sesión correctamente.
 *
 * @param userId ID del usuario que inició sesión.
 * @param onLogout Callback que se ejecuta cuando el usuario presiona el botón de cerrar sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onBusquedaChange: (String) -> Unit,
    onReintentar: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    accionesDisponibles: List<IngresoAccion> = emptyList(),
    onAccionIngreso: ((IngresoAccion, Long) -> Unit)? = null,
    onMenuSeleccionado: ((DashboardMenuOption) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState()}
    val mensajePendiente = stringResource(R.string.dashboard_section_pending)

    ModalNavigationDrawer(
        modifier = modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = {
            DashboardDrawer(
                opcionSeleccionada = DashboardMenuOption.DASHBOARD,

                onOpcionSeleccionada = { opcion ->
                    scope.launch {
                        drawerState.close()
                        if (opcion != DashboardMenuOption.DASHBOARD) {
                            if (onMenuSeleccionado != null) {
                                onMenuSeleccionado(opcion)
                            } else {
                                snackbarState.showSnackbar(
                                    mensajePendiente
                                )
                            }
                        }
                    }
                },
                onLogout = onLogout
            )
        }
    ) {
        Scaffold(
            containerColor = colors.background,

            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(R.string.dashboard_title),
                            style = typography.titleLarge)
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.dashboard_open_menu)
                            )
                        }
                    },

                    actions = {
                        Image(
                            painter = painterResource(R.drawable.logo_autoclimas),
                            contentDescription = stringResource(R.string.dashboard_logo_description),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.padding(end = 12.dp).width(72.dp).height(56.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.tertiary,
                        titleContentColor = colors.onTertiary,
                        navigationIconContentColor = colors.onTertiary
                    )
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AutoClimasGradients.loginBackground)
                    .padding(innerPadding)
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BuscadorIngreso(
                    textoBusqueda = uiState.textoBusqueda,
                    onBusquedaChange = onBusquedaChange
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = colors.surface,
                    contentColor = colors.onSurface
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_section_title),
                        style = typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        // Primera consulta o búsqueda en proceso
                        uiState.cargando -> {
                            Surface(
                                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                color = colors.surface,
                                contentColor = colors.onSurface
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator()
                                    Text(
                                        text = stringResource(R.string.dashboard_loading),
                                        style = typography.bodyMedium)
                                }
                            }
                        }

                        // La consulta falló
                        uiState.hayError -> {
                            MensajeDashboard(titulo = stringResource(R.string.dashboard_load_error),
                                onReintentar = onReintentar
                            )
                        }

                        // La consulta terminó sin resultados.
                        uiState.ingresos.isEmpty() -> {
                            val hayBusqueda = uiState.textoBusqueda.isNotBlank()
                            MensajeDashboard(
                                titulo = stringResource(
                                    if (hayBusqueda) { R.string.dashboard_no_results_title
                                    } else { R.string.dashboard_empty_title}
                                ),
                                descripcion = stringResource(
                                    if (hayBusqueda) { R.string.dashboard_no_results_description
                                    } else { R.string.dashboard_empty_description }
                                )
                            )
                        }

                        // Hay ingresos para mostrar
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(items = uiState.ingresos, key = { ingreso -> ingreso.ingresoId }
                                ) { ingreso ->
                                    IngresoCard(
                                        ingreso = ingreso,
                                        acciones = accionesDisponibles,
                                        onAccion = onAccionIngreso
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MensajeDashboard(
    titulo: String,
    modifier: Modifier = Modifier,
    descripcion: String? = null,
    onReintentar: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium)
            if (descripcion != null) {
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onReintentar != null) {
                TextButton(onClick = onReintentar
                ) {
                    Text(text = stringResource(R.string.dashboard_retry))
                }
            }
        }
    }
}

@Preview(
    name = "Dashboard integrado",
    showBackground = true,
    widthDp = 800,
    heightDp = 852
)
@Composable
private fun DashboardContentPreview() {
    MyApplicationTheme(darkTheme = false) {
        DashboardContent(
            uiState = DashboardUiState(
                cargando = false,
                ingresos = listOf(
                    IngresoResumen(
                        ingresoId = 1L,
                        placa = "ABC-123",
                        modelo = "Honda Civic",
                        color = "Rojo",
                        numeroSerie = "SERIE-DE-EJEMPLO",
                        nombreCliente = "Cliente de ejemplo",
                        telefonoCliente = "8100000000",
                        emailCliente = "cliente@example.com",
                        fechaEntrada = 1788955200000L
                    )
                )
            ),

            onBusquedaChange = {},
            onReintentar = {},
            onLogout = {},

            accionesDisponibles = listOf(
                IngresoAccion.VER_PDF,
                IngresoAccion.FIRMAR,
                IngresoAccion.EDITAR,
                IngresoAccion.ELIMINAR
            ),

            onAccionIngreso = { _, _ -> }
        )
    }
}