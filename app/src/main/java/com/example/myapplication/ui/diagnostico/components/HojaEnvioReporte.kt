package com.example.myapplication.ui.diagnostico.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.reporte.TipoReporte
import com.example.myapplication.ui.diagnostico.EnvioReporteUiState
import com.example.myapplication.ui.diagnostico.FaseEnvio
import com.example.myapplication.ui.theme.estadoIngresoColors

// Hoja para enviar el reporte por WhatsApp: qué incluir, vista previa del mensaje y botón de envío.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaEnvioReporte(
    estado: EnvioReporteUiState,
    nombreCliente: String,
    telefono: String,
    cantidadFotos: Int,
    mensaje: String,
    tipos: List<TipoReporte>,
    onCambiarTipo: (TipoReporte) -> Unit,
    onCambiarReporte: (Boolean) -> Unit,
    onCambiarFotos: (Boolean) -> Unit,
    onEnviar: () -> Unit,
    onAbrirChat: () -> Unit,
    onCerrar: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val verde = MaterialTheme.estadoIngresoColors.listoParaFirma
    val ocupado = estado.fase == FaseEnvio.PREPARANDO
    ModalBottomSheet(
        onDismissRequest = { if (!ocupado) onCerrar() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(verde.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = verde) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.send_title, nombreCliente), style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold)
                    Text(telefono, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant) } }

            // Solo se muestran las variantes que corresponden a la pantalla desde la que se envía.
            if (tipos.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tipos.forEach { tipo ->
                        FilterChip(
                            selected = estado.tipo == tipo,
                            onClick = { onCambiarTipo(tipo) },
                            enabled = !ocupado,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary, selectedLabelColor = colors.onPrimary),
                            label = { Text(stringResource(when (tipo) {
                                TipoReporte.COMPLETO -> R.string.send_type_full
                                TipoReporte.COTIZACION -> R.string.send_type_quote
                                TipoReporte.ACTA -> R.string.send_type_delivery }), style = MaterialTheme.typography.labelLarge) }) } } }

            // El PDF siempre va; lo único que se decide es si se agregan las fotos de evidencia.
            OpcionEnvio(stringResource(R.string.send_photos),
                pluralStringResource(R.plurals.send_photos_tag, cantidadFotos, cantidadFotos),
                estado.incluirFotos && cantidadFotos > 0, !ocupado && cantidadFotos > 0, onCambiarFotos)

            // Resumen de lo que se va a enviar, para que quien envía sepa qué recibirá el cliente.
            val conFotos = estado.incluirFotos && cantidadFotos > 0
            Surface(shape = RoundedCornerShape(14.dp), color = colors.primaryContainer) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.send_summary_title), style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold, color = colors.onPrimaryContainer)
                    Text(stringResource(when (estado.tipo) {
                        TipoReporte.COMPLETO -> R.string.send_desc_full
                        TipoReporte.COTIZACION -> R.string.send_desc_quote
                        TipoReporte.ACTA -> R.string.send_desc_delivery }), style = MaterialTheme.typography.bodyMedium,
                        color = colors.onPrimaryContainer)
                    Text(if (conFotos) pluralStringResource(R.plurals.send_desc_photos, cantidadFotos, cantidadFotos)
                        else stringResource(R.string.send_desc_no_photos), style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold, color = colors.onPrimaryContainer) } }

            Surface(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
                color = colors.surfaceVariant) {
                Text(mensaje, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) }

            when (estado.fase) {
                FaseEnvio.ERROR -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = colors.error, modifier = Modifier.size(20.dp))
                    Text(estado.error.orEmpty(), color = colors.error, style = MaterialTheme.typography.bodyMedium) }
                FaseEnvio.LISTO -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = verde, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.send_ready, nombreCliente), color = verde, style = MaterialTheme.typography.bodyMedium) }
                else -> Unit }

            // Para un cliente que no está en los contactos: abrir su chat primero para que aparezca entre los recientes.
            if (!ocupado) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextButton(onClick = onAbrirChat) {
                        Text(stringResource(R.string.send_open_chat), fontWeight = FontWeight.Bold) }
                    Text(stringResource(R.string.send_open_chat_hint), style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant) } }

            if (estado.fase == FaseEnvio.LISTO) {
                OutlinedButton(onClick = onCerrar, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp)) {
                    Text(stringResource(R.string.send_close), style = MaterialTheme.typography.titleMedium) }
            } else {
                Button(
                    onClick = onEnviar,
                    enabled = estado.puedeEnviar && (estado.incluirReporte || (estado.incluirFotos && cantidadFotos > 0)),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = verde)
                ) {
                    if (ocupado) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.onPrimary)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    Text("  " + stringResource(when {
                        ocupado -> R.string.send_preparing
                        estado.fase == FaseEnvio.ERROR -> R.string.send_retry
                        else -> R.string.send_button }), style = MaterialTheme.typography.titleMedium) } } } } }

// Fila con casilla para elegir qué contenido se envía.
@Composable
private fun OpcionEnvio(texto: String, etiqueta: String, marcada: Boolean, habilitada: Boolean, onCambio: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            .clickable(enabled = habilitada, role = Role.Checkbox) { onCambio(!marcada) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = marcada, onCheckedChange = null, enabled = habilitada)
        Text(texto, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        Surface(shape = CircleShape, color = colors.surfaceVariant) {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                color = colors.onSurfaceVariant, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) } } }
