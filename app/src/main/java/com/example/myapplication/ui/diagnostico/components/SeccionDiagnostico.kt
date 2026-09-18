package com.example.myapplication.ui.diagnostico.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

// Contenedor reutilizable para las secciones del diagnóstico.
// Organiza la presentación sin consultar ni modificar datos.
@Composable
fun SeccionDiagnostico(
    titulo: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    descripcion: String? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        contentColor = colors.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = colors.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado de la sección.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = colors.primaryContainer,
                    contentColor = colors.onPrimaryContainer
                ) {
                    Icon(imageVector = icono,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp))
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = titulo,
                        style = typography.titleMedium,
                        modifier = Modifier.semantics {
                            heading()
                        })
                    if (!descripcion.isNullOrBlank()) {
                        Text(text = descripcion,
                            style = typography.bodyMedium,
                            color = colors.onSurfaceVariant)
                    }
                }
            }
            HorizontalDivider(color = colors.outlineVariant)
            // Datos o campos correspondientes a esta sección.
            contenido()
        }
    }
}