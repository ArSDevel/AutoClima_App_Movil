package com.example.myapplication.ui.diagnostico

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import com.example.myapplication.data.local.model.EstadoIngreso
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.diagnostico.components.CotizacionDiagnostico
import com.example.myapplication.ui.diagnostico.components.DatosIngresoDiagnostico
import com.example.myapplication.ui.diagnostico.components.HallazgosDiagnostico
import com.example.myapplication.ui.diagnostico.components.SeccionDiagnostico
import com.example.myapplication.ui.theme.AutoClimasGradients

// Conecta el reporte con su ViewModel.
// El técnico debe provenir de la sesión autenticada.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticoScreen(
    viewModel: DiagnosticoViewModel,
    tecnicoId: Long,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    var confirmarSalida by rememberSaveable {
        mutableStateOf(false)
    }

    // Comparte la validación de salida entre la flecha superior
    // y el botón o gesto de regreso del dispositivo.
    val solicitarSalida: () -> Unit = {
        if (!estado.guardando) {
            if (estado.tieneCambiosSinGuardar) {
                confirmarSalida = true
            } else {
                onVolver()
            }
        }
    }

    BackHandler {
        solicitarSalida()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.diagnostico_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = solicitarSalida,
                        enabled = !estado.guardando
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.diagnostico_back
                            )
                        )
                    }
                },
                actions = {
                    Image(
                        painter = painterResource(
                            R.drawable.logo_autoclimas
                        ),
                        contentDescription = stringResource(
                            R.string.dashboard_logo_description
                        ),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .width(72.dp)
                            .height(56.dp)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AutoClimasGradients.loginBackground)
                .padding(padding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                estado.cargando -> {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        shape = MaterialTheme.shapes.large,
                        color = colors.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()

                            Text(
                                stringResource(
                                    R.string.diagnostico_loading
                                )
                            )
                        }
                    }
                }

                estado.errorCarga != null ||
                        estado.ingresoNoEncontrado -> {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .widthIn(max = 480.dp)
                            .padding(24.dp),
                        shape = MaterialTheme.shapes.large,
                        color = colors.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.diagnostico_load_error_title
                                ),
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = estado.errorCarga
                                    ?: stringResource(
                                        R.string.diagnostico_not_found
                                    ),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Button(
                                onClick = viewModel::reintentarCarga,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(
                                        R.string.diagnostico_retry
                                    )
                                )
                            }
                        }
                    }
                }

                estado.reporteDisponible -> {
                    val ingreso = estado.ingreso

                    if (ingreso != null) {
                        LazyColumn(
                            modifier = Modifier
                                .widthIn(max = 1000.dp)
                                .fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item(key = "presentacion") {
                                EncabezadoReporteDiagnostico(estado)
                            }

                            if (!estado.permiteEdicion) {
                                item(key = "aviso_edicion") {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.medium,
                                        color = colors.secondaryContainer,
                                        contentColor = colors.onSecondaryContainer
                                    ) {
                                        Text(
                                            text = stringResource(
                                                if (!estado.tieneChecklist) {
                                                    R.string.diagnostico_requires_checklist
                                                } else {
                                                    R.string.diagnostico_read_only
                                                }
                                            ),
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            item(key = "datos") {
                                DatosIngresoDiagnostico(
                                    ingreso = ingreso
                                )
                            }

                            item(key = "hallazgos") {
                                HallazgosDiagnostico(
                                    tieneChecklist = estado.tieneChecklist,
                                    hallazgos = estado.hallazgos,
                                    comentariosChecklist =
                                        estado.comentariosChecklist
                                )
                            }

                            item(key = "observaciones") {
                                SeccionDiagnostico(
                                    titulo = stringResource(
                                        R.string.diagnostico_notes_title
                                    ),
                                    descripcion = stringResource(
                                        R.string.diagnostico_notes_description
                                    ),
                                    icono = Icons.Default.Edit
                                ) {
                                    OutlinedTextField(
                                        value = estado.observaciones,
                                        onValueChange =
                                            viewModel::onObservacionesChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !estado.guardando,
                                        readOnly = !estado.puedeModificar,
                                        label = {
                                            Text(
                                                stringResource(
                                                    R.string.diagnostico_notes_label
                                                )
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                stringResource(
                                                    R.string.diagnostico_notes_hint
                                                )
                                            )
                                        },
                                        minLines = 4,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                }
                            }

                            item(key = "evidencias") {
                                SeccionDiagnostico(
                                    titulo = stringResource(
                                        R.string.diagnostico_evidence_title
                                    ),
                                    icono = Icons.Default.PhotoCamera
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.diagnostico_evidence_pending
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }

                            item(key = "cotizacion") {
                                CotizacionDiagnostico(
                                    formulario = estado.formulario,
                                    onAgregarConcepto =
                                        viewModel::agregarConcepto,
                                    onDescripcionChange =
                                        viewModel::onDescripcionConceptoChange,
                                    onImporteChange =
                                        viewModel::onImporteConceptoChange,
                                    onEliminarConcepto =
                                        viewModel::eliminarConcepto,
                                    habilitado = !estado.guardando &&
                                            !estado.guardadoExitoso,
                                    soloLectura = !estado.permiteEdicion,
                                    erroresCampos = estado.erroresCampos,
                                    mensajeBloqueo = when {
                                        estado.permiteEdicion -> null

                                        estado.estadoActual == EstadoIngreso.REGISTRADO ->
                                            stringResource(R.string.diagnostico_quote_blocked_registered)

                                        estado.estadoActual == EstadoIngreso.ENTREGADO ||
                                                estado.estadoActual == EstadoIngreso.CANCELADO ->
                                            stringResource(R.string.diagnostico_quote_blocked_closed)

                                        estado.estadoActual == EstadoIngreso.EN_REVISION ||
                                                estado.estadoActual == EstadoIngreso.EN_REPARACION ->
                                            stringResource(R.string.diagnostico_quote_blocked_checklist)

                                        else ->
                                            stringResource(R.string.diagnostico_quote_blocked_stage)
                                    }
                                )
                            }

                            item(key = "acciones") {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.large,
                                    color = colors.surface
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(12.dp)
                                    ) {
                                        estado.errorGuardado?.let { mensaje ->
                                            Text(
                                                text = mensaje,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.error
                                            )
                                        }

                                        if (estado.permiteEdicion) {
                                            Button(
                                                onClick = {
                                                    viewModel.guardar(tecnicoId)
                                                },
                                                enabled = estado.puedeGuardar,
                                                modifier = Modifier.fillMaxWidth(),
                                                contentPadding =
                                                    PaddingValues(16.dp)
                                            ) {
                                                if (estado.guardando) {
                                                    CircularProgressIndicator(
                                                        modifier =
                                                            Modifier.size(20.dp),
                                                        strokeWidth = 2.dp,
                                                        color = colors.primary
                                                    )

                                                    Spacer(
                                                        Modifier.width(12.dp)
                                                    )
                                                }

                                                Text(
                                                    stringResource(
                                                        when {
                                                            estado.guardando ->
                                                                R.string.diagnostico_saving

                                                            estado.diagnosticoId != null ->
                                                                R.string.diagnostico_save_changes

                                                            else ->
                                                                R.string.diagnostico_save
                                                        }
                                                    )
                                                )
                                            }
                                        }

                                        // El envío todavía no está implementado.
                                        OutlinedButton(
                                            onClick = {},
                                            enabled = false,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(16.dp)
                                        ) {
                                            Text(
                                                stringResource(
                                                    R.string.diagnostico_send
                                                )
                                            )
                                        }

                                        Text(
                                            text = stringResource(
                                                R.string.diagnostico_send_pending
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.onSurfaceVariant
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

    // Advierte antes de descartar notas o cambios en la cotización.
    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = {
                Text(stringResource(R.string.diagnostico_leave_title)
                )
            },
            text = {
                Text(stringResource(R.string.diagnostico_leave_message)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                        confirmarSalida = false
                        onVolver() }
                ) { Text(stringResource(R.string.diagnostico_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text(stringResource(R.string.diagnostico_keep_editing)) }
            }
        )
    }

    // Se muestra cuando finaliza el guardado.
    if (estado.guardadoExitoso && !estado.guardando) {
        AlertDialog(
            onDismissRequest = viewModel::consumirGuardadoExitoso,
            icon = {
                Icon(imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.primary) },
            title = {
                Text(stringResource(R.string.diagnostico_saved_title)) },
            text = {
                Text(stringResource(R.string.diagnostico_saved_message)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.consumirGuardadoExitoso()
                        onVolver() }
                ) {
                    Text(stringResource(R.string.diagnostico_back))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::consumirGuardadoExitoso
                ) {
                    Text(stringResource(R.string.diagnostico_stay))
                }
            }
        )
    }
}

// Presentación del reporte y estado de sus cambios.
// No representa un documento enviado ni firmado.
@Composable
private fun EncabezadoReporteDiagnostico(
    estado: DiagnosticoUiState
) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        contentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.primary)
            Box(
                modifier = Modifier.width(40.dp).height(4.dp)
                    .background(
                        color = colors.secondary,
                        shape = MaterialTheme.shapes.extraSmall))
            Text(text = stringResource(
                    R.string.diagnostico_report_description),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Text(
                text = stringResource(
                    when {estado.tieneCambiosSinGuardar ->
                            R.string.diagnostico_unsaved_changes
                        estado.diagnosticoId != null ->
                            R.string.diagnostico_saved_record
                        else ->
                            R.string.diagnostico_new_record
                    }),
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary
            )
        }
    }
}