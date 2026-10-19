package com.example.myapplication.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import com.example.myapplication.data.repository.ChecklistRepository
import androidx.compose.foundation.layout.fillMaxWidth
import com.example.myapplication.data.local.repository.DiagnosticoRepository
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.ui.components.BarraAccionInferior
import com.example.myapplication.ui.components.BotonCabecera
import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.components.PildoraEstado
import com.example.myapplication.ui.components.colorEstado
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.data.local.model.ReglasIngreso
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.estadoIngresoColors
import java.text.DateFormat
import java.util.Date

// Conecta la pantalla con el ViewModel.
// tecnicoId debe proceder de la sesión autenticada.
@Composable
fun DetalleIngresoScreen(
    viewModel: DetalleIngresoViewModel,
    tecnicoId: Long,
    onVolver: () -> Unit,
    onEditar: (Long) -> Unit,
    onAbrirChecklist: (Long) -> Unit,
    onAbrirDiagnostico: (Long) -> Unit,
    onAbrirEvidencias: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetalleIngresoContent(
        uiState = uiState,
        onVolver = onVolver,
        onEditar = onEditar,
        onReintentar = viewModel::reintentar,
        onLimpiarError = viewModel::limpiarErrorOperacion,
        onConsumirExito = viewModel::consumirOperacionExitosa,
        onAbrirDiagnostico = onAbrirDiagnostico,
        onAbrirChecklist = onAbrirChecklist,
        onAbrirEvidencias = onAbrirEvidencias,
        onConfirmar = { accion, motivo, destino ->
            when (accion) {
                IngresoAccion.INICIAR_REVISION -> viewModel.iniciarRevision(tecnicoId)
                IngresoAccion.INICIAR_REPARACION -> viewModel.iniciarReparacion(motivo, tecnicoId)
                IngresoAccion.ENVIAR_A_FIRMA -> viewModel.enviarAFirma(motivo, tecnicoId)
                IngresoAccion.DEVOLVER_A_REPARACION -> viewModel.devolverAReparacion(motivo, tecnicoId)
                IngresoAccion.CANCELAR -> viewModel.cancelar(motivo, tecnicoId)
                IngresoAccion.REGULARIZAR_ESTADO ->
                    viewModel.regularizarEstado(destino = destino, motivo = motivo, tecnicoId = tecnicoId)
                IngresoAccion.ELIMINAR -> viewModel.eliminar(tecnicoId)
                else -> error("La acción no corresponde a una confirmación del detalle.")
            }
        }
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
private fun DetalleIngresoContent(
    uiState: DetalleIngresoUiState,
    onVolver: () -> Unit,
    onEditar: (Long) -> Unit,
    onReintentar: () -> Unit,
    onLimpiarError: () -> Unit,
    onConsumirExito: () -> Unit,
    onConfirmar: (IngresoAccion, String, EstadoIngreso) -> Unit,
    onAbrirChecklist: (Long) -> Unit,
    onAbrirDiagnostico: (Long) -> Unit,
    onAbrirEvidencias: (Long) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val ingreso = uiState.ingreso
    var menuAbierto by rememberSaveable { mutableStateOf(false) }

    var accionPendiente by rememberSaveable { mutableStateOf<IngresoAccion?>(null) }
    var motivo by rememberSaveable { mutableStateOf("") }
    var destinoRegularizacion by rememberSaveable { mutableStateOf(EstadoIngreso.REGISTRADO) }

    val accionesHabilitadas = !uiState.cargando && !uiState.procesando && !uiState.operacionExitosa &&
            !uiState.eliminado && uiState.errorCarga == null && ingreso != null

    fun solicitarAccion(accion: IngresoAccion) {
        if (!accionesHabilitadas) return
        onLimpiarError()
        motivo = ""
        destinoRegularizacion = EstadoIngreso.REGISTRADO
        accionPendiente = accion
    }

    fun cerrarConfirmacion() {
        if (uiState.procesando) return
        accionPendiente = null
        motivo = ""
        onLimpiarError()
    }

    // Evita abandonar la pantalla mientras se guarda una operación.
    BackHandler(enabled = uiState.procesando) {
        // Esperar a que finalice la operación.
    }

    LaunchedEffect(uiState.operacionExitosa, uiState.eliminado, uiState.procesando) {
        if (!uiState.procesando) {
            if (uiState.eliminado) { onVolver() } else if (uiState.operacionExitosa) {
                accionPendiente = null
                motivo = ""
                onConsumirExito()
            }
        }
    }

    val estadoActual = ingreso?.let { EstadoIngreso.desdeValor(it.estado) }
    val puedeEditar = estadoActual != null && ReglasIngreso.editar(estadoActual)
    val puedeDevolver = estadoActual != null && ReglasIngreso.devolverAReparacion(estadoActual)
    val puedeCancelar = estadoActual != null && ReglasIngreso.cancelar(estadoActual)
    val puedeEliminar = estadoActual != null && ReglasIngreso.eliminar(
        estado = estadoActual, tieneActividadPosterior = uiState.tieneActividadPosterior)
    val puedeRegularizar = estadoActual != null && ReglasIngreso.regularizarEstado(estadoActual)
    val puedeIniciarRevision = estadoActual != null && ReglasIngreso.iniciarRevision(estadoActual)
    val tieneChecklist = uiState.historial.any { it.accion == "CHECKLIST" }
    val tieneDiagnostico = uiState.historial.any { it.accion == "DIAGNOSTICO" }
    // Una sola acción principal según el estado actual.
    val accionPrincipal: IngresoAccion? = when {
        estadoActual == null -> null
        puedeIniciarRevision -> IngresoAccion.INICIAR_REVISION
        ReglasIngreso.iniciarReparacion(estadoActual) -> IngresoAccion.INICIAR_REPARACION
        ReglasIngreso.enviarAFirma(estadoActual) -> IngresoAccion.ENVIAR_A_FIRMA
        // Firma y entrega las desarrollará otro equipo.
        estadoActual == EstadoIngreso.LISTO_PARA_FIRMA -> IngresoAccion.FIRMAR
        estadoActual == EstadoIngreso.LISTO_PARA_ENTREGA -> IngresoAccion.ENTREGAR
        puedeRegularizar -> IngresoAccion.REGULARIZAR_ESTADO
        else -> null
    }
    val requisitoFaltante: Int? = when {
        accionPrincipal == IngresoAccion.INICIAR_REPARACION && !tieneChecklist -> R.string.detalle_missing_checklist
        accionPrincipal == IngresoAccion.ENVIAR_A_FIRMA && !tieneDiagnostico -> R.string.detalle_missing_diagnosis
        accionPrincipal == IngresoAccion.FIRMAR || accionPrincipal == IngresoAccion.ENTREGAR -> R.string.in_development
        else -> null
    }
    val hayMasOpciones = puedeEditar || puedeDevolver || puedeCancelar || puedeEliminar

    Scaffold(
        containerColor = colors.background,
        topBar = {
            EncabezadoAutoClimas(
                titulo = ingreso?.placa ?: stringResource(R.string.detalle_title),
                subtitulo = ingreso?.let { datos ->
                    listOfNotNull(datos.modelo.takeIf { it.isNotBlank() },
                        datos.color?.takeIf { it.isNotBlank() }).joinToString(" · ")
                },
                onVolver = onVolver,
                habilitado = !uiState.procesando,
                acciones = {
                    if (ingreso != null && hayMasOpciones) {
                        Box {
                            BotonCabecera(
                                icono = Icons.Default.MoreVert,
                                descripcion = stringResource(R.string.detalle_more_options),
                                habilitado = accionesHabilitadas,
                                onClick = { menuAbierto = true }
                            )
                            DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                                if (puedeEditar) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(if (estadoActual != null &&
                                            ReglasIngreso.soloContacto(estadoActual)) {
                                            R.string.detalle_edit_contact } else { R.string.dashboard_action_edit })) },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                        onClick = { menuAbierto = false; onEditar(ingreso.ingresoId) }
                                    )
                                }
                                if (puedeDevolver) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.dashboard_action_return_repair)) },
                                        leadingIcon = { Icon(Icons.Default.Undo, contentDescription = null) },
                                        onClick = { menuAbierto = false; solicitarAccion(IngresoAccion.DEVOLVER_A_REPARACION) }
                                    )
                                }
                                if (puedeCancelar) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.dashboard_action_cancel), color = colors.error) },
                                        leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = colors.error) },
                                        onClick = { menuAbierto = false; solicitarAccion(IngresoAccion.CANCELAR) }
                                    )
                                }
                                if (puedeEliminar) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.dashboard_action_delete), color = colors.error) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = colors.error) },
                                        onClick = { menuAbierto = false; solicitarAccion(IngresoAccion.ELIMINAR) }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (ingreso != null && accionPrincipal != null) {
                BarraAccionInferior {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (requisitoFaltante != null) {
                            Text(
                                text = stringResource(requisitoFaltante),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                solicitarAccion(accionPrincipal)
                            },
                            enabled = accionesHabilitadas && requisitoFaltante == null,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = stringResource(when (accionPrincipal) {
                                    IngresoAccion.INICIAR_REVISION -> R.string.dashboard_action_start_review
                                    IngresoAccion.INICIAR_REPARACION -> R.string.dashboard_action_start_repair
                                    IngresoAccion.ENVIAR_A_FIRMA -> R.string.dashboard_action_to_signature
                                    IngresoAccion.FIRMAR -> R.string.dashboard_action_sign
                                    IngresoAccion.ENTREGAR -> R.string.dashboard_action_deliver
                                    else -> R.string.dashboard_action_regularize
                                }),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                uiState.cargando || uiState.eliminado -> {
                    item { PanelDetalle(titulo = stringResource(R.string.detalle_loading)) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) } }
                }

                uiState.errorCarga != null -> {
                    item { PanelDetalle(titulo = stringResource(R.string.detalle_load_error)) {
                        Text(uiState.errorCarga)
                        Button(onClick = onReintentar) { Text(stringResource(R.string.dashboard_retry)) } } }
                }

                ingreso == null -> {
                    item {
                        if (uiState.procesando) { PanelDetalle(titulo = stringResource(R.string.detalle_processing)) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        } else { PanelDetalle(titulo = stringResource(R.string.navigation_ingreso_not_found)) {
                            TextButton(onClick = onVolver) {
                                Text(stringResource(R.string.registro_back_dashboard))
                            }
                        }
                        }
                    }
                }
                else -> {
                    val estado = EstadoIngreso.desdeValor(ingreso.estado)
                    if (uiState.procesando) {
                        item {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                    if (accionPendiente == null && uiState.errorOperacion != null) {
                        item { Text(text = uiState.errorOperacion, color = colors.error) }
                    }
                    item { PasosIngreso(estado = estado) }
                    item {
                        // Permite capturar durante revisión y reparación.
                        // En otras etapas permite consultar un checklist ya guardado.
                        val permiteEditarChecklist = ChecklistRepository.puedeEditar(estado)
                        val tieneChecklistGuardado = uiState.historial.any { it.accion == "CHECKLIST" }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                HerramientaIngreso(
                                    titulo = stringResource(R.string.dashboard_action_checklist),
                                    icono = Icons.Default.Checklist,
                                    color = colors.primary,
                                    habilitada = accionesHabilitadas && (permiteEditarChecklist || tieneChecklistGuardado),
                                    onClick = { onAbrirChecklist(ingreso.ingresoId) },
                                    modifier = Modifier.weight(1f)
                                )
                                HerramientaIngreso(
                                    titulo = stringResource(R.string.dashboard_action_evidence),
                                    icono = Icons.Default.CameraAlt,
                                    color = MaterialTheme.estadoIngresoColors.listoParaEntrega,
                                    habilitada = accionesHabilitadas,
                                    onClick = { onAbrirEvidencias(ingreso.ingresoId) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                HerramientaIngreso(
                                    titulo = stringResource(R.string.dashboard_action_diagnosis),
                                    icono = Icons.Default.Description,
                                    color = MaterialTheme.estadoIngresoColors.enReparacion,
                                    habilitada = accionesHabilitadas,
                                    onClick = { onAbrirDiagnostico(ingreso.ingresoId) },
                                    modifier = Modifier.weight(1f)
                                )
                                // La firma se consulta desde que el ingreso queda listo para firma.
                                HerramientaIngreso(
                                    titulo = stringResource(R.string.dashboard_action_sign),
                                    icono = Icons.Default.Draw,
                                    color = MaterialTheme.estadoIngresoColors.listoParaFirma,
                                    habilitada = false,
                                    nota = stringResource(R.string.in_development),
                                    onClick = {},
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = colors.surface,
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                FilaDato(Icons.Default.Person, ingreso.nombreCliente, conDivisor = false)
                                FilaDato(Icons.Default.Phone, ingreso.telefonoCliente)
                                FilaDato(Icons.Default.Email, ingreso.emailCliente)
                                FilaDato(Icons.Default.DateRange, fechaDetalle(ingreso.fechaEntrada))
                                FilaDato(Icons.Default.Description, ingreso.motivoIngreso)
                                FilaDato(Icons.Default.Info, ingreso.condicionInicial)
                                FilaDato(Icons.Default.Tag, ingreso.numeroSerie)
                            }
                        }
                    }
                    if (ReglasIngreso.estaCerrado(estado)) {
                        item {
                            Text(text = stringResource(R.string.detalle_closed),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant)
                        }
                    }
                    item {
                        PanelDetalle(titulo = stringResource(R.string.detalle_history)) {
                            if (uiState.historial.isEmpty()) {
                                Text(stringResource(R.string.detalle_history_empty))
                            }
                            uiState.historial.forEachIndexed { indice, evento ->
                                EventoDetalle(evento = evento, ultimo = indice == uiState.historial.lastIndex)
                            }
                        }
                    }
                }
            }
        }
    }

    val accion = accionPendiente
    if (accion != null && ingreso != null && !uiState.eliminado) {
        val requiereMotivo = accion != IngresoAccion.ELIMINAR && accion != IngresoAccion.INICIAR_REVISION
        if (accion == IngresoAccion.ELIMINAR) {
            // Eliminar es un cuadro con aviso, no una hoja.
            AlertDialog(
                onDismissRequest = { cerrarConfirmacion() },
                icon = {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                            .background(colors.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null,
                            tint = colors.error, modifier = Modifier.size(28.dp))
                    }
                },
                title = { Text(stringResource(R.string.detalle_delete_title, ingreso.placa)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = stringResource(R.string.detalle_delete_warning))
                        uiState.errorOperacion?.let { Text(text = it, color = colors.error) }
                        if (uiState.procesando) { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = accionesHabilitadas,
                        onClick = { onConfirmar(accion, "", destinoRegularizacion) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.error, contentColor = colors.onError)
                    ) { Text(stringResource(R.string.dashboard_action_delete)) }
                },
                dismissButton = {
                    TextButton(enabled = !uiState.procesando, onClick = { cerrarConfirmacion() }) {
                        Text(stringResource(R.string.detalle_back))
                    }
                }
            )
        } else {
            ModalBottomSheet(
                onDismissRequest = { cerrarConfirmacion() },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(tituloConfirmacion(accion)),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        PildoraEstado(estado = EstadoIngreso.desdeValor(ingreso.estado))
                    }
                    if (accion == IngresoAccion.REGULARIZAR_ESTADO) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(EstadoIngreso.REGISTRADO, EstadoIngreso.EN_REVISION).forEach { destino ->
                                FilterChip(selected = destinoRegularizacion == destino,
                                    enabled = !uiState.procesando,
                                    onClick = { destinoRegularizacion = destino
                                        onLimpiarError() },
                                    label = { Text(stringResource(etiquetaEstadoDetalle(destino)))}
                                )
                            }
                        }
                    }
                    if (requiereMotivo) {
                        OutlinedTextField(
                            value = motivo,
                            onValueChange = {
                                motivo = it
                                onLimpiarError()
                            },
                            label = { Text(stringResource(R.string.detalle_reason)) },
                            enabled = !uiState.procesando,
                            minLines = 3,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        text = stringResource(
                            if (accion == IngresoAccion.CANCELAR) R.string.detalle_cancel_warning
                            else R.string.detalle_change_warning
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    uiState.errorOperacion?.let { Text(text = it, color = colors.error) }
                    if (uiState.procesando) { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            enabled = !uiState.procesando,
                            onClick = { cerrarConfirmacion() },
                            modifier = Modifier.heightIn(min = 56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text(stringResource(R.string.detalle_back)) }
                        Button(
                            enabled = accionesHabilitadas && (!requiereMotivo || motivo.isNotBlank()),
                            onClick = { onConfirmar(accion, motivo.trim(), destinoRegularizacion) },
                            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = if (accion == IngresoAccion.CANCELAR) ButtonDefaults.buttonColors(
                                containerColor = colors.error, contentColor = colors.onError
                            ) else ButtonDefaults.buttonColors()
                        ) {
                            Text(text = stringResource(R.string.detalle_confirm),
                                style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelDetalle(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
        Column(modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}

@StringRes
private fun etiquetaEstadoDetalle(estado: EstadoIngreso): Int {
    return when (estado) {
        EstadoIngreso.REGISTRADO -> R.string.dashboard_status_registered
        EstadoIngreso.EN_REVISION -> R.string.dashboard_status_review
        EstadoIngreso.EN_REPARACION -> R.string.dashboard_status_repair
        EstadoIngreso.LISTO_PARA_FIRMA -> R.string.dashboard_status_signature
        EstadoIngreso.LISTO_PARA_ENTREGA -> R.string.dashboard_status_delivery
        EstadoIngreso.ENTREGADO -> R.string.dashboard_status_delivered
        EstadoIngreso.CANCELADO -> R.string.dashboard_status_cancelled
        EstadoIngreso.NO_DISPONIBLE -> R.string.dashboard_status_unknown
    }
}

@StringRes
private fun tituloConfirmacion(accion: IngresoAccion): Int {
    return when (accion) {
        IngresoAccion.INICIAR_REVISION -> R.string.dashboard_action_start_review
        IngresoAccion.INICIAR_REPARACION -> R.string.dashboard_action_start_repair
        IngresoAccion.ENVIAR_A_FIRMA -> R.string.dashboard_action_to_signature
        IngresoAccion.DEVOLVER_A_REPARACION -> R.string.dashboard_action_return_repair
        IngresoAccion.CANCELAR -> R.string.dashboard_action_cancel
        IngresoAccion.ELIMINAR -> R.string.dashboard_action_delete
        IngresoAccion.REGULARIZAR_ESTADO -> R.string.dashboard_action_regularize
        else -> error("Acción sin diálogo de confirmación.")
    }
}

@Composable
private fun fechaDetalle(milisegundos: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
        DateFormat.SHORT, locale
    ).format(Date(milisegundos))
}

@Composable
private fun EventoDetalle(evento: EventoIngreso, ultimo: Boolean) {
    val colors = MaterialTheme.colorScheme
    val titulo = stringResource(
        when (evento.accion) {
            "CREADO" -> R.string.detalle_event_created
            "EDITADO" -> R.string.detalle_event_edited
            "ESTADO" -> R.string.detalle_event_state
            "CHECKLIST" -> R.string.detalle_event_checklist
            "EVIDENCIA" -> R.string.fotos_event_added
            "FOTO_PRINCIPAL" -> R.string.fotos_event_main
            "EVIDENCIA_ELIMINADA" -> R.string.fotos_event_deleted
            "DIAGNOSTICO" -> R.string.detalle_event_diagnosis
            else -> R.string.detalle_event_other
        }
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.padding(top = 6.dp).size(12.dp).clip(CircleShape).background(colors.primary))
            if (!ultimo) {
                Box(modifier = Modifier.padding(top = 4.dp).width(2.dp).height(40.dp)
                    .background(colors.outlineVariant))
            }
        }
        Column(modifier = Modifier.weight(1f).padding(bottom = if (ultimo) 0.dp else 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.titleSmall)
            Text(text = fechaDetalle(evento.fecha) + " · " +
                    stringResource(R.string.detalle_event_author, evento.tecnicoId),
                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            if (evento.accion == "ESTADO") {
                Text(text = stringResource(
                    R.string.detalle_event_transition, stringResource(etiquetaEstadoDetalle(
                        EstadoIngreso.desdeValor(evento.estadoAnterior))),
                    stringResource(etiquetaEstadoDetalle(EstadoIngreso.desdeValor(evento.estadoNuevo)))
                ), style = MaterialTheme.typography.bodyMedium)
            }
            if (evento.motivo.isNotBlank()) {
                Text(text = evento.motivo, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// Paso actual del ingreso dentro del proceso del taller.
@Composable
private fun PasosIngreso(estado: EstadoIngreso) {
    val colors = MaterialTheme.colorScheme
    // CANCELADO y NO_DISPONIBLE no forman parte del avance normal.
    if (estado == EstadoIngreso.CANCELADO || estado == EstadoIngreso.NO_DISPONIBLE) {
        PildoraEstado(estado = estado)
        return
    }
    val actual = when (estado) {
        EstadoIngreso.REGISTRADO -> 0
        EstadoIngreso.EN_REVISION -> 1
        EstadoIngreso.EN_REPARACION -> 2
        EstadoIngreso.LISTO_PARA_FIRMA -> 3
        EstadoIngreso.LISTO_PARA_ENTREGA -> 4
        else -> 5
    }
    val pasos = listOf(
        R.string.detalle_step_registered,
        R.string.detalle_step_review,
        R.string.detalle_step_repair,
        R.string.detalle_step_signature,
        R.string.detalle_step_delivery
    )
    val colorActual = colorEstado(estado)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp)) {
            pasos.forEachIndexed { indice, texto ->
                val hecho = indice < actual
                val enCurso = indice == actual
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier.size(26.dp).clip(CircleShape)
                            .background(when { hecho -> colors.primary; enCurso -> colors.surface
                                else -> colors.surfaceVariant })
                            .then(if (enCurso) Modifier.border(3.dp, colorActual, CircleShape) else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hecho) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null,
                                modifier = Modifier.size(16.dp), tint = colors.onPrimary)
                        }
                    }
                    // El tamaño no crece con la letra grande del sistema: son cinco etiquetas en una fila.
                    Text(
                        text = stringResource(texto),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = with(androidx.compose.ui.platform.LocalDensity.current) { 11.dp.toSp() }),
                        fontWeight = if (enCurso) FontWeight.Bold else FontWeight.Normal,
                        color = when { enCurso -> colorActual; hecho -> colors.primary
                            else -> colors.onSurfaceVariant },
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// Acceso a un módulo del ingreso.
@Composable
private fun HerramientaIngreso(
    titulo: String,
    icono: ImageVector,
    color: Color,
    habilitada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    nota: String? = null
) {
    Surface(
        onClick = onClick,
        enabled = habilitada,
        modifier = modifier.alpha(if (habilitada) 1f else 0.5f),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(MaterialTheme.shapes.small)
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icono, contentDescription = null, tint = color)
            }
            Column {
                Text(text = titulo, style = MaterialTheme.typography.titleMedium)
                if (nota != null) {
                    Text(text = nota, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// Dato con icono. No se muestra si no tiene valor.
@Composable
private fun FilaDato(icono: ImageVector, valor: String?, conDivisor: Boolean = true) {
    if (valor.isNullOrBlank()) return
    if (conDivisor) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icono, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(
    name = "Detalle · claro",
    showBackground = true,
    widthDp = 400,
    heightDp = 850
)
@Composable
private fun DetalleClaroPreview() {
    MyApplicationTheme(darkTheme = false) {
        DetalleIngresoContent(
            uiState = estadoDetallePreview(),
            onVolver = {},
            onEditar = {},
            onAbrirDiagnostico = {},
            onReintentar = {},
            onLimpiarError = {},
            onConsumirExito = {},
            onAbrirChecklist = {},
            onConfirmar = { _, _, _ -> }
        )
    }
}

@Preview(
    name = "Detalle · oscuro",
    showBackground = true,
    widthDp = 400,
    heightDp = 850
)
@Composable
private fun DetalleOscuroPreview() {
    MyApplicationTheme(darkTheme = true) {
        DetalleIngresoContent(
            uiState = estadoDetallePreview(),
            onVolver = {},
            onEditar = {},
            onReintentar = {},
            onLimpiarError = {},
            onAbrirDiagnostico = {},
            onConsumirExito = {},
            onAbrirChecklist = {},
            onConfirmar = { _, _, _ -> }
        )
    }
}

private fun estadoDetallePreview(): DetalleIngresoUiState {
    return DetalleIngresoUiState(
        cargando = false,
        ingreso = IngresoResumen(
            ingresoId = 1L,
            placa = "ABC-123",
            modelo = "Honda Civic",
            color = "Rojo",
            numeroSerie = "SERIE-DE-EJEMPLO",
            nombreCliente = "Cliente de ejemplo",
            telefonoCliente = "8100000000",
            emailCliente = null,
            fechaEntrada = 1788955200000L,
            estado = EstadoIngreso.REGISTRADO.name,
            motivoIngreso = "El aire acondicionado no enfría.",
            condicionInicial = "Vehículo recibido para revisión."
        ),
        historial = listOf(
            EventoIngreso(
                eventoId = 1L,
                ingresoId = 1L,
                fecha = 1788955200000L,
                tecnicoId = 1L,
                accion = "CREADO",
                estadoAnterior = "",
                estadoNuevo = EstadoIngreso.REGISTRADO.name,
                motivo = "Ingreso registrado."
            )
        )
    )
}