package com.example.myapplication.ui.diagnostico.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.ConceptoCotizacionFormulario

// Fila editable de la cotización.
// Los errores corresponden a este concepto, no a toda la lista.
@Composable
fun ConceptoCotizacionItem(
    numero: Int,
    concepto: ConceptoCotizacionFormulario,
    onDescripcionChange: (String) -> Unit,
    onImporteChange: (String) -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    soloLectura: Boolean = false,
    errorDescripcion: String? = null,
    errorImporte: String? = null
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colors.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (
                errorDescripcion != null || errorImporte != null
            ) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.diagnostico_concept_number,
                        numero
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // En modo de consulta no se ofrece eliminar.
                if (!soloLectura) {
                    IconButton(
                        onClick = onEliminar,
                        enabled = habilitado
                    ) {
                        Icon(imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(
                                R.string.diagnostico_concept_remove,
                                numero
                            ),
                            tint = if (habilitado) {
                                colors.error
                            } else {
                                colors.onSurface.copy(alpha = 0.38f)
                            })
                    }
                }
            }
            OutlinedTextField(
                value = concepto.descripcion,
                onValueChange = onDescripcionChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = habilitado,
                readOnly = soloLectura,
                label = {
                    Text(stringResource(R.string.diagnostico_concept_description)) },
                placeholder = {
                    Text(stringResource(R.string.diagnostico_concept_description_hint))
                },
                isError = errorDescripcion != null,
                supportingText = {
                    if (errorDescripcion != null) {
                        Text(errorDescripcion) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization =
                        androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next),
                minLines = 1,
                maxLines = 3,
                shape = MaterialTheme.shapes.small)
            OutlinedTextField(
                value = concepto.importe,
                onValueChange = onImporteChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = habilitado,
                readOnly = soloLectura,
                label = { Text(stringResource(R.string.diagnostico_concept_amount)) },
                prefix = { Text(stringResource(R.string.diagnostico_currency_symbol)) },
                suffix = { Text(stringResource(R.string.diagnostico_currency_code)) },
                placeholder = { Text(stringResource(R.string.diagnostico_concept_amount_hint)) },
                isError = errorImporte != null,
                supportingText = {
                    Text(text = errorImporte ?:
                    stringResource(R.string.diagnostico_concept_amount_help))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done),
                singleLine = true,
                shape = MaterialTheme.shapes.small
            )
        }
    }
}