package com.example.myapplication.ui.firma

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.OutlinedButton
import com.example.myapplication.data.reporte.ConceptoReporte
import com.example.myapplication.data.reporte.DatosReporte
import com.example.myapplication.data.reporte.FotoReporte
import com.example.myapplication.data.reporte.TipoReporte
import com.example.myapplication.ui.camara.CamaraIngresoViewModel
import com.example.myapplication.ui.diagnostico.EnvioReporteViewModel
import com.example.myapplication.ui.diagnostico.components.HojaEnvioReporte
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.reporte.FormatoReporte
import com.example.myapplication.ui.components.BarraAccionInferior
import com.example.myapplication.ui.components.DialogoAviso
import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.components.PildoraEstado
import com.example.myapplication.ui.components.TipoAviso
import com.example.myapplication.ui.components.colorTituloSeccion
import com.example.myapplication.ui.theme.estadoIngresoColors
import java.io.File
import java.text.DateFormat
import java.util.Date

// Firma del cliente al aceptar la cotización. Al confirmar, el ingreso pasa a listo para entrega.
@Composable
fun FirmaScreen(
    viewModel: FirmaViewModel,
    envioViewModel: EnvioReporteViewModel,
    evidenciasViewModel: CamaraIngresoViewModel,
    tecnicoId: Long,
    onVolver: () -> Unit,
    onAbrirEntrega: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val estadoEnvio by envioViewModel.uiState.collectAsStateWithLifecycle()
    val estadoFotos by evidenciasViewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    var mostrarEnvio by rememberSaveable { mutableStateOf(false) }

    // Pide confirmar la salida solo si ya hay una firma dibujada sin guardar.
    val solicitarSalida: () -> Unit = {
        if (!estado.guardando) { if (estado.hayTrazos) confirmarSalida = true else onVolver() } }
    BackHandler { solicitarSalida() }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            EncabezadoAutoClimas(
                titulo = stringResource(R.string.firma_title),
                subtitulo = estado.ingreso?.placa,
                onVolver = solicitarSalida,
                habilitado = !estado.guardando,
                acciones = { estado.estadoActual?.let { PildoraEstado(estado = it, sobreCabecera = true) } }) },
        bottomBar = {
            if (estado.puedeFirmar) {
                BarraAccionInferior {
                    Button(
                        onClick = {
                            viewModel.confirmar(renderizarFirma(estado.trazos), tecnicoId) },
                        enabled = estado.puedeConfirmar,
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.estadoIngresoColors.listoParaFirma)) {
                        if (estado.guardando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                                color = colors.onPrimary)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(22.dp)) }
                        Text(
                            text = "  " + stringResource(if (estado.guardando) R.string.firma_saving else R.string.firma_confirm),
                            style = MaterialTheme.typography.titleMedium) } }
            } else if (estado.firmaGuardada != null) {
                // Con la firma guardada ya se puede mandar el acta al cliente.
                BarraAccionInferior {
                    if (estado.estadoActual == EstadoIngreso.LISTO_PARA_ENTREGA) {
                        OutlinedButton(
                            onClick = { mostrarEnvio = true },
                            modifier = Modifier.width(64.dp).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send_button)) }
                        Button(onClick = onAbrirEntrega, modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            shape = RoundedCornerShape(16.dp)) {
                            Text(stringResource(R.string.firma_to_delivery), style = MaterialTheme.typography.titleMedium) }
                    } else {
                        Button(onClick = { mostrarEnvio = true }, modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.estadoIngresoColors.listoParaFirma)) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("  " + stringResource(R.string.send_delivery_receipt), style = MaterialTheme.typography.titleMedium) } } } } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            when {
                estado.cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                estado.errorCarga != null -> Text(estado.errorCarga!!, color = colors.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp), textAlign = TextAlign.Center)
                else -> Column(
                    modifier = Modifier.widthIn(max = 640.dp).fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResumenFirma(nombre = estado.ingreso?.nombreCliente.orEmpty(), total = estado.totalCentavos)
                    Surface(shape = MaterialTheme.shapes.large, color = colors.surface, shadowElevation = 1.dp) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.firma_customer_signature).uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold, color = colorTituloSeccion())
                                if (estado.puedeFirmar) {
                                    TextButton(onClick = viewModel::borrar, enabled = estado.hayTrazos && !estado.guardando) {
                                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text("  " + stringResource(R.string.firma_clear), fontWeight = FontWeight.Bold) } } }
                            val guardada = estado.firmaGuardada
                            if (estado.puedeFirmar) {
                                RecuadroFirma(
                                    trazos = estado.trazos,
                                    habilitado = !estado.guardando,
                                    onInicio = viewModel::iniciarTrazo,
                                    onPunto = viewModel::agregarPunto)
                            } else if (guardada != null) {
                                FirmaGuardada(ruta = guardada.rutaImagen, fecha = guardada.fecha)
                            } else {
                                Text(stringResource(R.string.firma_not_available), style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant, modifier = Modifier.padding(8.dp)) } } }
                    estado.error?.let { Text(it, color = colors.error, style = MaterialTheme.typography.bodyMedium) }
                    if (estado.puedeFirmar) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.firma_next), style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant) } } } } } }

    // Hoja para enviar el acta de entrega por WhatsApp. Aquí solo se decide si se agregan las evidencias.
    val ingresoEnvio = estado.ingreso
    if (mostrarEnvio && ingresoEnvio != null) {
        // Las fotos del acta son las evidencias del ingreso; el técnico decide en la hoja si se incluyen.
        val fotosActa = estadoFotos.evidencias.sortedBy { it.id }.map { FotoReporte(File(it.rutaImagen), it.descripcion) }
        val mensaje = stringResource(R.string.send_message_delivery, ingresoEnvio.nombreCliente, ingresoEnvio.modelo, ingresoEnvio.placa)
        val datos = DatosReporte(
            ingresoId = ingresoEnvio.ingresoId, placa = ingresoEnvio.placa, modelo = ingresoEnvio.modelo,
            color = ingresoEnvio.color, serie = ingresoEnvio.numeroSerie, cliente = ingresoEnvio.nombreCliente,
            telefono = ingresoEnvio.telefonoCliente, correo = ingresoEnvio.emailCliente,
            fechaEntrada = ingresoEnvio.fechaEntrada, motivo = ingresoEnvio.motivoIngreso,
            condicionInicial = ingresoEnvio.condicionInicial, hallazgos = emptyList(), checklist = emptyList(),
            comentariosChecklist = "", observaciones = "",
            conceptos = estado.conceptos.map { ConceptoReporte(it.descripcion, it.importeCentavos) },
            totalCentavos = estado.totalCentavos, fotos = fotosActa, tecnicoId = null)
        HojaEnvioReporte(
            estado = estadoEnvio,
            nombreCliente = ingresoEnvio.nombreCliente,
            telefono = ingresoEnvio.telefonoCliente,
            cantidadFotos = fotosActa.size,
            mensaje = mensaje,
            tipos = listOf(TipoReporte.ACTA),
            onCambiarTipo = envioViewModel::cambiarTipo,
            onCambiarReporte = envioViewModel::cambiarReporte,
            onCambiarFotos = envioViewModel::cambiarFotos,
            onEnviar = { envioViewModel.enviar(datos, mensaje, tecnicoId) },
            onAbrirChat = { envioViewModel.abrirChat(ingresoEnvio.telefonoCliente, mensaje) },
            onCerrar = { mostrarEnvio = false
                envioViewModel.reiniciar() }) }

    // Avisa antes de perder una firma dibujada.
    if (confirmarSalida) {
        DialogoAviso(
            tipo = TipoAviso.ADVERTENCIA,
            titulo = stringResource(R.string.firma_leave_title),
            mensaje = stringResource(R.string.firma_leave_message),
            textoSecundario = stringResource(R.string.firma_leave),
            onSecundario = { confirmarSalida = false
                onVolver() },
            textoPrincipal = stringResource(R.string.firma_keep),
            onPrincipal = { confirmarSalida = false },
            onDismiss = { confirmarSalida = false }) }

    // Se muestra cuando la firma ya quedó guardada.
    if (estado.firmaConfirmada && !estado.guardando) {
        DialogoAviso(
            tipo = TipoAviso.EXITO,
            titulo = stringResource(R.string.firma_saved_title),
            mensaje = stringResource(R.string.firma_saved_message),
            textoSecundario = stringResource(R.string.firma_to_detail),
            onSecundario = { viewModel.consumirExito()
                onVolver() },
            textoPrincipal = stringResource(R.string.firma_to_delivery),
            onPrincipal = { viewModel.consumirExito()
                onAbrirEntrega() },
            onDismiss = viewModel::consumirExito) } }

// Cliente y total de la cotización que se acepta con la firma.
@Composable
private fun ResumenFirma(nombre: String, total: Long?) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = MaterialTheme.shapes.large, color = colors.surface, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Row(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = colors.onSurfaceVariant)
                Text(nombre, style = MaterialTheme.typography.bodyLarge) }
            Row(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = colors.onSurfaceVariant)
                Text(stringResource(R.string.firma_quote), style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f))
                Text(total?.let { FormatoReporte.pesos(it) } ?: stringResource(R.string.firma_quote_pending),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold) } } } }

// Recuadro donde el cliente dibuja con el dedo. Guarda los puntos en proporción al tamaño.
@Composable
private fun RecuadroFirma(
    trazos: List<List<Offset>>,
    habilitado: Boolean,
    onInicio: (Offset) -> Unit,
    onPunto: (Offset) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tinta = colors.onSurface
    val linea = colors.onSurfaceVariant
    val descripcion = stringResource(R.string.firma_canvas_description)
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(ASPECTO_FIRMA)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.5f))
            .border(2.dp, colors.outline, RoundedCornerShape(14.dp))
            .semantics { contentDescription = descripcion }
            .pointerInput(habilitado) {
                if (!habilitado) return@pointerInput
                detectDragGestures(
                    onDragStart = { punto -> onInicio(Offset(punto.x / size.width, punto.y / size.height)) },
                    onDrag = { cambio, _ ->
                        cambio.consume()
                        onPunto(Offset(cambio.position.x / size.width, cambio.position.y / size.height)) }) }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Línea guía y marca de la firma.
            drawLine(color = linea, start = Offset(24.dp.toPx(), size.height - 48.dp.toPx()),
                end = Offset(size.width - 24.dp.toPx(), size.height - 48.dp.toPx()), strokeWidth = 1.5.dp.toPx())
            val estilo = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            trazos.forEach { puntos ->
                if (puntos.size > 1) {
                    val camino = Path().apply {
                        moveTo(puntos.first().x * size.width, puntos.first().y * size.height)
                        puntos.drop(1).forEach { lineTo(it.x * size.width, it.y * size.height) } }
                    drawPath(camino, color = tinta, style = estilo) } } }
        if (trazos.isEmpty()) {
            Text(stringResource(R.string.firma_hint), style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant, modifier = Modifier.align(Alignment.Center)) } } }

// Firma ya registrada, sobre blanco como se guardó.
@Composable
private fun FirmaGuardada(ruta: String, fecha: Long) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AsyncImage(
            model = File(ruta),
            contentDescription = stringResource(R.string.firma_canvas_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().aspectRatio(ASPECTO_FIRMA)
                .background(Color.White, RoundedCornerShape(14.dp))
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(14.dp)))
        Text(
            text = stringResource(R.string.firma_signed_on,
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(fecha))),
            style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)) } }
