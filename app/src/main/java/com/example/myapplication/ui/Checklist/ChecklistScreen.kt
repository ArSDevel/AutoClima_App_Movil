package com.example.myapplication.ui.checklist

import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.components.DialogoAviso
import com.example.myapplication.ui.components.TipoAviso
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.PhotoCamera
import com.example.myapplication.ui.components.BarraAccionInferior
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.myapplication.ui.theme.AutoClimasGradients
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.checklist.components.PreguntaChecklist
import com.example.myapplication.ui.checklist.components.SeccionChecklist

// Pantalla del checklist: nueve preguntas, fotos y comentarios.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel,
    tecnicoId: Long,
    onVolver: () -> Unit,
    evidenciasViewModel: com.example.myapplication.ui.camara.CamaraIngresoViewModel? = null,
    onAgregarFoto: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    // Se utiliza tanto desde la barra superior como
    // desde el botón o gesto de regreso del dispositivo.
    val solicitarSalida: () -> Unit = {
        if (!estado.guardando) {
            if (estado.tieneCambiosSinGuardar) {
                confirmarSalida = true
            } else {
                onVolver() } } }

    // Durante el guardado también intercepta el regreso
    // para evitar abandonar la operación.
    BackHandler {
        solicitarSalida() }

    val respondidas = listOf(
        estado.faltaGasRefrigerante, estado.fugaVisibleMangueras, estado.fugasDetectadasLuzUV,
        estado.compresorEmbragaAlEncender, estado.compresorDanadoOAmarrado, estado.bandaCompresorDesgastada,
        estado.funcionanAbanicosRadiador, estado.filtroCabinaSucioUObstruido, estado.condensadorObstruido
    ).count { it != null }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EncabezadoAutoClimas(
                titulo = stringResource(R.string.checklist_title),
                subtitulo = estado.ingreso?.placa,
                onVolver = solicitarSalida,
                habilitado = !estado.guardando,
                // Barra de avance dentro de la cabecera, como en el diseño.
                contenidoInferior = if (estado.formularioDisponible) {
                    {
                        LinearProgressIndicator(
                            progress = { respondidas / 9f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = if (respondidas == 0) androidx.compose.ui.graphics.Color.Transparent
                            else androidx.compose.ui.graphics.Color.White,
                            trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round) }
                } else null,
                acciones = {
                    if (estado.formularioDisponible) {
                        Text(
                            text = "$respondidas/9",
                            style = MaterialTheme.typography.titleLarge,
                            color = androidx.compose.ui.graphics.Color.White) } }) },
        bottomBar = {
            // Guardar es la acción principal del checklist.
            if (estado.formularioDisponible && estado.permiteEdicion) {
                BarraAccionInferior {
                    Button(
                        onClick = { viewModel.guardar(tecnicoId) },
                        enabled = estado.puedeGuardar,
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (estado.guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(12.dp)) }
                        Text(
                            text = stringResource(
                                when {
                                    estado.guardando -> R.string.checklist_saving
                                    estado.checklistId != null -> R.string.checklist_save_changes
                                    else -> R.string.checklist_save }),
                            style = MaterialTheme.typography.titleMedium) } } } }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                estado.cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp)) }

                estado.errorCarga != null || estado.ingresoNoEncontrado -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .widthIn(max = 480.dp)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = estado.errorCarga
                                ?: stringResource(
                                    R.string.checklist_not_found),
                            style = MaterialTheme.typography.bodyLarge)

                        Button(onClick = viewModel::reintentarCarga) {
                            Text(stringResource(R.string.checklist_retry)) } } }

                estado.formularioDisponible -> {
                    LazyColumn(
                        modifier = Modifier
                            .widthIn(max = 840.dp)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (!estado.permiteEdicion) {
                            item(key = "consulta") {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme
                                        .primaryContainer,
                                    contentColor = MaterialTheme.colorScheme
                                        .onPrimaryContainer
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.checklist_read_only),
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium) } } }

                        item(key = "refrigerante") {
                            SeccionChecklist(
                                titulo = stringResource(
                                    R.string.checklist_section_refrigerant_title),
                                descripcion = stringResource(
                                    R.string.checklist_section_refrigerant_description),
                                icono = Icons.Default.Search,
                                preguntasRespondidas = listOf(
                                    estado.faltaGasRefrigerante,
                                    estado.fugaVisibleMangueras,
                                    estado.fugasDetectadasLuzUV
                                ).count { it != null },
                                totalPreguntas = 3
                            ) {
                                PreguntaChecklist(
                                    numero = 1,
                                    pregunta = stringResource(
                                        R.string.checklist_question_refrigerant),
                                    respuesta = estado.faltaGasRefrigerante,
                                    onRespuestaChange =
                                        viewModel::onFaltaGasRefrigeranteChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes)

                                PreguntaChecklist(
                                    numero = 2,
                                    pregunta = stringResource(
                                        R.string.checklist_question_hoses),
                                    respuesta = estado.fugaVisibleMangueras,
                                    onRespuestaChange =
                                        viewModel::onFugaVisibleManguerasChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes)

                                PreguntaChecklist(
                                    numero = 3,
                                    pregunta = stringResource(
                                        R.string.checklist_question_uv),
                                    respuesta = estado.fugasDetectadasLuzUV,
                                    onRespuestaChange =
                                        viewModel::onFugasDetectadasLuzUVChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes) } }

                        item(key = "compresor") {
                            SeccionChecklist(
                                titulo = stringResource(
                                    R.string.checklist_section_compressor_title),
                                descripcion = stringResource(
                                    R.string.checklist_section_compressor_description),
                                icono = Icons.Default.Build,
                                preguntasRespondidas = listOf(
                                    estado.compresorEmbragaAlEncender,
                                    estado.compresorDanadoOAmarrado,
                                    estado.bandaCompresorDesgastada
                                ).count { it != null },
                                totalPreguntas = 3
                            ) {
                                PreguntaChecklist(
                                    numero = 4,
                                    pregunta = stringResource(
                                        R.string.checklist_question_engagement),
                                    respuesta = estado.compresorEmbragaAlEncender,
                                    onRespuestaChange =
                                        viewModel::onCompresorEmbragaAlEncenderChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes)

                                PreguntaChecklist(
                                    numero = 5,
                                    pregunta = stringResource(
                                        R.string.checklist_question_compressor_damage),
                                    respuesta = estado.compresorDanadoOAmarrado,
                                    onRespuestaChange =
                                        viewModel::onCompresorDanadoOAmarradoChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes)

                                PreguntaChecklist(
                                    numero = 6,
                                    pregunta = stringResource(
                                        R.string.checklist_question_belt),
                                    respuesta = estado.bandaCompresorDesgastada,
                                    onRespuestaChange =
                                        viewModel::onBandaCompresorDesgastadaChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes) } }

                        item(key = "ventilacion") {
                            SeccionChecklist(
                                titulo = stringResource(
                                    R.string.checklist_section_ventilation_title),
                                descripcion = stringResource(
                                    R.string.checklist_section_ventilation_description),
                                icono = Icons.Default.Settings,
                                preguntasRespondidas = listOf(
                                    estado.funcionanAbanicosRadiador,
                                    estado.filtroCabinaSucioUObstruido,
                                    estado.condensadorObstruido
                                ).count { it != null },
                                totalPreguntas = 3
                            ) {
                                PreguntaChecklist(
                                    numero = 7,
                                    pregunta = stringResource(
                                        R.string.checklist_question_fans),
                                    respuesta = estado.funcionanAbanicosRadiador,
                                    onRespuestaChange =
                                        viewModel::onFuncionanAbanicosRadiadorChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes)

                                PreguntaChecklist(
                                    numero = 8,
                                    pregunta = stringResource(
                                        R.string.checklist_question_filter),
                                    respuesta = estado.filtroCabinaSucioUObstruido,
                                    onRespuestaChange =
                                        viewModel::onFiltroCabinaSucioUObstruidoChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes)

                                PreguntaChecklist(
                                    numero = 9,
                                    pregunta = stringResource(
                                        R.string.checklist_question_condenser),
                                    respuesta = estado.condensadorObstruido,
                                    onRespuestaChange =
                                        viewModel::onCondensadorObstruidoChange,
                                    habilitada = estado.puedeModificar,
                                    mostrarPendiente =
                                        estado.mostrarPreguntasPendientes) } }

                        item(key = "evidencias") {
                            if (evidenciasViewModel != null) {
                                val estadoFotos by evidenciasViewModel.uiState.collectAsStateWithLifecycle()
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary)
                                        Text(
                                            text = stringResource(R.string.registro_evidence_title).uppercase(),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            color = com.example.myapplication.ui.components.colorTituloSeccion()) }
                                    if (estadoFotos.permiteAgregar && estadoFotos.rutaPendiente == null) {
                                        androidx.compose.material3.OutlinedButton(
                                            onClick = onAgregarFoto,
                                            enabled = estadoFotos.puedeCapturar && !estado.guardando,
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                            Text("  " + stringResource(R.string.fotos_add),
                                                style = MaterialTheme.typography.titleMedium) } }
                                    com.example.myapplication.ui.camara.components.GaleriaIngresoConectada(
                                        viewModel = evidenciasViewModel,
                                        tecnicoId = tecnicoId,
                                        habilitada = !estado.guardando) } } }

                        item(key = "comentarios") {
                            OutlinedTextField(
                                value = estado.comentarios,
                                onValueChange = viewModel::onComentariosChange,
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = !estado.puedeModificar,
                                label = {
                                    Text(
                                        stringResource(
                                            R.string.checklist_comments)) },
                                minLines = 4,
                                shape = MaterialTheme.shapes.medium) }

                        estado.errorGuardado?.let { mensaje ->
                            item(key = "error_guardado") {
                                Text(
                                    text = mensaje,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium) } } } } } } }

    // Confirma la salida únicamente cuando hay cambios pendientes.
    if (confirmarSalida) {
        DialogoAviso(
            tipo = TipoAviso.ADVERTENCIA,
            titulo = stringResource(R.string.checklist_leave_title),
            mensaje = stringResource(R.string.checklist_leave_message),
            textoSecundario = stringResource(R.string.checklist_discard),
            onSecundario = { confirmarSalida = false
                onVolver() },
            textoPrincipal = stringResource(R.string.checklist_keep_editing),
            onPrincipal = { confirmarSalida = false },
            onDismiss = { confirmarSalida = false }) }

    // Espera a que termine toda la operación antes de mostrar el éxito.
    if (estado.guardadoExitoso && !estado.guardando) {
        DialogoAviso(
            tipo = TipoAviso.EXITO,
            titulo = stringResource(R.string.checklist_saved_title),
            mensaje = stringResource(R.string.checklist_saved_message),
            textoSecundario = stringResource(R.string.checklist_stay),
            onSecundario = viewModel::consumirGuardadoExitoso,
            textoPrincipal = stringResource(R.string.checklist_back_to_detail),
            onPrincipal = { viewModel.consumirGuardadoExitoso()
                onVolver() },
            onDismiss = viewModel::consumirGuardadoExitoso) } }

// Presenta el vehículo y el avance real de las respuestas.
// El progreso no representa un diagnóstico ni un cambio de estado.
@Composable
private fun EncabezadoChecklist(
    estado: ChecklistUiState
) {
    val ingreso = estado.ingreso ?: return
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = colors.primaryContainer,
                    contentColor = colors.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(28.dp)) }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = ingreso.placa,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.primary)

                    Text(
                        text = ingreso.modelo,
                        style = MaterialTheme.typography.bodyLarge)

                    Text(
                        text = stringResource(
                            R.string.checklist_ingreso_number,
                            ingreso.ingresoId),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant) } }

            Text(
                text = stringResource(R.string.checklist_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant)

            LinearProgressIndicator(
                progress = { estado.progreso },
                modifier = Modifier.fillMaxWidth())

            Text(
                text = stringResource(
                    R.string.checklist_section_progress,
                    estado.preguntasRespondidas,
                    estado.totalPreguntas),
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary)

            Text(
                text = stringResource(
                    when {
                        estado.tieneCambiosSinGuardar ->
                            R.string.checklist_unsaved_changes

                        estado.checklistId != null ->
                            R.string.checklist_saved_record

                        else ->
                            R.string.checklist_new_record }),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant) } } }