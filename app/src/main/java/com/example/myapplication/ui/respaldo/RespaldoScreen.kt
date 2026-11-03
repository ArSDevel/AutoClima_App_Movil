package com.example.myapplication.ui.respaldo

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.util.FormatoFechas
import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.dashboard.DashboardMenuOption
import com.example.myapplication.ui.dashboard.components.DashboardDrawer
import com.example.myapplication.ui.usuarios.BotonPrincipal
import kotlinx.coroutines.launch

// Tipos con los que distintos explorador de archivos marcan un .zip.
val TIPOS_ZIP = arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")

// Pantalla de respaldo: exportar una copia de los datos y restaurar desde un archivo.
@Composable
fun RespaldoScreen(
    viewModel: RespaldoViewModel,
    onVolver: () -> Unit,
    onMenuSeleccionado: (DashboardMenuOption) -> Unit,
    onLogout: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val contexto = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val textoMensaje = estado.mensaje?.let { stringResource(it) }

    val guardar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) viewModel.exportarA(uri) }
    val exportarAntes: () -> Unit = { guardar.launch(viewModel.nombreSugerido()) }
    val elegir = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.elegirRespaldo(uri) }

    LaunchedEffect(textoMensaje) {
        if (textoMensaje != null) {
            snackbarHostState.showSnackbar(textoMensaje)
            viewModel.mensajeVisto() } }

    // Cuando el respaldo para compartir está listo se abre la hoja de compartir de Android.
    LaunchedEffect(estado.archivoParaCompartir) {
        val archivo = estado.archivoParaCompartir ?: return@LaunchedEffect
        val uri = FileProvider.getUriForFile(contexto, "${contexto.packageName}.archivos", archivo)
        val intento = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, contexto.getString(R.string.respaldo_share_subject))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        contexto.startActivity(Intent.createChooser(intento, contexto.getString(R.string.respaldo_share_chooser)))
        viewModel.compartirListo() }

    // La combinación ocupa toda la pantalla mientras se revisa lo que pasaría.
    val plan = estado.plan
    if (plan != null && (estado.paso == PasoRespaldo.VISTA_PREVIA || estado.paso == PasoRespaldo.CONFIRMAR_COMBINAR)) {
        VistaPreviaCombinacion(
            plan = plan, modo = estado.modo, ganadores = estado.ganadores,
            onModo = viewModel::cambiarModo, onGanador = viewModel::cambiarGanador,
            onCombinar = viewModel::pedirConfirmarCombinacion, onCancelar = viewModel::cancelar,
            onExportarAntes = exportarAntes)
    } else {
        BackHandler {
            if (drawerState.isOpen) { scope.launch { drawerState.close() } } else if (!estado.trabajando) { onVolver() } }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = !estado.trabajando,
            drawerContent = {
                DashboardDrawer(
                    opcionSeleccionada = DashboardMenuOption.RESPALDO,
                    onOpcionSeleccionada = { opcion ->
                        scope.launch {
                            drawerState.close()
                            if (opcion != DashboardMenuOption.RESPALDO && !estado.trabajando) onMenuSeleccionado(opcion) } },
                    onLogout = { scope.launch { drawerState.close(); if (!estado.trabajando) onLogout() } }) }) {
            Scaffold(
                containerColor = colors.background,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    EncabezadoAutoClimas(
                        titulo = stringResource(R.string.respaldo_title),
                        onMenu = { scope.launch { drawerState.open() } }) }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                    Column(
                        modifier = Modifier.widthIn(max = 640.dp).fillMaxSize().verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        EstadoUltimoRespaldo(estado.ultimoRespaldoMs)
                        TarjetaRespaldo(
                            titulo = stringResource(R.string.respaldo_export_title),
                            texto = stringResource(R.string.respaldo_export_text)) {
                            BotonPrincipal(
                                texto = stringResource(R.string.respaldo_save),
                                onClick = exportarAntes,
                                habilitado = !estado.trabajando)
                            OutlinedButton(
                                onClick = viewModel::prepararParaCompartir, enabled = !estado.trabajando,
                                modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.respaldo_share)) } }
                        TarjetaRespaldo(
                            titulo = stringResource(R.string.respaldo_restore_title),
                            texto = stringResource(R.string.respaldo_restore_text)) {
                            OutlinedButton(
                                onClick = { elegir.launch(TIPOS_ZIP) }, enabled = !estado.trabajando,
                                modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.respaldo_choose)) } }
                        if (estado.copias.isNotEmpty()) {
                            TarjetaRespaldo(
                                titulo = stringResource(R.string.respaldo_auto_title),
                                texto = stringResource(R.string.respaldo_auto_text)) {
                                estado.copias.forEach { copia ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(FormatoFechas.fechaYHora(copia.fechaMs), style = MaterialTheme.typography.bodyMedium)
                                        TextButton(onClick = { viewModel.elegirCopia(copia) }, enabled = !estado.trabajando) {
                                            Text(stringResource(R.string.respaldo_auto_restore)) } } } } }
                        TarjetaRespaldo(
                            titulo = stringResource(R.string.respaldo_danger_title),
                            texto = stringResource(R.string.respaldo_danger_text), peligro = true) {
                            OutlinedButton(
                                onClick = viewModel::pedirBorrarTodo, enabled = !estado.trabajando,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                                modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.respaldo_wipe_button)) } } } } } } }

    DialogosRespaldo(viewModel = viewModel, estado = estado, onExportarAntes = exportarAntes, pideContrasena = true) }

// Cuánto hace del último respaldo; en rojo si pasó más de una semana o nunca se hizo.
@Composable
private fun EstadoUltimoRespaldo(ultimoMs: Long?) {
    val colors = MaterialTheme.colorScheme
    val dias = ultimoMs?.let { ((System.currentTimeMillis() - it) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(0) }
    val atrasado = dias == null || dias > 7
    val texto = when {
        dias == null -> stringResource(R.string.respaldo_last_never)
        dias == 0 -> stringResource(R.string.respaldo_last_today)
        else -> pluralStringResource(R.plurals.respaldo_last_days, dias, dias) }
    Surface(
        shape = MaterialTheme.shapes.large, color = if (atrasado) colors.errorContainer else colors.primaryContainer) {
        Text(
            text = texto, modifier = Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.titleSmall,
            color = if (atrasado) colors.onErrorContainer else colors.onPrimaryContainer) } }

@Composable
private fun TarjetaRespaldo(titulo: String, texto: String, peligro: Boolean = false, acciones: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large, color = colors.surface, shadowElevation = 1.dp,
        border = if (peligro) BorderStroke(1.dp, colors.error) else null) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = if (peligro) colors.error else colors.onSurface)
            Text(texto, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            acciones() } } }
