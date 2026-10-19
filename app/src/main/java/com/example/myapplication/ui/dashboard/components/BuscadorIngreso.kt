package com.example.myapplication.ui.dashboard.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.foundation.background

// Campo de búsqueda con sombra y botón para borrar el texto.
@Composable
fun BuscadorIngreso(
    textoBusqueda: String,
    onBusquedaChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textoGuia: String? = null
) {
    val colors = MaterialTheme.colorScheme

    OutlinedTextField(
        value = textoBusqueda,
        onValueChange = onBusquedaChange,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp)
            .shadow(elevation = 6.dp, shape = MaterialTheme.shapes.medium),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = {
            Text(text = textoGuia ?: stringResource(R.string.dashboard_search_placeholder),
                style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null) },
        trailingIcon = {
            if (textoBusqueda.isNotEmpty()) {
                IconButton(onClick = { onBusquedaChange("") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.dashboard_clear_search)) } } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.onSurfaceVariant,
            focusedPlaceholderColor = colors.onSurfaceVariant,
            unfocusedPlaceholderColor = colors.onSurfaceVariant,
            focusedLeadingIconColor = colors.primary,
            unfocusedLeadingIconColor = colors.onSurfaceVariant,
            focusedTrailingIconColor = colors.onSurfaceVariant,
            unfocusedTrailingIconColor = colors.onSurfaceVariant,
            cursorColor = colors.primary)) }

// Vista previa del buscador.
@Preview(
    name = "Buscador de ingresos",
    showBackground = true)
@Composable
private fun BuscadorIngresoPreview() {
    var texto by rememberSaveable {
        mutableStateOf("") }
    MyApplicationTheme(darkTheme = false) {
        BuscadorIngreso(
            textoBusqueda = texto,
            onBusquedaChange = { nuevoTexto -> texto = nuevoTexto },
            modifier = Modifier.padding(16.dp)) } }