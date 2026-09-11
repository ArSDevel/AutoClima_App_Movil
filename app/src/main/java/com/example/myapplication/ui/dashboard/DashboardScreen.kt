package com.example.myapplication.ui.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.data.local.model.ReglasIngreso
import com.example.myapplication.ui.dashboard.components.BuscadorIngreso
import com.example.myapplication.ui.dashboard.components.DashboardDrawer
import com.example.myapplication.ui.dashboard.components.FiltroEstadoIngreso
import com.example.myapplication.ui.dashboard.components.IngresoCard
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

// Conecta el estado del ViewModel con la pantalla.
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit,
    onMenuSeleccionado: (DashboardMenuOption) -> Unit = {},
    modifier: Modifier = Modifier,
    onEditarIngreso: ((Long) -> Unit)? = null,
    onVerDetalle: ((Long) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Recalcula las fechas relativas cuando volvemos a la pantalla.
    // El ViewModel solo repite la consulta si cambió el rango.
    DisposableEffect(lifecycleOwner, viewModel) {
        val lifecycle = lifecycleOwner.lifecycle
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) { viewModel.actualizarPeriodoFecha() }
        }
        lifecycle.addObserver(observador)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) { viewModel.actualizarPeriodoFecha() }
        onDispose { lifecycle.removeObserver(observador) }
    }

    DashboardContent(
        uiState = uiState, onBusquedaChange = viewModel::cambiarBusqueda,
        onEstadoChange = viewModel::cambiarEstado, onFechaChange = viewModel::cambiarFecha,
        onOrdenChange = viewModel::cambiarOrden, onLimpiarFiltros = viewModel::limpiarFiltros,
        onReintentar = viewModel::reintentar, onLogout = onLogout,
        onMenuSeleccionado = onMenuSeleccionado, onEditarIngreso = onEditarIngreso,
        onVerDetalle = onVerDetalle, modifier = modifier
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onBusquedaChange: (String) -> Unit,
    onEstadoChange: (EstadoIngreso?) -> Unit,
    onFechaChange: (FiltroFechaIngreso) -> Unit,
    onOrdenChange: (OrdenIngreso) -> Unit,
    onLimpiarFiltros: () -> Unit,
    onReintentar: () -> Unit,
    onLogout: () -> Unit,
    onMenuSeleccionado: (DashboardMenuOption) -> Unit,
    modifier: Modifier = Modifier,
    onEditarIngreso: ((Long) -> Unit)? = null,
    onVerDetalle: ((Long) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }

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
                        when (opcion) {
                            DashboardMenuOption.DASHBOARD -> Unit
                            DashboardMenuOption.REGISTRO -> onMenuSeleccionado(opcion)
                            else -> snackbarState.showSnackbar(mensajePendiente)
                        }
                    }
                },
                onLogout = onLogout
            )
        }
    ) {
        Scaffold(
            containerColor = colors.background,
            snackbarHost = { SnackbarHost(snackbarState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(text = stringResource(R.string.dashboard_title),
                                style = MaterialTheme.typography.titleLarge)
                            Text(text = stringResource(R.string.dashboard_section_title),
                                style = MaterialTheme.typography.labelMedium)
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.dashboard_open_menu))
                        }
                    },
                    actions = {
                        Image(painter = painterResource(R.drawable.logo_autoclimas),
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
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().background(AutoClimasGradients.loginBackground)
                    .padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(modifier = Modifier.widthIn(max = 1000.dp).fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(key = "presentacion") {
                        PanelDashboard {
                            Text(text = stringResource(R.string.dashboard_section_title),
                                style = MaterialTheme.typography.headlineSmall)
                            Text(text = stringResource(R.string.dashboard_overview_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant)
                            Button(onClick = { onMenuSeleccionado(DashboardMenuOption.REGISTRO) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Add,
                                    contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.registro_title_new))
                            }
                        }
                    }
                    item(key = "filtros") {
                        PanelDashboard {
                            Text(text = stringResource(R.string.dashboard_filters_title),
                                style = MaterialTheme.typography.titleMedium)
                            BuscadorIngreso(textoBusqueda = uiState.textoBusqueda, onBusquedaChange = onBusquedaChange)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FiltroEstadoIngreso(estadoSeleccionado = uiState.estadoSeleccionado,
                                    onEstadoSeleccionado = onEstadoChange)
                                SelectorDashboard(
                                    titulo = stringResource(R.string.dashboard_date_filter_label),
                                    seleccionado = uiState.fechaSeleccionada, opciones = FiltroFechaIngreso.entries,
                                    etiqueta = ::etiquetaFecha, onSeleccionar = onFechaChange)
                                SelectorDashboard(
                                    titulo = stringResource(R.string.dashboard_sort_label),
                                    seleccionado = uiState.ordenSeleccionado, opciones = OrdenIngreso.entries,
                                    etiqueta = ::etiquetaOrden, onSeleccionar = onOrdenChange)
                            }

                            if (uiState.hayFiltrosActivos) {
                                TextButton(onClick = onLimpiarFiltros) {
                                    Text(stringResource(R.string.dashboard_clear_filters))
                                }
                            }
                            if (!uiState.cargando && !uiState.hayError) {
                                Text(text = stringResource(R.string.dashboard_results_count, uiState.cantidadResultados),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }

                    when {
                        uiState.cargando -> {
                            item(key = "carga_listado") {
                                PanelDashboard {
                                    CircularProgressIndicator(modifier = Modifier.align(
                                            Alignment.CenterHorizontally))
                                    Text(text = stringResource(R.string.dashboard_loading),
                                        modifier = Modifier.align(Alignment.CenterHorizontally))
                                }
                            }
                        }
                        uiState.hayError -> {
                            item(key = "error_listado") {
                                MensajeDashboard(titulo = stringResource(R.string.dashboard_load_error),
                                    textoAccion = stringResource(R.string.dashboard_retry),
                                    onAccion = onReintentar)
                            }
                        }

                        uiState.ingresos.isEmpty() -> {
                            item(key = "listado_vacio") {
                                MensajeDashboard(
                                    titulo = stringResource(
                                        if (uiState.hayFiltrosActivos) { R.string.dashboard_no_results_title }
                                        else { R.string.dashboard_empty_title }),
                                    descripcion = stringResource(
                                        if (uiState.hayFiltrosActivos) { R.string.dashboard_filter_no_results_description
                                        } else { R.string.dashboard_empty_description }),
                                    textoAccion = stringResource(
                                        if (uiState.hayFiltrosActivos) { R.string.dashboard_clear_filters
                                        } else { R.string.registro_title_new }),
                                    onAccion = {
                                        if (uiState.hayFiltrosActivos) { onLimpiarFiltros()
                                        } else { onMenuSeleccionado(DashboardMenuOption.REGISTRO) }
                                    }
                                )
                            }
                        }

                        else -> {
                            items(
                                items = uiState.ingresos,
                                key = { "ingreso_${it.ingresoId}" }
                            ) { ingreso ->
                                val estado = EstadoIngreso.desdeValor(ingreso.estado)
                                // Solo mostramos el acceso rápido a edición
                                // cuando corresponde y está conectado.
                                val acciones =
                                    if (onEditarIngreso != null && ReglasIngreso.editar(estado)) {
                                        listOf(IngresoAccion.EDITAR)
                                    } else { emptyList() }

                                IngresoCard(
                                    ingreso = ingreso,
                                    acciones = acciones,
                                    onAccion = { accion, ingresoId ->
                                        if (accion == IngresoAccion.EDITAR) { onEditarIngreso?.invoke(ingresoId) } },
                                    onVerDetalle = onVerDetalle
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Selector visual compartido por fecha y orden.
 * Solo comunica la selección; no realiza consultas.
 */
@Composable
private fun <T> SelectorDashboard(
    titulo: String,
    seleccionado: T,
    opciones: List<T>,
    etiqueta: (T) -> Int,
    onSeleccionar: (T) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val textoSeleccionado = stringResource(etiqueta(seleccionado))
    Box {
        OutlinedButton(onClick = { abierto = true },
            shape = MaterialTheme.shapes.small
        ) {
            Text(text = stringResource(R.string.dashboard_selector_value, titulo,
                    textoSeleccionado),
                style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null)
        }
        DropdownMenu(expanded = abierto,
            onDismissRequest = { abierto = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(stringResource(etiqueta(opcion))) },
                    trailingIcon = {
                        if (opcion == seleccionado) {
                            Icon(imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    onClick = { abierto = false
                        onSeleccionar(opcion)
                    }
                )
            }
        }
    }
}

@StringRes
private fun etiquetaFecha(filtro: FiltroFechaIngreso): Int {
    return when (filtro) {
        FiltroFechaIngreso.TODAS -> R.string.dashboard_date_all
        FiltroFechaIngreso.HOY -> R.string.dashboard_date_today
        FiltroFechaIngreso.ULTIMOS_SIETE_DIAS -> R.string.dashboard_date_last_seven_days
        FiltroFechaIngreso.ESTE_MES -> R.string.dashboard_date_this_month
    }
}

@StringRes
private fun etiquetaOrden(orden: OrdenIngreso): Int {
    return when (orden) {
        OrdenIngreso.RECIENTES -> R.string.dashboard_sort_recent
        OrdenIngreso.ANTIGUOS -> R.string.dashboard_sort_oldest
        OrdenIngreso.PLACA_ASCENDENTE -> R.string.dashboard_sort_plate
    }
}

@Composable
private fun PanelDashboard(
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), content = contenido)
    }
}

@Composable
private fun MensajeDashboard(
    titulo: String,
    textoAccion: String,
    onAccion: () -> Unit,
    descripcion: String? = null
) {
    PanelDashboard {
        Text(text = titulo,
            style = MaterialTheme.typography.titleMedium)
        if (descripcion != null) {
            Text(text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onAccion) { Text(textoAccion) }
    }
}

// Datos exclusivos de las previews.
private val ingresosPreview = listOf(
    IngresoResumen(
        ingresoId = 1L,
        placa = "ABC-123",
        modelo = "Honda Civic",
        color = "Rojo",
        numeroSerie = "SERIE-DE-EJEMPLO",
        nombreCliente = "Cliente de ejemplo",
        telefonoCliente = "8100000000",
        emailCliente = "cliente@example.com",
        fechaEntrada = 1788955200000L,
        estado = EstadoIngreso.EN_REPARACION.name
    ),
    IngresoResumen(
        ingresoId = 2L,
        placa = "XYZ-456",
        modelo = "Nissan Versa",
        color = "Azul",
        numeroSerie = null,
        nombreCliente = "Otro cliente",
        telefonoCliente = "8111111111",
        emailCliente = null,
        fechaEntrada = 1788955200000L,
        estado = EstadoIngreso.LISTO_PARA_FIRMA.name
    )
)

private val estadoDashboardPreview = DashboardUiState(
    cargando = false,
    ingresos = ingresosPreview
)

@Composable
private fun ContenidoDashboardPreview(
    uiState: DashboardUiState = estadoDashboardPreview
) {
    DashboardContent(
        uiState = uiState,
        onBusquedaChange = {},
        onEstadoChange = {},
        onFechaChange = {},
        onOrdenChange = {},
        onLimpiarFiltros = {},
        onReintentar = {},
        onLogout = {},
        onMenuSeleccionado = {},
        onEditarIngreso = {},
        onVerDetalle = {}
    )
}

@Preview(
    name = "Dashboard · teléfono",
    showBackground = true,
    widthDp = 360,
    heightDp = 1000
)
@Preview(
    name = "Dashboard · amplio",
    showBackground = true,
    widthDp = 800,
    heightDp = 1000
)
@Composable
private fun DashboardClaroPreview() {
    MyApplicationTheme(darkTheme = false) {
        ContenidoDashboardPreview()
    }
}

@Preview(
    name = "Dashboard · oscuro",
    showBackground = true,
    widthDp = 360,
    heightDp = 1000
)
@Composable
private fun DashboardOscuroPreview() {
    MyApplicationTheme(darkTheme = true) {
        ContenidoDashboardPreview()
    }
}

@Preview(
    name = "Dashboard · errores",
    showBackground = true,
    widthDp = 400,
    heightDp = 1000
)
@Composable
private fun DashboardErroresPreview() {
    MyApplicationTheme(darkTheme = false) {
        ContenidoDashboardPreview(
            uiState = DashboardUiState(
                cargando = false,
                hayError = true
            )
        )
    }
}