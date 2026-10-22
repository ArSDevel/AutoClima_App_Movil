package com.example.myapplication.ui.diagnostico.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.FormularioDiagnostico
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// Presenta los conceptos y el total de la cotización.
// Los cambios se delegan al ViewModel; aquí no se guarda en la base.
@Composable
fun CotizacionDiagnostico(
    formulario: FormularioDiagnostico,
    onAgregarConcepto: () -> Unit,
    onDescripcionChange: (Int, String) -> Unit,
    onImporteChange: (Int, String) -> Unit,
    onEliminarConcepto: (Int) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    soloLectura: Boolean = false,
    erroresCampos: Map<String, String> = emptyMap()) {
    val colors = MaterialTheme.colorScheme
    val totalCentavos = formulario.totalEnCentavos()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.diagnostico_quote_title).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.8.sp),
                    fontWeight = FontWeight.Bold,
                    color = com.example.myapplication.ui.components.colorTituloSeccion())
                if (!soloLectura) {
                    IconButton(onClick = onAgregarConcepto, enabled = habilitado) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.diagnostico_quote_add),
                            tint = colors.primary) } } }
            if (formulario.conceptos.isEmpty()) {
                Text(text = stringResource(R.string.diagnostico_quote_pending),
                    style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant) }
            formulario.conceptos.forEachIndexed { indice, concepto ->
                if (indice > 0) HorizontalDivider(color = colors.outlineVariant)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top) {
                    OutlinedTextField(
                        value = concepto.descripcion,
                        onValueChange = { onDescripcionChange(indice, it) },
                        modifier = Modifier.weight(1f),
                        enabled = habilitado,
                        readOnly = soloLectura,
                        placeholder = { Text(stringResource(R.string.diagnostico_concept_short), maxLines = 1, softWrap = false) },
                        isError = erroresCampos["conceptos.$indice.descripcion"] != null,
                        supportingText = erroresCampos["conceptos.$indice.descripcion"]?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next),
                        maxLines = 2,
                        shape = MaterialTheme.shapes.small)
                    OutlinedTextField(
                        value = concepto.importe,
                        onValueChange = { onImporteChange(indice, it) },
                        modifier = Modifier.width(124.dp),
                        enabled = habilitado,
                        readOnly = soloLectura,
                        prefix = { Text(stringResource(R.string.diagnostico_currency_symbol)) },
                        placeholder = { Text("0.00") },
                        isError = erroresCampos["conceptos.$indice.importe"] != null,
                        supportingText = erroresCampos["conceptos.$indice.importe"]?.let { { Text("!") } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small)
                    if (!soloLectura) {
                        IconButton(onClick = { onEliminarConcepto(indice) }, enabled = habilitado) {
                            Icon(Icons.Default.Close,
                                contentDescription = stringResource(R.string.diagnostico_concept_remove, indice + 1),
                                tint = colors.onSurfaceVariant) } } }
                erroresCampos["conceptos.$indice.importe"]?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = colors.error) } }
            erroresCampos["conceptos.total"]?.let { mensaje ->
                Text(text = mensaje, style = MaterialTheme.typography.bodySmall, color = colors.error) }
            if (formulario.tieneCotizacion) {
                HorizontalDivider(color = colors.outline, thickness = 2.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.diagnostico_quote_total),
                        style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (totalCentavos != null) formatearImporte(totalCentavos) else "—",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold) }
                // Si falta algún importe, el aviso va aparte y en pequeño.
                if (totalCentavos == null) {
                    Text(
                        text = stringResource(R.string.diagnostico_quote_invalid_total),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant) } } } } }

// Formatea centavos como pesos mexicanos sin convertir a Double; equivale a FormatoReporte.pesos.
private fun formatearImporte(centavos: Long): String {
    val formato = NumberFormat.getCurrencyInstance(
        Locale("es", "MX")
    ).apply {
        currency = Currency.getInstance("MXN")
        minimumFractionDigits = 2
        maximumFractionDigits = 2 }

    return formato.format(
        BigDecimal.valueOf(centavos, 2)) }