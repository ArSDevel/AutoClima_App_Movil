package com.example.myapplication.ui.camara.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.camara.CamaraIngresoViewModel

// Observa los cambios de fotos sin recargar ni borrar el formulario del diagnóstico.
@Composable
fun GaleriaIngresoConectada(
    viewModel: CamaraIngresoViewModel,
    tecnicoId: Long,
    habilitada: Boolean = true
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            estado.cargando -> CircularProgressIndicator()
            estado.errorCarga != null -> {
                Text(estado.errorCarga.orEmpty(), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::reintentarCarga, enabled = !estado.guardando) {
                    Text(stringResource(R.string.camera_retry)) } }
            estado.ingresoNoEncontrado -> Text(stringResource(R.string.evidence_missing_ingreso))
            else -> {
                val editable = estado.permiteAgregar && habilitada && estado.rutaPendiente == null
                if (estado.guardando) CircularProgressIndicator()
                val mensaje = estado.errorGaleria ?: estado.mensajeGaleria
                if (mensaje != null) {
                    Text(
                        mensaje,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        color = if (estado.errorGaleria != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface)
                    TextButton(onClick = viewModel::limpiarMensajeGaleria) {
                        Text(stringResource(R.string.evidence_close)) } }
                GaleriaFotografias(
                    rutas = estado.evidencias.map { it.rutaImagen },
                    principal = estado.ingreso?.fotoPath,
                    ocupado = estado.guardando || !habilitada,
                    onPrincipal = if (editable) { ruta ->
                        estado.evidencias.firstOrNull { it.rutaImagen == ruta }?.let {
                            viewModel.seleccionarPrincipal(it.id, tecnicoId) }
                    } else null,
                    onEliminar = if (editable) { ruta ->
                        estado.evidencias.firstOrNull { it.rutaImagen == ruta }?.let {
                            viewModel.eliminarEvidencia(it.id, tecnicoId) }
                    } else null,
                    permiteEliminar = { editable && estado.evidencias.size > 1 },
                    aviso = when {
                        !estado.permiteAgregar -> stringResource(R.string.evidence_read_only)
                        estado.evidencias.size == 1 -> stringResource(R.string.fotos_keep_one)
                        else -> null }) } } } }