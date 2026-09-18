package com.example.myapplication.ui.diagnostico.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    erroresCampos: Map<String, String> = emptyMap()
) {
    val colors = MaterialTheme.colorScheme
    val totalCentavos = formulario.totalEnCentavos()
    SeccionDiagnostico(
        titulo = stringResource(R.string.diagnostico_quote_title),
        descripcion = stringResource(R.string.diagnostico_quote_description),
        icono = Icons.Default.List,
        modifier = modifier
    ) {
        if (formulario.conceptos.isEmpty()) {
            Text(text = stringResource(R.string.diagnostico_quote_pending),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        } else {
            formulario.conceptos.forEachIndexed { indice, concepto ->
                ConceptoCotizacionItem(
                    numero = indice + 1,
                    concepto = concepto,
                    onDescripcionChange = { valor ->
                        onDescripcionChange(indice, valor) },
                    onImporteChange = { valor ->
                        onImporteChange(indice, valor) },
                    onEliminar = {
                        onEliminarConcepto(indice) },
                    habilitado = habilitado,
                    soloLectura = soloLectura,
                    errorDescripcion = erroresCampos[
                        "conceptos.$indice.descripcion"],
                    errorImporte = erroresCampos[
                        "conceptos.$indice.importe"]
                )
            }
        }
        // En modo de consulta no se ofrecen cambios a la cotización.
        if (!soloLectura) {
            OutlinedButton(
                onClick = onAgregarConcepto,
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Add,
                    contentDescription = null)
                Text(text = stringResource(R.string.diagnostico_quote_add),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Presenta errores generales, como un total fuera de rango.
        erroresCampos["conceptos.total"]?.let { mensaje ->
            Text(text = mensaje, style = MaterialTheme.typography.bodySmall,
                color = colors.error)
        }

        // No muestra cero como si fuera una cotización terminada
        // cuando todavía no se han agregado conceptos.
        if (formulario.tieneCotizacion) {
            Surface(modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = stringResource(R.string.diagnostico_quote_total),
                        style = MaterialTheme.typography.titleSmall)
                    if (totalCentavos != null) {
                        Text(text = stringResource(
                                R.string.diagnostico_quote_total_amount,
                                formatearImporte(totalCentavos)),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    } else {
                        Text(text = stringResource(
                                R.string.diagnostico_quote_invalid_total),
                            style = MaterialTheme.typography.bodyLarge) }
                    Text(text = stringResource(
                            R.string.diagnostico_quote_total_help),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// Formatea centavos como pesos mexicanos sin convertir a Double.
private fun formatearImporte(centavos: Long): String {
    val formato = NumberFormat.getCurrencyInstance(
        Locale("es", "MX")
    ).apply {
        currency = Currency.getInstance("MXN")
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    return formato.format(
        BigDecimal.valueOf(centavos, 2)
    )
}