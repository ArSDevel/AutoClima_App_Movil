package com.example.myapplication.ui.diagnostico

import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.diagnostico.components.descripcionHallazgo
import com.example.myapplication.ui.theme.estadoIngresoColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import com.example.myapplication.ui.components.DialogoAviso
import com.example.myapplication.ui.components.TipoAviso
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.myapplication.ui.components.BarraAccionInferior
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.activity.compose.BackHandler
import com.example.myapplication.ui.camara.CamaraIngresoViewModel
import com.example.myapplication.ui.camara.components.GaleriaIngresoConectada
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
import androidx.compose.foundation.layout.consumeWindowInsets
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
    evidenciasViewModel: CamaraIngresoViewModel,
    tecnicoId: Long,
    onVolver: () -> Unit,
    onAbrirChecklist: () -> Unit = {},
    onAbrirEvidencias: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val estadoFotos by evidenciasViewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    var confirmarSalida by rememberSaveable {
        mutableStateOf(false)
    }

    // Comparte la validación de salida entre la flecha superior
    // y el botón o gesto de regreso del dispositivo.
    val solicitarSalida: () -> Unit = {
        if (!estado.guardando && !estadoFotos.guardando) {
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
            EncabezadoAutoClimas(
                titulo = stringResource(R.string.diagnostico_title),
                onVolver = solicitarSalida,
                habilitado = !estado.guardando && !estadoFotos.guardando,
                subtitulo = estado.ingreso?.let { listOf(it.placa, it.nombreCliente).filter { t -> t.isNotBlank() }.joinToString(" · ") }
            )
        },
        bottomBar = {
            if (estado.reporteDisponible) {
                BarraAccionInferior {
                    // El envío por WhatsApp lo desarrollará otro equipo.
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.width(64.dp).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.whatsapp_pending))
                    }
                    if (estado.permiteEdicion) {
                        Button(
                            onClick = { viewModel.guardar(tecnicoId) },
                            enabled = estado.puedeGuardar && !estadoFotos.guardando,
                            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (estado.guardando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.onPrimary
                                )
                                Spacer(Modifier.width(12.dp))
                            }
                            Text(
                                text = stringResource(
                                    when {
                                        estado.guardando -> R.string.diagnostico_saving
                                        estado.diagnosticoId != null -> R.string.diagnostico_save_changes
                                        else -> R.string.diagnostico_save
                                    }
                                ),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .consumeWindowInsets(padding)
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
                            if (!estado.permiteEdicion) {
                                item(key = "aviso_edicion") {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.small,
                                        color = colors.primaryContainer,
                                        contentColor = colors.onPrimaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = null,
                                                modifier = Modifier.size(20.dp))
                                            Text(
                                                text = stringResource(
                                                    if (!estado.tieneChecklist) R.string.diagnostico_requires_checklist
                                                    else R.string.diagnostico_read_only),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }

                            item(key = "hallazgos") {
                                CajaHallazgos(
                                    estado = estado,
                                    fotos = estadoFotos.evidencias.map { it.rutaImagen },
                                    onAbrirChecklist = onAbrirChecklist,
                                    onAbrirEvidencias = onAbrirEvidencias
                                )
                            }

                            item(key = "observaciones") {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.large,
                                    color = colors.surface,
                                    shadowElevation = 1.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.diagnostico_notes_title).uppercase(),
                                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.8.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = colors.tertiary
                                        )
                                        OutlinedTextField(
                                            value = estado.observaciones,
                                            onValueChange = viewModel::onObservacionesChange,
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !estado.guardando && !estadoFotos.guardando,
                                            readOnly = !estado.puedeModificar,
                                            minLines = 3,
                                            shape = MaterialTheme.shapes.small
                                        )
                                    }
                                }
                            }

                            item(key = "cotizacion") {
                                CotizacionDiagnostico(
                                    formulario = estado.formulario,
                                    onAgregarConcepto = viewModel::agregarConcepto,
                                    onDescripcionChange = viewModel::onDescripcionConceptoChange,
                                    onImporteChange = viewModel::onImporteConceptoChange,
                                    onEliminarConcepto = viewModel::eliminarConcepto,
                                    habilitado = !estado.guardando && !estado.guardadoExitoso,
                                    soloLectura = !estado.permiteEdicion,
                                    erroresCampos = estado.erroresCampos
                                )
                            }

                            estado.errorGuardado?.let { mensaje ->
                                item(key = "error_guardado") {
                                    Text(text = mensaje, style = MaterialTheme.typography.bodyMedium,
                                        color = colors.error)
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
        DialogoAviso(
            tipo = TipoAviso.ADVERTENCIA,
            titulo = stringResource(R.string.diagnostico_leave_title),
            mensaje = stringResource(R.string.diagnostico_leave_message),
            textoSecundario = stringResource(R.string.diagnostico_discard),
            onSecundario = { confirmarSalida = false
                onVolver() },
            textoPrincipal = stringResource(R.string.diagnostico_keep_editing),
            onPrincipal = { confirmarSalida = false },
            onDismiss = { confirmarSalida = false }
        )
    }

    // Se muestra cuando finaliza el guardado.
    if (estado.guardadoExitoso && !estado.guardando) {
        DialogoAviso(
            tipo = TipoAviso.EXITO,
            titulo = stringResource(R.string.diagnostico_saved_title),
            mensaje = stringResource(R.string.diagnostico_saved_message),
            textoSecundario = stringResource(R.string.diagnostico_stay),
            onSecundario = viewModel::consumirGuardadoExitoso,
            textoPrincipal = stringResource(R.string.diagnostico_back),
            onPrincipal = { viewModel.consumirGuardadoExitoso()
                onVolver() },
            onDismiss = viewModel::consumirGuardadoExitoso
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

// Hallazgos del checklist en una caja compacta, con las fotos del ingreso en miniatura.
@Composable
private fun CajaHallazgos(
    estado: DiagnosticoUiState,
    fotos: List<String>,
    onAbrirChecklist: () -> Unit,
    onAbrirEvidencias: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.diagnostico_findings_title).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.8.sp),
                    fontWeight = FontWeight.Bold,
                    color = colors.tertiary
                )
                TextButton(onClick = onAbrirChecklist) {
                    Text(stringResource(R.string.dashboard_action_checklist), fontWeight = FontWeight.Bold)
                }
            }
            when {
                !estado.tieneChecklist -> FilaHallazgo(
                    icono = Icons.Default.Warning, color = colors.error,
                    texto = stringResource(R.string.diagnostico_checklist_pending))
                estado.hallazgos.isEmpty() -> FilaHallazgo(
                    icono = Icons.Default.Check, color = MaterialTheme.estadoIngresoColors.listoParaFirma,
                    texto = stringResource(R.string.diagnostico_findings_ok))
                else -> estado.hallazgos.forEach { hallazgo ->
                    FilaHallazgo(
                        icono = Icons.Default.Warning, color = colors.error,
                        texto = stringResource(descripcionHallazgo(hallazgo)))
                }
            }
            if (estado.comentariosChecklist.isNotBlank()) {
                Text(text = estado.comentariosChecklist, style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant)
            }
            if (fotos.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fotos.take(3).forEach { ruta ->
                        coil.compose.AsyncImage(
                            model = java.io.File(ruta),
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onAbrirEvidencias)
                        )
                    }
                    if (fotos.size > 3) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant).clickable(onClick = onAbrirEvidencias),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+${fotos.size - 3}", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaHallazgo(icono: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color, texto: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Text(text = texto, style = MaterialTheme.typography.titleSmall)
    }
}
