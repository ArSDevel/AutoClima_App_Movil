package com.example.myapplication.ui.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.components.colorEstado
import com.example.myapplication.ui.components.etiquetaEstado
import com.example.myapplication.ui.dashboard.components.BuscadorIngreso
import com.example.myapplication.ui.dashboard.components.DashboardDrawer
import com.example.myapplication.ui.dashboard.components.IngresoCard
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
    onVerDetalle: ((Long) -> Unit)? = null,
    onVerFotos: ((Long) -> Unit)? = null,
    onAbrirChecklist: ((Long) -> Unit)? = null,
    onAbrirDiagnostico: ((Long) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Recalcula las fechas relativas cuando volvemos a la pantalla.
    // El ViewModel solo repite la consulta si cambió el rango.
    DisposableEffect(lifecycleOwner, viewModel) {
        val lifecycle = lifecycleOwner.lifecycle
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) { viewModel.actualizarPeriodoFecha() } }
        lifecycle.addObserver(observador)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) { viewModel.actualizarPeriodoFecha() }
        onDispose { lifecycle.removeObserver(observador) } }

    DashboardContent(
        uiState = uiState, onBusquedaChange = viewModel::cambiarBusqueda,
        onEstadoChange = viewModel::cambiarEstado, onFechaChange = viewModel::cambiarFecha,
        onOrdenChange = viewModel::cambiarOrden, onLimpiarFiltros = viewModel::limpiarFiltros,
        onReintentar = viewModel::reintentar, onLogout = onLogout,
        onMenuSeleccionado = onMenuSeleccionado, onEditarIngreso = onEditarIngreso,
        onVerDetalle = onVerDetalle, onVerFotos = onVerFotos,
        onAbrirChecklist = onAbrirChecklist, onAbrirDiagnostico = onAbrirDiagnostico,
        modifier = modifier) }

// Contenido del listado: búsqueda, filtros por estado y tarjetas.
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class)
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
    onVerDetalle: ((Long) -> Unit)? = null,
    onVerFotos: ((Long) -> Unit)? = null,
    onAbrirChecklist: ((Long) -> Unit)? = null,
    onAbrirDiagnostico: ((Long) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }
    var mostrarFiltros by rememberSaveable { mutableStateOf(false) }

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
                            DashboardMenuOption.REGISTRO,
                            DashboardMenuOption.CHECKLIST,
                            DashboardMenuOption.DIAGNOSTICO,
                            DashboardMenuOption.EVIDENCIAS,
                            DashboardMenuOption.HISTORIAL -> {
                                onMenuSeleccionado(opcion) }
                            else -> snackbarState.showSnackbar(mensajePendiente) } } },
                onLogout = onLogout) }
    ) {
        Scaffold(
            containerColor = colors.background,
            snackbarHost = { SnackbarHost(snackbarState) },
            topBar = {
                Column {
                    EncabezadoAutoClimas(
                        titulo = stringResource(R.string.ingresos_title),
                        onMenu = { scope.launch { drawerState.open() } },
                        espacioInferior = 28.dp,
                        acciones = {
                            com.example.myapplication.ui.components.LogoAutoClimas(
                                sobreFondoOscuro = true,
                                descripcion = stringResource(R.string.dashboard_logo_description),
                                modifier = Modifier.size(width = 86.dp, height = 58.dp)) })
                    // La búsqueda se superpone a la parte inferior de la cabecera.
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .layout { medible, restricciones ->
                                val colocable = medible.measure(restricciones)
                                val solape = 28.dp.roundToPx()
                                layout(colocable.width, colocable.height - solape) {
                                    colocable.place(0, -solape) } },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BuscadorIngreso(
                            textoBusqueda = uiState.textoBusqueda,
                            onBusquedaChange = onBusquedaChange,
                            modifier = Modifier.weight(1f))
                        BotonFiltros(
                            activo = uiState.fechaSeleccionada != FiltroFechaIngreso.TODAS ||
                                    uiState.ordenSeleccionado != OrdenIngreso.RECIENTES,
                            onClick = { mostrarFiltros = true }) } } },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onMenuSeleccionado(DashboardMenuOption.REGISTRO) },
                    containerColor = colors.secondary,
                    contentColor = colors.onSecondary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.registro_title_new)) } }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "estados") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                ChipEstado(
                                    texto = stringResource(R.string.dashboard_filter_all),
                                    seleccionado = uiState.estadoSeleccionado == null,
                                    onClick = { onEstadoChange(null) }) }
                            items(EstadoIngreso.entries.filter { it != EstadoIngreso.NO_DISPONIBLE }) { estado ->
                                ChipEstado(
                                    texto = stringResource(etiquetaEstado(estado)),
                                    seleccionado = uiState.estadoSeleccionado == estado,
                                    onClick = { onEstadoChange(estado) },
                                    color = colorEstado(estado)) } } }

                    when {
                        uiState.cargando -> {
                            item(key = "carga_listado") {
                                PanelDashboard {
                                    CircularProgressIndicator(modifier = Modifier.align(
                                        Alignment.CenterHorizontally))
                                    Text(text = stringResource(R.string.dashboard_loading),
                                        modifier = Modifier.align(Alignment.CenterHorizontally)) } } }
                        uiState.hayError -> {
                            item(key = "error_listado") {
                                MensajeDashboard(titulo = stringResource(R.string.dashboard_load_error),
                                    textoAccion = stringResource(R.string.dashboard_retry),
                                    onAccion = onReintentar) } }

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
                                        } else { onMenuSeleccionado(DashboardMenuOption.REGISTRO) } }) } }

                        else -> {
                            items(
                                items = uiState.ingresos,
                                key = { "ingreso_${it.ingresoId}" }
                            ) { ingreso ->
                                val estado = EstadoIngreso.desdeValor(ingreso.estado)
                                IngresoCard(
                                    ingreso = ingreso,
                                    onVerDetalle = onVerDetalle,
                                    onVerFotos = onVerFotos,
                                    onAbrirChecklist = onAbrirChecklist,
                                    onAbrirDiagnostico = onAbrirDiagnostico,
                                    // El acceso rápido a edición solo aparece cuando corresponde.
                                    onEditar = if (onEditarIngreso != null && ReglasIngreso.editar(estado)) {
                                        onEditarIngreso
                                    } else null) } } } } } }

        if (mostrarFiltros) {
            ModalBottomSheet(
                onDismissRequest = { mostrarFiltros = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.filtros_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold)
                        TextButton(onClick = onLimpiarFiltros) {
                            Text(stringResource(R.string.dashboard_clear_filters)) } }
                    GrupoFiltro(titulo = stringResource(R.string.filtros_state)) {
                        ChipEstado(
                            texto = stringResource(R.string.dashboard_filter_all),
                            seleccionado = uiState.estadoSeleccionado == null,
                            onClick = { onEstadoChange(null) })
                        EstadoIngreso.entries.forEach { estado ->
                            ChipEstado(
                                texto = stringResource(etiquetaEstado(estado)),
                                seleccionado = uiState.estadoSeleccionado == estado,
                                onClick = { onEstadoChange(estado) }) } }
                    GrupoFiltro(titulo = stringResource(R.string.dashboard_date_filter_label)) {
                        FiltroFechaIngreso.entries.forEach { filtro ->
                            ChipEstado(
                                texto = stringResource(etiquetaFecha(filtro)),
                                seleccionado = uiState.fechaSeleccionada == filtro,
                                onClick = { onFechaChange(filtro) }) } }
                    GrupoFiltro(titulo = stringResource(R.string.dashboard_sort_label)) {
                        OrdenIngreso.entries.forEach { orden ->
                            ChipEstado(
                                texto = stringResource(etiquetaOrden(orden)),
                                seleccionado = uiState.ordenSeleccionado == orden,
                                onClick = { onOrdenChange(orden) }) } }
                    Button(
                        onClick = { mostrarFiltros = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.pluralStringResource(R.plurals.filtros_show_results, uiState.cantidadResultados, uiState.cantidadResultados),
                            style = MaterialTheme.typography.titleMedium) } } } } } }

// Grupo de opciones con título dentro de la hoja de filtros.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrupoFiltro(
    titulo: String,
    contenido: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) { contenido() } } }

// Chip de filtro; si recibe color muestra un punto de ese color.
@Composable
private fun ChipEstado(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color? = null
) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = if (color != null) {
            { Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (seleccionado) MaterialTheme.colorScheme.onPrimary else color)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary)) }

// Botón que abre la hoja de filtros; marca si hay filtros activos.
@Composable
private fun BotonFiltros(
    activo: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            BadgedBox(badge = { if (activo) Badge(containerColor = MaterialTheme.colorScheme.secondary) }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = stringResource(R.string.filtros_open)) } } } }

// Texto de cada periodo de fecha.
@StringRes
private fun etiquetaFecha(filtro: FiltroFechaIngreso): Int {
    return when (filtro) {
        FiltroFechaIngreso.TODAS -> R.string.dashboard_date_all
        FiltroFechaIngreso.HOY -> R.string.dashboard_date_today
        FiltroFechaIngreso.ULTIMOS_SIETE_DIAS -> R.string.dashboard_date_last_seven_days
        FiltroFechaIngreso.ESTE_MES -> R.string.dashboard_date_this_month } }

// Texto de cada orden del listado.
@StringRes
private fun etiquetaOrden(orden: OrdenIngreso): Int {
    return when (orden) {
        OrdenIngreso.RECIENTES -> R.string.dashboard_sort_recent
        OrdenIngreso.ANTIGUOS -> R.string.dashboard_sort_oldest
        OrdenIngreso.PLACA_ASCENDENTE -> R.string.dashboard_sort_plate } }

// Panel blanco para mensajes y avisos del listado.
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
            verticalArrangement = Arrangement.spacedBy(12.dp), content = contenido) } }

// Mensaje con una acción, para listado vacío o con error.
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
                color = MaterialTheme.colorScheme.onSurfaceVariant) }
        TextButton(onClick = onAccion) { Text(textoAccion) } } }

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
        estado = EstadoIngreso.EN_REPARACION.name),
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
        estado = EstadoIngreso.LISTO_PARA_FIRMA.name))

private val estadoDashboardPreview = DashboardUiState(
    cargando = false,
    ingresos = ingresosPreview)

// Contenido de ejemplo para las vistas previas.
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
        onVerDetalle = {}) }

// Vista previa del listado en modo claro.
@Preview(
    name = "Dashboard · teléfono",
    showBackground = true,
    widthDp = 360,
    heightDp = 1000)
@Preview(
    name = "Dashboard · amplio",
    showBackground = true,
    widthDp = 800,
    heightDp = 1000)
@Composable
private fun DashboardClaroPreview() {
    MyApplicationTheme(darkTheme = false) {
        ContenidoDashboardPreview() } }

// Vista previa del listado en modo oscuro.
@Preview(
    name = "Dashboard · oscuro",
    showBackground = true,
    widthDp = 360,
    heightDp = 1000)
@Composable
private fun DashboardOscuroPreview() {
    MyApplicationTheme(darkTheme = true) {
        ContenidoDashboardPreview() } }

// Vista previa del listado con error de carga.
@Preview(
    name = "Dashboard · errores",
    showBackground = true,
    widthDp = 400,
    heightDp = 1000)
@Composable
private fun DashboardErroresPreview() {
    MyApplicationTheme(darkTheme = false) {
        ContenidoDashboardPreview(
            uiState = DashboardUiState(
                cargando = false,
                hayError = true)) } }
