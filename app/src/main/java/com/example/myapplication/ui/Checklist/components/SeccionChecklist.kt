package com.example.myapplication.ui.checklist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

// Agrupa las preguntas de un sistema del vehículo.
// El contador recibe el número de respuestas contestadas,
// incluyendo tanto "Sí" como "No".
@Composable
fun SeccionChecklist(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    preguntasRespondidas: Int,
    totalPreguntas: Int,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Encabezado visual de la sección.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(28.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = titulo,
                            style = typography.titleMedium,
                            modifier = Modifier.semantics {
                                heading()
                            }
                        )

                        Text(
                            text = descripcion,
                            style = typography.bodyMedium
                        )
                    }
                }

                // El contador ocupa una línea independiente
                // para permitir textos grandes y pantallas estrechas.
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = colors.surface,
                    contentColor = colors.onSurface
                ) {
                    Text(
                        text = stringResource(
                            R.string.checklist_section_progress,
                            preguntasRespondidas,
                            totalPreguntas
                        ),
                        style = typography.labelMedium,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                    )
                }
            }
        }

        // Aquí se colocan las preguntas de la sección.
        contenido()
    }
}