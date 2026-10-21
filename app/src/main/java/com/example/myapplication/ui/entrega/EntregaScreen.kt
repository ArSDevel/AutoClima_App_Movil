package com.example.myapplication.ui.entrega

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.ui.components.BarraAccionInferior
import com.example.myapplication.ui.components.DialogoAviso
import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.components.PildoraEstado
import com.example.myapplication.ui.components.TipoAviso
import com.example.myapplication.ui.theme.estadoIngresoColors

// Entrega del vehículo al cliente. Resume lo que ya está completo y deja una nota opcional.
@Composable
fun EntregaScreen(
    viewModel: EntregaViewModel,
    tecnicoId: Long,
    onVolver: () -> Unit,
    onIrAIngresos: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val yaEntregado = estado.estadoActual == EstadoIngreso.ENTREGADO
    BackHandler(enabled = estado.guardando) { }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            EncabezadoAutoClimas(
                titulo = stringResource(R.string.entrega_title),
                subtitulo = estado.ingreso?.let { listOf(it.placa, it.modelo).filter { t -> t.isNotBlank() }.joinToString(" · ") },
                onVolver = onVolver,
                habilitado = !estado.guardando,
                acciones = { estado.estadoActual?.let { PildoraEstado(estado = it, sobreCabecera = true) } }) },
        bottomBar = {
            if (!estado.cargando && estado.errorCarga == null && !yaEntregado) {
                BarraAccionInferior {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!estado.puedeEntregar && !estado.guardando) {
                            Text(stringResource(R.string.entrega_not_ready), style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant) }
                        Button(
                            onClick = { viewModel.confirmar(tecnicoId) },
                            enabled = estado.puedeEntregar,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.estadoIngresoColors.listoParaFirma)
                        ) {
                            if (estado.guardando) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                                    color = colors.onPrimary)
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(22.dp)) }
                            Text("  " + stringResource(R.string.entrega_confirm), style = MaterialTheme.typography.titleMedium) } } } } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            when {
                estado.cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                estado.errorCarga != null -> Text(estado.errorCarga!!, color = colors.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp), textAlign = TextAlign.Center)
                else -> Column(
                    modifier = Modifier.widthIn(max = 640.dp).fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(shape = MaterialTheme.shapes.large, color = colors.surface, shadowElevation = 1.dp) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            FilaRequisito(stringResource(R.string.entrega_checklist), estado.requisitos.checklist)
                            HorizontalDivider(color = colors.outlineVariant)
                            FilaRequisito(stringResource(R.string.entrega_diagnosis), estado.requisitos.diagnostico)
                            HorizontalDivider(color = colors.outlineVariant)
                            FilaRequisito(stringResource(R.string.entrega_signature), estado.requisitos.firma) } }
                    if (yaEntregado) {
                        Text(stringResource(R.string.entrega_already), style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant)
                    } else {
                        OutlinedTextField(
                            value = estado.nota,
                            onValueChange = viewModel::cambiarNota,
                            label = { Text(stringResource(R.string.entrega_note)) },
                            placeholder = { Text(stringResource(R.string.entrega_note_hint)) },
                            supportingText = { Text(stringResource(R.string.registro_optional)) },
                            enabled = !estado.guardando,
                            minLines = 3,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(16.dp))
                            Text(stringResource(R.string.entrega_saved_note), style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant) } }
                    estado.error?.let { Text(it, color = colors.error, style = MaterialTheme.typography.bodyMedium) } } } } }

    // Se muestra cuando la entrega queda registrada.
    if (estado.entregado && !estado.guardando) {
        DialogoAviso(
            tipo = TipoAviso.EXITO,
            titulo = stringResource(R.string.entrega_done_title),
            mensaje = stringResource(R.string.entrega_done_message),
            textoSecundario = stringResource(R.string.entrega_to_detail),
            onSecundario = { viewModel.consumirExito()
                onVolver() },
            textoPrincipal = stringResource(R.string.entrega_to_list),
            onPrincipal = { viewModel.consumirExito()
                onIrAIngresos() },
            onDismiss = viewModel::consumirExito) } }

// Paso previo a la entrega con una marca verde si está completo o un aviso si falta.
@Composable
private fun FilaRequisito(texto: String, completo: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (completo) Icons.Default.Check else Icons.Default.Warning,
            contentDescription = null,
            tint = if (completo) MaterialTheme.estadoIngresoColors.listoParaFirma else colors.error)
        Text(texto, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (!completo) {
            Text(stringResource(R.string.entrega_missing), style = MaterialTheme.typography.bodySmall, color = colors.error) } } }
