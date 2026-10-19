package com.example.myapplication.ui.historial.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EventoHistorial
import com.example.myapplication.ui.theme.estadoIngresoColors
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

// Actividad en una sola línea; al tocarla se despliegan la nota y el acceso al ingreso.
@Composable
fun EventoHistorialCard(evento: EventoHistorial, onAbrirIngreso: (Long) -> Unit) {
    var expandido by rememberSaveable(evento.eventoId) { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val estados = MaterialTheme.estadoIngresoColors
    val fecha = fechaRelativa(evento.fecha)
    val tecnico = nombreCorto(evento.nombreTecnico)
        ?: stringResource(R.string.historial_actor_unknown, evento.tecnicoId)

    val (icono, color) = when (evento.accion) {
        "ESTADO" -> Icons.AutoMirrored.Filled.ArrowForward to estados.enRevision
        "EVIDENCIA", "FOTO_PRINCIPAL", "EVIDENCIA_ELIMINADA" -> Icons.Default.CameraAlt to estados.listoParaEntrega
        "CHECKLIST" -> Icons.Default.Checklist to estados.registrado
        "DIAGNOSTICO" -> Icons.Default.Description to estados.enReparacion
        "EDITADO" -> Icons.Default.Edit to estados.registrado
        else -> Icons.Default.History to estados.registrado
    }
    // Un cambio de estado se titula con el cambio mismo: "Revisión → Reparación".
    val titulo = if (evento.accion == "ESTADO" && evento.estadoNuevo.isNotBlank() && evento.estadoAnterior.isNotBlank()) {
        stringResource(R.string.detalle_event_transition,
            stringResource(etiquetaEstadoHistorial(evento.estadoAnterior)),
            stringResource(etiquetaEstadoHistorial(evento.estadoNuevo)))
    } else stringResource(etiquetaAccionHistorial(evento.accion))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().clickable { expandido = !expandido }.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icono, contentDescription = null, tint = color)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = titulo, style = MaterialTheme.typography.titleSmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = if (expandido) "${evento.placa} · $fecha" else "${evento.placa} · $fecha · $tecnico",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = stringResource(if (expandido) R.string.historial_less else R.string.historial_details),
                    tint = colors.onSurfaceVariant
                )
            }
            if (expandido) {
                if (evento.motivo.isNotBlank()) {
                    Surface(shape = MaterialTheme.shapes.small, color = colors.surfaceVariant) {
                        Text(
                            text = evento.motivo,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp),
                            tint = colors.onSurfaceVariant)
                        Text(tecnico, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    TextButton(onClick = { onAbrirIngreso(evento.ingresoId) }) {
                        Text(stringResource(R.string.historial_open_short), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// "Jesús Martínez Pérez" → "Jesús M."
private fun nombreCorto(nombre: String?): String? {
    val partes = nombre?.trim()?.split(" ")?.filter { it.isNotBlank() } ?: return null
    if (partes.isEmpty()) return null
    return if (partes.size == 1) partes[0] else "${partes[0]} ${partes[1].first()}."
}

@Composable
private fun fechaRelativa(milisegundos: Long): String {
    val ahora = Calendar.getInstance()
    val evento = Calendar.getInstance().apply { timeInMillis = milisegundos }
    val hora = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(milisegundos))
    val dias = (ahora.get(Calendar.YEAR) * 366 + ahora.get(Calendar.DAY_OF_YEAR)) -
            (evento.get(Calendar.YEAR) * 366 + evento.get(Calendar.DAY_OF_YEAR))
    return when (dias) {
        0 -> stringResource(R.string.historial_today) + " " + hora
        1 -> stringResource(R.string.historial_yesterday) + " " + hora
        else -> DateFormat.getDateInstance(DateFormat.SHORT).format(Date(milisegundos)) + " " + hora
    }
}
