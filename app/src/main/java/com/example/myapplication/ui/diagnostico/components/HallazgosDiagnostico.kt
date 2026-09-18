package com.example.myapplication.ui.diagnostico.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.diagnostico.HallazgoDiagnostico

// Presenta los resultados del checklist.
// No interpreta un checklist ausente como una revisión sin problemas.
@Composable
fun HallazgosDiagnostico(
    tieneChecklist: Boolean,
    hallazgos: List<HallazgoDiagnostico>,
    comentariosChecklist: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    SeccionDiagnostico(
        titulo = stringResource(R.string.diagnostico_findings_title),
        descripcion = stringResource(
            R.string.diagnostico_findings_description
        ),
        icono = Icons.Default.Warning,
        modifier = modifier
    ) {
        when {
            !tieneChecklist -> {
                MensajeHallazgos(
                    texto = stringResource(
                        R.string.diagnostico_checklist_pending
                    )
                )
            }

            hallazgos.isEmpty() -> {
                MensajeHallazgos(
                    texto = stringResource(
                        R.string.diagnostico_findings_empty
                    )
                )
            }

            else -> {
                Text(
                    text = stringResource(
                        R.string.diagnostico_findings_count,
                        hallazgos.size
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant
                )

                hallazgos.forEachIndexed { indice, hallazgo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = colors.secondaryContainer,
                            contentColor = colors.onSecondaryContainer
                        ) {
                            Text(
                                text = (indice + 1).toString(),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                )
                            )
                        }

                        Text(
                            text = stringResource(
                                descripcionHallazgo(hallazgo)
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Los comentarios originales se muestran como consulta.
        // Las notas adicionales se escribirán en otra sección.
        if (tieneChecklist) {
            HorizontalDivider(color = colors.outlineVariant)

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.diagnostico_checklist_comments_title
                    ),
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = comentariosChecklist.trim().ifEmpty {
                        stringResource(
                            R.string.diagnostico_checklist_comments_empty
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

// Mensaje informativo para revisión pendiente o sin hallazgos.
@Composable
private fun MensajeHallazgos(
    texto: String
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(24.dp))
            Text(text = texto,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f))
        }
    }
}

// Traduce cada hallazgo a un recurso de texto.
// La interpretación de las respuestas se realiza en DiagnosticoUiState.
@StringRes
private fun descripcionHallazgo(
    hallazgo: HallazgoDiagnostico
): Int {
    return when (hallazgo) {
        HallazgoDiagnostico.FALTA_REFRIGERANTE ->
            R.string.diagnostico_finding_refrigerant

        HallazgoDiagnostico.FUGA_VISIBLE_MANGUERAS ->
            R.string.diagnostico_finding_hoses

        HallazgoDiagnostico.FUGA_DETECTADA_UV ->
            R.string.diagnostico_finding_uv

        HallazgoDiagnostico.COMPRESOR_NO_EMBRAGA ->
            R.string.diagnostico_finding_engagement

        HallazgoDiagnostico.COMPRESOR_DANADO ->
            R.string.diagnostico_finding_compressor

        HallazgoDiagnostico.BANDA_DESGASTADA ->
            R.string.diagnostico_finding_belt

        HallazgoDiagnostico.VENTILADORES_NO_FUNCIONAN ->
            R.string.diagnostico_finding_fans

        HallazgoDiagnostico.FILTRO_OBSTRUIDO ->
            R.string.diagnostico_finding_filter

        HallazgoDiagnostico.CONDENSADOR_OBSTRUIDO ->
            R.string.diagnostico_finding_condenser
    }
}