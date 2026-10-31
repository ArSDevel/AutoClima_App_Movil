package com.example.myapplication.ui.respaldo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.respaldo.EstadoDeIngreso
import com.example.myapplication.data.respaldo.IngresoComparado
import com.example.myapplication.data.respaldo.Lado
import com.example.myapplication.data.respaldo.ModoCombinacion
import com.example.myapplication.data.respaldo.PlanCombinacion
import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.components.etiquetaEstado
import com.example.myapplication.ui.usuarios.BotonPrincipal

// Pantalla "Esto es lo que pasaría": muestra, antes de tocar nada, qué se agregaría, qué queda igual y qué ingresos están
// distintos en el celular y en el respaldo, y deja elegir de qué lado se queda cada uno.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VistaPreviaCombinacion(
    plan: PlanCombinacion,
    modo: ModoCombinacion,
    ganadores: Map<Long, Lado>,
    onModo: (ModoCombinacion) -> Unit,
    onGanador: (Long, Lado) -> Unit,
    onCombinar: () -> Unit,
    onCancelar: () -> Unit,
    onExportarAntes: (() -> Unit)?) {
    val colors = MaterialTheme.colorScheme
    BackHandler(onBack = onCancelar)
    Scaffold(
        containerColor = colors.background,
        topBar = { EncabezadoAutoClimas(titulo = stringResource(R.string.respaldo_preview_title), onVolver = onCancelar) },
        bottomBar = {
            Surface(color = colors.surface, shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotonPrincipal(texto = stringResource(R.string.respaldo_merge), onClick = onCombinar,
                        habilitado = plan.nuevos.isNotEmpty() || ganadores.values.any { it == Lado.RESPALDO })
                    if (onExportarAntes != null) {
                        OutlinedButton(onClick = onExportarAntes, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.respaldo_export_before)) } }
                    TextButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.usuarios_delete_cancel)) } } } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item(key = "nuevos") {
                    Resumen(
                        titulo = pluralStringResource(R.plurals.respaldo_preview_new, plan.nuevos.size, plan.nuevos.size),
                        detalle = stringResource(R.string.respaldo_preview_new_detail, plan.vehiculosNuevos, plan.clientesNuevos),
                        accion = stringResource(R.string.respaldo_preview_new_action)) }
                item(key = "iguales") {
                    Resumen(
                        titulo = pluralStringResource(R.plurals.respaldo_preview_same, plan.iguales, plan.iguales),
                        detalle = null, accion = stringResource(R.string.respaldo_preview_same_action)) }
                item(key = "distintos") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(pluralStringResource(R.plurals.respaldo_preview_diff, plan.distintos.size, plan.distintos.size),
                            style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                        if (plan.distintos.isNotEmpty()) {
                            Text(stringResource(R.string.respaldo_preview_diff_hint), style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Chip(stringResource(R.string.respaldo_mode_recent), modo == ModoCombinacion.MAS_RECIENTE) {
                                    onModo(ModoCombinacion.MAS_RECIENTE) }
                                Chip(stringResource(R.string.respaldo_mode_phone), modo == ModoCombinacion.SIEMPRE_CELULAR) {
                                    onModo(ModoCombinacion.SIEMPRE_CELULAR) }
                                Chip(stringResource(R.string.respaldo_mode_backup), modo == ModoCombinacion.SIEMPRE_RESPALDO) {
                                    onModo(ModoCombinacion.SIEMPRE_RESPALDO) } } } } }
                items(plan.distintos, key = { it.idRespaldo }) { ingreso ->
                    TarjetaDistinto(ingreso, ganadores[ingreso.idRespaldo] ?: Lado.CELULAR) { onGanador(ingreso.idRespaldo, it) } } } } } }

@Composable
private fun Chip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = seleccionado, onClick = onClick, label = { Text(texto) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.primary, selectedLabelColor = colors.onPrimary)) }

@Composable
private fun Resumen(titulo: String, detalle: String?, accion: String) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = MaterialTheme.shapes.large, color = colors.surface, shadowElevation = 1.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            if (detalle != null) Text(detalle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Text(accion, style = MaterialTheme.typography.bodyMedium, color = colors.primary) } } }

// Un ingreso que está en los dos lados con diferencias: se ve qué tiene cada uno y se elige cuál se queda.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaDistinto(ingreso: IngresoComparado, ganador: Lado, onGanador: (Lado) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = MaterialTheme.shapes.large, color = colors.surface, shadowElevation = 1.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(ingreso.placa, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            ingreso.celular?.let { Text(stringResource(R.string.respaldo_side_phone, descripcion(it)), style = MaterialTheme.typography.bodyMedium) }
            Text(stringResource(R.string.respaldo_side_backup, descripcion(ingreso.respaldo)), style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Chip(stringResource(R.string.respaldo_keep_phone), ganador == Lado.CELULAR) { onGanador(Lado.CELULAR) }
                Chip(stringResource(R.string.respaldo_use_backup), ganador == Lado.RESPALDO) { onGanador(Lado.RESPALDO) } } } } }

@Composable
private fun descripcion(lado: EstadoDeIngreso): String {
    val estado = stringResource(etiquetaEstado(EstadoIngreso.desdeValor(lado.estado)))
    val fotos = pluralStringResource(R.plurals.respaldo_photos, lado.fotos, lado.fotos)
    val firma = stringResource(if (lado.tieneFirma) R.string.respaldo_with_signature else R.string.respaldo_without_signature)
    return stringResource(R.string.respaldo_side_line, estado, fechaCorta(lado.ultimoMovimiento), fotos, firma) }
