package com.example.myapplication.ui.checklist.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.estadoIngresoColors

// Pregunta reutilizable del checklist.
// null representa una pregunta que todavía no se responde.
// La pregunta y las dos respuestas comparten una sola fila.
@Composable
fun PreguntaChecklist(
    numero: Int,
    pregunta: String,
    respuesta: Boolean?,
    onRespuestaChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    habilitada: Boolean = true,
    mostrarPendiente: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    val pendiente = mostrarPendiente && respuesta == null

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colors.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (pendiente) colors.error else colors.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$numero. $pregunta",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() })
                // Las dos opciones forman un grupo de selección única.
                Row(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OpcionRespuestaChecklist(
                        descripcion = stringResource(R.string.checklist_answer_yes),
                        icono = Icons.Default.Check,
                        colorSeleccion = MaterialTheme.estadoIngresoColors.listoParaFirma,
                        seleccionada = respuesta == true,
                        habilitada = habilitada,
                        onClick = { onRespuestaChange(true) })
                    OpcionRespuestaChecklist(
                        descripcion = stringResource(R.string.checklist_answer_no),
                        icono = Icons.Default.Close,
                        colorSeleccion = colors.error,
                        seleccionada = respuesta == false,
                        habilitada = habilitada,
                        onClick = { onRespuestaChange(false) }) } }
            if (pendiente) {
                Text(
                    text = stringResource(R.string.checklist_answer_required),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error) } } } }

// Conserva visible la selección incluso en modo de consulta.
// selectable comunica a accesibilidad si está seleccionada
// y si permite interacción.
@Composable
private fun OpcionRespuestaChecklist(
    descripcion: String,
    icono: ImageVector,
    colorSeleccion: Color,
    seleccionada: Boolean,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (seleccionada) colorSeleccion else colors.surfaceVariant,
        contentColor = if (seleccionada) colors.surface else colors.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .selectable(
                    selected = seleccionada,
                    enabled = habilitada,
                    role = Role.RadioButton,
                    onClick = onClick)
                .heightIn(min = 48.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = descripcion,
                modifier = Modifier.size(24.dp)) } } }
