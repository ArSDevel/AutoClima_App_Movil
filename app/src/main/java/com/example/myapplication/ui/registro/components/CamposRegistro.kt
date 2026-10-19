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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    primero() }

                Box(Modifier.weight(1f)) {
                    segundo() } }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                primero()
                segundo() } } } }

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
    tipo: KeyboardType = KeyboardType.Text,
    destacado: Boolean = false,
    contenidoFinal: (@Composable () -> Unit)? = null,
    ayuda: String? = null
) {
    val nombre = stringResource(etiqueta)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Los campos opcionales se marcan con una palabra; los demás no necesitan marca.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))
            if (!obligatorio) {
                Text(
                    text = stringResource(R.string.registro_optional),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) } }

        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth().heightIn(min = if (multilinea) 120.dp else 64.dp),
            shape = MaterialTheme.shapes.small,
            textStyle = if (destacado) {
                MaterialTheme.typography.titleLarge.copy(letterSpacing = 1.sp)
            } else {
                MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp) },
            singleLine = !multilinea,
            minLines = if (multilinea) 3 else 1,
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant),
            keyboardOptions = KeyboardOptions(
                keyboardType = tipo),
            trailingIcon = {
                if (contenidoFinal != null) {
                    contenidoFinal()
                } else if (valor.isNotEmpty() && habilitado) {
                    IconButton(
                        onClick = { onCambio("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(
                                R.string.registro_clear_field,
                                nombre)) } } })

        if (ayuda != null && error == null) {
            Text(
                text = ayuda,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall) } } }

// Campo de fecha que abre el selector de calendario.
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
            text = stringResource(R.string.dashboard_entry_date),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))

        OutlinedButton(
            enabled = habilitado,
            shape = MaterialTheme.shapes.small,
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            onClick = {
                val calendario = Calendar.getInstance()

                FormularioIngreso.interpretarFecha(valor)?.let {
                    calendario.time = it }

                DatePickerDialog(
                    context,
                    { _, anio, mes, dia ->
                        val seleccionada = Calendar.getInstance().apply {
                            clear()
                            set(anio, mes, dia) }

                        onCambio(
                            FormularioIngreso.formatearFecha(
                                seleccionada.timeInMillis)) },
                    calendario.get(Calendar.YEAR),
                    calendario.get(Calendar.MONTH),
                    calendario.get(Calendar.DAY_OF_MONTH)
                ).show() }
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.width(12.dp))

            Text(
                text = valor.ifBlank {
                    stringResource(R.string.registro_select_date) },
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                modifier = Modifier.weight(1f)) }

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall) } } }