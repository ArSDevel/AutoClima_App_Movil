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
import androidx.compose.foundation.layout.fillMaxWidth
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
    onEditar: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetalleIngresoContent(
        uiState = uiState,
        onVolver = onVolver,
        onEditar = onEditar,
        onReintentar = viewModel::reintentar,
        onLimpiarError = viewModel::limpiarErrorOperacion,
        onConsumirExito = viewModel::consumirOperacionExitosa,
        onConfirmar = { accion, motivo, destino ->
            when (accion) {
                IngresoAccion.INICIAR_REVISION -> viewModel.iniciarRevision(motivo, tecnicoId)
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
    onConfirmar: (IngresoAccion, String, EstadoIngreso) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val ingreso = uiState.ingreso

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.detalle_title),
                        style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = { IconButton(enabled = !uiState.procesando, onClick = onVolver) {
                        Icon(imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.detalle_back))
                    }
                },
                actions = { Image(painter = painterResource(R.drawable.logo_autoclimas),
                        contentDescription = stringResource(R.string.dashboard_logo_description),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(end = 12.dp).width(72.dp).height(56.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.tertiary,
                    titleContentColor = colors.onTertiary,
                    navigationIconContentColor = colors.onTertiary
                )
            )
        }
    ) { padding ->
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .background(AutoClimasGradients.loginBackground)
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                item { PanelDetalle(titulo = ingreso.placa) {
                        Text(text = ingreso.modelo, style = MaterialTheme.typography.titleMedium)
                        EstadoDetalle(estado)
                        Text(text = stringResource(R.string.registro_saved_identifier,
                            ingreso.ingresoId),
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
                item { PanelDetalle(titulo = stringResource(R.string.detalle_vehicle)) {
                        DatoDetalle(etiqueta = R.string.dashboard_color, valor = ingreso.color)
                        DatoDetalle(etiqueta = R.string.dashboard_serial_number, valor = ingreso.numeroSerie)
                    }
                }
                item { PanelDetalle(titulo = stringResource(R.string.detalle_customer)) {
                        DatoDetalle(etiqueta = R.string.dashboard_customer, valor = ingreso.nombreCliente)
                        DatoDetalle(etiqueta = R.string.dashboard_phone, valor = ingreso.telefonoCliente)
                        DatoDetalle(etiqueta = R.string.dashboard_email, valor = ingreso.emailCliente)
                    }
                }

                item { PanelDetalle(titulo = stringResource(R.string.detalle_reception)) {
                        DatoDetalle(etiqueta = R.string.dashboard_entry_date, valor = fechaDetalle(ingreso.fechaEntrada))
                        DatoDetalle(etiqueta = R.string.registro_reason, valor = ingreso.motivoIngreso)
                        DatoDetalle(etiqueta = R.string.registro_initial_condition, valor = ingreso.condicionInicial)
                    }
                }
                item { PanelDetalle(titulo = stringResource(R.string.detalle_actions)) {
                        if (uiState.procesando) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text(stringResource(R.string.detalle_processing))
                        }
                        if (accionPendiente == null && uiState.errorOperacion != null) {
                            Text(text = uiState.errorOperacion, color = colors.error)
                        }
                        if (ReglasIngreso.editar(estado)) {
                            Button(enabled = accionesHabilitadas, onClick = { onEditar(ingreso.ingresoId) }
                            ) { Text(stringResource(if (ReglasIngreso.soloContacto(estado)) { R.string.detalle_edit_contact
                            } else { R.string.dashboard_action_edit })) }
                        }
                        if (ReglasIngreso.iniciarRevision(estado)) {
                            BotonDetalle(texto = R.string.dashboard_action_start_review,
                                habilitado = accionesHabilitadas,
                                onClick = { solicitarAccion(IngresoAccion.INICIAR_REVISION) }
                            )
                        }
                        if (ReglasIngreso.devolverAReparacion(estado)) {
                            BotonDetalle(
                                texto = R.string.dashboard_action_return_repair,
                                habilitado = accionesHabilitadas,
                                onClick = { solicitarAccion(IngresoAccion.DEVOLVER_A_REPARACION)
                                }
                            )
                        }
                        if (ReglasIngreso.regularizarEstado(estado)) {
                            Text(stringResource(R.string.detalle_unknown_help))
                            BotonDetalle(texto = R.string.dashboard_action_regularize,
                                habilitado = accionesHabilitadas,
                                onClick = { solicitarAccion(IngresoAccion.REGULARIZAR_ESTADO) }
                            )
                        }
                        if (ReglasIngreso.cancelar(estado)) {
                            BotonDetalle(texto = R.string.dashboard_action_cancel,
                                habilitado = accionesHabilitadas,
                                destructiva = true,
                                onClick = { solicitarAccion(IngresoAccion.CANCELAR) }
                            )
                        }
                        if (ReglasIngreso.eliminar(estado = estado,
                                tieneActividadPosterior = uiState.tieneActividadPosterior) ){
                            BotonDetalle(texto = R.string.dashboard_action_delete,
                                habilitado = accionesHabilitadas,
                                destructiva = true,
                                onClick = { solicitarAccion(IngresoAccion.ELIMINAR) }
                            )
                        }

                        if (ReglasIngreso.estaCerrado(estado)) {
                            Text(stringResource(R.string.detalle_closed))
                        }
                        ModulosPendientesDetalle(estado)
                    }
                }

                item {
                    PanelDetalle(titulo = stringResource(R.string.detalle_history)) {
                        Text(text = stringResource(R.string.detalle_history_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                        if (uiState.historial.isEmpty()) {
                            Text(stringResource(R.string.detalle_history_empty))
                        }
                    }
                }
                items(items = uiState.historial, key = { "evento-${it.eventoId}" }) { evento -> EventoDetalle(evento) }
            }
        }
    }
}

val accion = accionPendiente
if (accion != null && ingreso != null && !uiState.eliminado) {
    val esEliminar = accion == IngresoAccion.ELIMINAR
    AlertDialog(onDismissRequest = { cerrarConfirmacion() },
        title = { Text(stringResource(tituloConfirmacion(accion))) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(R.string.detalle_confirmation_target,
                    ingreso.ingresoId, ingreso.placa))
                Text(text = stringResource(
                        when (accion) {
                            IngresoAccion.ELIMINAR -> R.string.detalle_delete_warning
                            IngresoAccion.CANCELAR -> R.string.detalle_cancel_warning
                            else -> R.string.detalle_change_warning
                        }
                    )
                )
                if (accion == IngresoAccion.REGULARIZAR_ESTADO) {
                    Text(stringResource(R.string.detalle_select_state))
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
                if (!esEliminar) {
                    OutlinedTextField(value = motivo,
                        onValueChange = { motivo = it
                            onLimpiarError()
                        },
                        label = { Text(stringResource(R.string.detalle_reason)) },
                        enabled = !uiState.procesando,
                        minLines = 2, modifier = Modifier.fillMaxWidth())
                }
                uiState.errorOperacion?.let {
                    Text(text = it, color = colors.error)
                }
                if (uiState.procesando) { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            TextButton(enabled = accionesHabilitadas && (esEliminar || motivo.isNotBlank()),
                onClick = { onConfirmar(accion,
                        motivo.trim(),
                        destinoRegularizacion) }
            ) { Text(stringResource(R.string.detalle_confirm)) } },
        dismissButton = {
            TextButton(enabled = !uiState.procesando, onClick = { cerrarConfirmacion() }) {
                Text(stringResource(R.string.detalle_back)) }
        }
    )
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
            Text(text = titulo, style = MaterialTheme.typography.titleLarge)
            contenido()
        }
    }
}

@Composable
private fun DatoDetalle(
    @StringRes etiqueta: Int,
    valor: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = stringResource(etiqueta),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = valor?.takeIf { it.isNotBlank() } ?:
        stringResource(R.string.dashboard_not_registered),
            style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun BotonDetalle(
    @StringRes texto: Int,
    habilitado: Boolean,
    onClick: () -> Unit,
    destructiva: Boolean = false
) {
    TextButton(
        modifier = Modifier,
        enabled = habilitado,
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (destructiva) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            })) { Text(stringResource(texto)) }
}

@Composable
private fun EstadoDetalle(estado: EstadoIngreso) {
    val colores = MaterialTheme.estadoIngresoColors
    val color = when (estado) {
        EstadoIngreso.REGISTRADO -> colores.registrado
        EstadoIngreso.EN_REVISION -> colores.enRevision
        EstadoIngreso.EN_REPARACION -> colores.enReparacion
        EstadoIngreso.LISTO_PARA_FIRMA -> colores.listoParaFirma
        EstadoIngreso.LISTO_PARA_ENTREGA -> colores.listoParaEntrega
        EstadoIngreso.ENTREGADO -> colores.entregado
        EstadoIngreso.CANCELADO -> colores.cancelado
        EstadoIngreso.NO_DISPONIBLE -> colores.noDisponible }
    Surface(color = color.copy(alpha = 0.12f), contentColor = color, shape = MaterialTheme.shapes.small) {
        Text(text = stringResource(etiquetaEstadoDetalle(estado)),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )
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
private fun EventoDetalle(evento: EventoIngreso) {
    val titulo = stringResource(
        when (evento.accion) {
            "CREADO" -> R.string.detalle_event_created
            "EDITADO" -> R.string.detalle_event_edited
            "ESTADO" -> R.string.detalle_event_state
            else -> R.string.detalle_event_other
        }
    )

    PanelDetalle(titulo = titulo) {
        Text(text = fechaDetalle(evento.fecha),
            style = MaterialTheme.typography.labelMedium)
        Text(text = stringResource(R.string.detalle_event_author, evento.tecnicoId),
            style = MaterialTheme.typography.bodySmall)

        if (evento.accion == "ESTADO") {
            Text(text = stringResource(
                    R.string.detalle_event_transition, stringResource(etiquetaEstadoDetalle(
                            EstadoIngreso.desdeValor(evento.estadoAnterior))),
                    stringResource(etiquetaEstadoDetalle(EstadoIngreso.desdeValor(evento.estadoNuevo)))
            ))
        }
        if (evento.motivo.isNotBlank()) { Text(evento.motivo) }
    }
}

@Composable
private fun ModulosPendientesDetalle(estado: EstadoIngreso) {
    val recursos = when (estado) {
        EstadoIngreso.EN_REVISION -> listOf(
            R.string.dashboard_action_checklist,
            R.string.dashboard_action_evidence,
            R.string.dashboard_action_diagnosis
        )
        EstadoIngreso.EN_REPARACION -> listOf(
            R.string.dashboard_action_evidence,
            R.string.dashboard_action_diagnosis
        )
        EstadoIngreso.LISTO_PARA_FIRMA -> listOf(
            R.string.dashboard_action_pdf,
            R.string.dashboard_action_sign
        )
        EstadoIngreso.LISTO_PARA_ENTREGA -> listOf(
            R.string.dashboard_action_pdf,
            R.string.dashboard_action_deliver
        )
        else -> emptyList()
    }
    if (recursos.isNotEmpty()) {
        HorizontalDivider()
        Text(text = stringResource(R.string.detalle_pending_title),
            style = MaterialTheme.typography.titleSmall)
        recursos.forEach { recurso ->
            Text(text = stringResource(R.string.detalle_pending_action, stringResource(recurso)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Text(text = stringResource(R.string.detalle_pending_description),
            style = MaterialTheme.typography.bodySmall)
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
            onReintentar = {},
            onLimpiarError = {},
            onConsumirExito = {},
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
            onConsumirExito = {},
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