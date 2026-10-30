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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    contenido: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Título breve de la sección; la descripción y el contador ya no se muestran.
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(22.dp))
            Text(
                text = titulo.uppercase(),
                style = typography.labelLarge.copy(letterSpacing = 0.8.sp),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = com.example.myapplication.ui.components.colorTituloSeccion(),
                modifier = Modifier.semantics { heading() }) }

        // Aquí se colocan las preguntas de la sección.
        contenido() } }