package com.example.myapplication.ui.historial

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.dashboard.DashboardMenuOption
import com.example.myapplication.ui.dashboard.components.BuscadorIngreso
import com.example.myapplication.ui.dashboard.components.DashboardDrawer
import com.example.myapplication.ui.historial.components.EventoHistorialCard
import kotlinx.coroutines.launch

// Actividad de todos los técnicos, de lo más reciente a lo más antiguo.
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    onVolver: () -> Unit,
    onAbrirIngreso: (Long) -> Unit,
    onMenuSeleccionado: (DashboardMenuOption) -> Unit = {},
    onLogout: () -> Unit = {}) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler {
        if (drawerState.isOpen) { scope.launch { drawerState.close() } } else { onVolver() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DashboardDrawer(
                opcionSeleccionada = DashboardMenuOption.HISTORIAL,
                onOpcionSeleccionada = { opcion ->
                    scope.launch {
                        drawerState.close()
                        if (opcion != DashboardMenuOption.HISTORIAL) onMenuSeleccionado(opcion) } },
                onLogout = { scope.launch { drawerState.close(); onLogout() } }) }) {
        Scaffold(
            containerColor = colors.background,
            topBar = {
                Column {
                    EncabezadoAutoClimas(
                        titulo = stringResource(R.string.historial_title),
                        onMenu = { scope.launch { drawerState.open() } },
                        espacioInferior = 28.dp)
                    // La búsqueda se superpone a la parte inferior de la cabecera.
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .layout { medible, restricciones ->
                                val colocable = medible.measure(restricciones)
                                val solape = 28.dp.roundToPx()
                                layout(colocable.width, colocable.height - solape) {
                                    colocable.place(0, -solape) } }) {
                        BuscadorIngreso(
                            textoBusqueda = estado.busqueda,
                            onBusquedaChange = viewModel::buscar,
                            textoGuia = stringResource(R.string.historial_search)) } } }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item(key = "filtros") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                ChipHistorial(stringResource(R.string.historial_chip_all), estado.accion == null) {
                                    viewModel.filtrarAccion(null) } }
                            items(listOf(
                                "ESTADO" to R.string.historial_chip_states,
                                "FOTOS" to R.string.historial_photos,
                                "CHECKLIST" to R.string.dashboard_action_checklist,
                                "DIAGNOSTICO" to R.string.dashboard_action_diagnosis
                            )) { (accion, texto) ->
                                ChipHistorial(stringResource(texto), estado.accion == accion) {
                                    viewModel.filtrarAccion(accion) } }
                            item {
                                ChipHistorial(stringResource(R.string.historial_today),
                                    estado.periodo == PeriodoHistorial.HOY) {
                                    viewModel.filtrarPeriodo(
                                        if (estado.periodo == PeriodoHistorial.HOY) PeriodoHistorial.TODOS
                                        else PeriodoHistorial.HOY) } } } }
                    if (estado.cargando && estado.eventos.isEmpty()) {
                        item(key = "carga") {
                            Row(Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(Modifier.size(24.dp))
                                Text(stringResource(R.string.historial_loading)) } } }
                    if (estado.hayError) {
                        item(key = "error") {
                            Surface(shape = MaterialTheme.shapes.large, color = colors.surface) {
                                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(stringResource(R.string.historial_error), color = colors.error)
                                    TextButton(onClick = viewModel::reintentar) {
                                        Text(stringResource(R.string.historial_retry)) } } } }
                    } else if (!estado.cargando && estado.eventos.isEmpty()) {
                        item(key = "vacio") {
                            Surface(shape = MaterialTheme.shapes.large, color = colors.surface) {
                                Text(stringResource(if (estado.tieneFiltros) R.string.historial_no_results
                                    else R.string.historial_empty), Modifier.fillMaxWidth().padding(20.dp)) } } }
                    items(estado.eventos, key = { it.eventoId }) { evento ->
                        EventoHistorialCard(evento, onAbrirIngreso) }
                    if (estado.hayMas || (estado.cargando && estado.eventos.isNotEmpty())) {
                        item(key = "mas") {
                            OutlinedButton(onClick = viewModel::cargarMas, enabled = !estado.cargando,
                                modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(if (estado.cargando) R.string.historial_loading else R.string.historial_more)) } } } } } } } }

// Chip de filtro del historial.
@Composable
private fun ChipHistorial(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto, style = MaterialTheme.typography.labelLarge) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary)) }
