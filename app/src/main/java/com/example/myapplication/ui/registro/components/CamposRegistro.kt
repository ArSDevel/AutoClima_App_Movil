package com.example.myapplication.ui.registro.components

import android.app.DatePickerDialog
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.FormularioIngreso
import java.util.Calendar

/*
 * Utiliza las mismas entradas en una o dos columnas.
 * La distribución depende del ancho, no de la orientación.
 */
@Composable
internal fun CamposAdaptables(
    primero: @Composable () -> Unit,
    segundo: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (maxWidth >= 560.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    primero()
                }

                Box(Modifier.weight(1f)) {
                    segundo()
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                primero()
                segundo()
            }
        }
    }
}

/*
 * Etiqueta externa y campo Material 3.
 * El botón de limpiar conserva un área táctil estándar.
 */
@Composable
internal fun CampoRegistroTexto(
    @StringRes etiqueta: Int,
    valor: String,
    onCambio: (String) -> Unit,
    habilitado: Boolean,
    error: String? = null,
    obligatorio: Boolean = false,
    multilinea: Boolean = false,
    tipo: KeyboardType = KeyboardType.Text
) {
    val nombre = stringResource(etiqueta)

    val textoEtiqueta = if (obligatorio) {
        stringResource(R.string.registro_required_label, nombre)
    } else {
        nombre
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = textoEtiqueta,
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            singleLine = !multilinea,
            minLines = if (multilinea) 3 else 1,
            isError = error != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = tipo
            ),
            trailingIcon = {
                if (valor.isNotEmpty() && habilitado) {
                    IconButton(
                        onClick = { onCambio("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(
                                R.string.registro_clear_field,
                                nombre
                            )
                        )
                    }
                }
            }
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
internal fun FechaRegistro(
    valor: String,
    error: String?,
    habilitado: Boolean,
    onCambio: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(
                R.string.registro_required_label,
                stringResource(R.string.dashboard_entry_date)
            ),
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedButton(
            enabled = habilitado,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            onClick = {
                val calendario = Calendar.getInstance()

                FormularioIngreso.interpretarFecha(valor)?.let {
                    calendario.time = it
                }

                DatePickerDialog(
                    context,
                    { _, anio, mes, dia ->
                        val seleccionada = Calendar.getInstance().apply {
                            clear()
                            set(anio, mes, dia)
                        }

                        onCambio(
                            FormularioIngreso.formatearFecha(
                                seleccionada.timeInMillis
                            )
                        )
                    },
                    calendario.get(Calendar.YEAR),
                    calendario.get(Calendar.MONTH),
                    calendario.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = valor.ifBlank {
                    stringResource(R.string.registro_select_date)
                }
            )
        }

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}