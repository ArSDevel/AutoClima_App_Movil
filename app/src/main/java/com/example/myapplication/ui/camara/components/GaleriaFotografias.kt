package com.example.myapplication.ui.camara.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.myapplication.R
import java.io.File

// Cuadrícula de tres columnas compartida por Registro, Evidencias y Diagnóstico.
// El llamador decide qué cambios están permitidos y cómo se guardan.
@Composable
fun GaleriaFotografias(
    rutas: List<String>,
    principal: String?,
    ocupado: Boolean = false,
    onPrincipal: ((String) -> Unit)? = null,
    onEliminar: ((String) -> Unit)? = null,
    permiteEliminar: (String) -> Boolean = { false },
    aviso: String? = null,
    mostrarVacio: Boolean = true
) {
    var seleccion by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }
    val fotos = rutas.distinct()
    val colors = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (fotos.isEmpty() && mostrarVacio) {
            Text(stringResource(R.string.evidence_empty), color = colors.onSurfaceVariant) }
        fotos.chunked(3).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { ruta ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        border = if (ruta == principal) BorderStroke(2.dp, colors.primary) else null
                    ) {
                        Box(
                            modifier = Modifier.aspectRatio(1f).clickable(
                                enabled = !ocupado,
                                onClickLabel = stringResource(R.string.fotos_open_options)
                            ) {
                                confirmarEliminar = false
                                seleccion = ruta }
                        ) {
                            ImagenFotografia(ruta, Modifier.fillMaxSize(), recortar = true)
                            if (ruta == principal) {
                                // La foto principal es la que se ve en la lista de ingresos.
                                Surface(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(28.dp),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = androidx.compose.ui.graphics.Color(0xFFF5B301)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = stringResource(R.string.fotos_main),
                                            tint = androidx.compose.ui.graphics.Color(0xFF3A2C08),
                                            modifier = Modifier.size(16.dp)) } } }
                            // Quitar la foto sin abrir primero sus opciones.
                            if (onEliminar != null && permiteEliminar(ruta)) {
                                Surface(
                                    onClick = {
                                        seleccion = ruta
                                        confirmarEliminar = true },
                                    enabled = !ocupado,
                                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp).size(28.dp),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = colors.scrim.copy(alpha = 0.6f),
                                    contentColor = androidx.compose.ui.graphics.Color.White
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.fotos_delete),
                                            modifier = Modifier.size(16.dp)) } } } } } }
                repeat(3 - fila.size) { Spacer(Modifier.weight(1f)) } } }
        if (aviso != null && fotos.isNotEmpty()) {
            Text(aviso, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) } }

    val rutaSeleccionada = seleccion?.takeIf { it in fotos }
    if (rutaSeleccionada != null) {
        if (!confirmarEliminar) Dialog(onDismissRequest = {
            if (!ocupado) {
                seleccion = null
                confirmarEliminar = false }
        }) {
            Surface(shape = MaterialTheme.shapes.large) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ImagenFotografia(rutaSeleccionada, Modifier.fillMaxWidth().height(300.dp)) }
                    item {
                        if (rutaSeleccionada == principal) {
                            Text(stringResource(R.string.fotos_main), color = colors.primary)
                        } else if (onPrincipal != null) {
                            Button(
                                onClick = {
                                    onPrincipal(rutaSeleccionada)
                                    seleccion = null },
                                enabled = !ocupado,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(stringResource(R.string.fotos_choose_main)) } } }
                    item {
                        if (onEliminar != null && permiteEliminar(rutaSeleccionada)) {
                            TextButton(
                                onClick = { confirmarEliminar = true },
                                enabled = !ocupado,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                            ) { Text(stringResource(R.string.fotos_delete)) }
                        } else if (aviso != null) {
                            Text(aviso, style = MaterialTheme.typography.bodySmall) } }
                    item {
                        TextButton(
                            onClick = { seleccion = null },
                            enabled = !ocupado,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.evidence_close)) } } } } }
        if (confirmarEliminar) {
            AlertDialog(
                onDismissRequest = { if (!ocupado) confirmarEliminar = false },
                title = { Text(stringResource(R.string.fotos_delete_title)) },
                text = { Text(stringResource(R.string.fotos_delete_message)) },
                confirmButton = {
                    TextButton(
                        enabled = !ocupado && permiteEliminar(rutaSeleccionada),
                        onClick = {
                            confirmarEliminar = false
                            seleccion = null
                            onEliminar?.invoke(rutaSeleccionada) }
                    ) { Text(stringResource(R.string.fotos_delete)) } },
                dismissButton = {
                    TextButton(onClick = { confirmarEliminar = false }, enabled = !ocupado) {
                        Text(stringResource(R.string.fotos_cancel)) } }) } } }

// Muestra una fotografía con indicador de carga y de error.
@Composable
private fun ImagenFotografia(ruta: String, modifier: Modifier, recortar: Boolean = false) {
    var error by remember(ruta) { mutableStateOf(false) }
    var cargando by remember(ruta) { mutableStateOf(true) }
    Box(modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = File(ruta),
            contentDescription = stringResource(R.string.evidence_photo_description),
            contentScale = if (recortar) ContentScale.Crop else ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            onLoading = { cargando = true; error = false },
            onSuccess = { cargando = false; error = false },
            onError = { cargando = false; error = true })
        if (cargando) CircularProgressIndicator(modifier = Modifier.size(24.dp))
        if (error) Text(
            stringResource(R.string.evidence_image_unavailable),
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error) } }