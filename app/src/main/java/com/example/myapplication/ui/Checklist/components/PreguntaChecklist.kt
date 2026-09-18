package com.example.myapplication.ui.checklist.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

// Pregunta reutilizable del checklist.
// null representa una pregunta que todavía no se responde.
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
            color = if (pendiente) {
                colors.error
            } else {
                colors.outlineVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Número y pregunta. El texto puede ocupar varias líneas.
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = colors.primaryContainer,
                    contentColor = colors.onPrimaryContainer
                ) {
                    Text(
                        text = numero.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 8.dp
                        )
                    )
                }

                Text(
                    text = pregunta,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
            }

            // Las dos opciones forman un grupo de selección única.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OpcionRespuestaChecklist(
                    texto = stringResource(R.string.checklist_answer_yes),
                    seleccionada = respuesta == true,
                    habilitada = habilitada,
                    onClick = { onRespuestaChange(true) },
                    modifier = Modifier.weight(1f)
                )

                OpcionRespuestaChecklist(
                    texto = stringResource(R.string.checklist_answer_no),
                    seleccionada = respuesta == false,
                    habilitada = habilitada,
                    onClick = { onRespuestaChange(false) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (pendiente) {
                Text(
                    text = stringResource(
                        R.string.checklist_answer_required
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error
                )
            }
        }
    }
}

// Conserva visible la selección incluso en modo de consulta.
// selectable comunica a accesibilidad si está seleccionada
// y si permite interacción.
@Composable
private fun OpcionRespuestaChecklist(
    texto: String,
    seleccionada: Boolean,
    habilitada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (seleccionada) {
            colors.primaryContainer
        } else {
            colors.surface
        },
        contentColor = if (seleccionada) {
            colors.onPrimaryContainer
        } else {
            colors.onSurfaceVariant
        },
        border = BorderStroke(
            width = if (seleccionada) 2.dp else 1.dp,
            color = if (seleccionada) {
                colors.primary
            } else {
                colors.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .selectable(
                    selected = seleccionada,
                    enabled = habilitada,
                    role = Role.RadioButton,
                    onClick = onClick
                )
                .heightIn(min = 48.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}