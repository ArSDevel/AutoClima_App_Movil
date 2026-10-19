package com.example.myapplication.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.ui.components.PildoraEstado
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.DateFormat
import java.util.Date

// Tarjeta compacta de un ingreso.
// Al tocarla se abre el detalle. Las fotos y la edición tienen su icono.
@Composable
fun IngresoCard(
    ingreso: IngresoResumen,
    modifier: Modifier = Modifier,
    onVerDetalle: ((Long) -> Unit)? = null,
    onVerFotos: ((Long) -> Unit)? = null,
    onEditar: ((Long) -> Unit)? = null,
    onAbrirChecklist: ((Long) -> Unit)? = null,
    onAbrirDiagnostico: ((Long) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val estado = EstadoIngreso.desdeValor(ingreso.estado)
    val locale = LocalConfiguration.current.locales[0]
    val fechaFormateada = remember(ingreso.fechaEntrada, locale) {
        DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(ingreso.fechaEntrada))
    }
    val vehiculo = listOfNotNull(
        ingreso.modelo.takeIf { it.isNotBlank() },
        ingreso.color?.takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    Card(
        onClick = { onVerDetalle?.invoke(ingreso.ingresoId) },
        enabled = onVerDetalle != null,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = colors.surface,
            contentColor = colors.onSurface,
            disabledContainerColor = colors.surface,
            disabledContentColor = colors.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FotoIngresoCard(ruta = ingreso.fotoPath)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = ingreso.placa,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = vehiculo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    PildoraEstado(estado = estado)
                }
                Column {
                    if (onVerFotos != null) {
                        IconButton(onClick = { onVerFotos(ingreso.ingresoId) }) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = stringResource(R.string.fotos_card_action),
                                tint = colors.primary
                            )
                        }
                    }
                    if (onEditar != null) {
                        IconButton(onClick = { onEditar(ingreso.ingresoId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.dashboard_action_edit),
                                tint = colors.primary
                            )
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    DatoBreve(icono = Icons.Default.Person, texto = ingreso.nombreCliente)
                    DatoBreve(icono = Icons.Default.DateRange, texto = fechaFormateada)
                }
                // Siguiente paso del ingreso: checklist en revisión, diagnóstico en reparación.
                val (textoAccion, abrirAccion) = when {
                    estado == EstadoIngreso.EN_REVISION && onAbrirChecklist != null ->
                        R.string.dashboard_action_checklist to onAbrirChecklist
                    estado == EstadoIngreso.EN_REPARACION && onAbrirDiagnostico != null ->
                        R.string.dashboard_action_diagnosis to onAbrirDiagnostico
                    else -> R.string.dashboard_card_view_detail to onVerDetalle
                }
                if (abrirAccion != null) {
                    Button(
                        onClick = { abrirAccion(ingreso.ingresoId) },
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(start = 16.dp, end = 10.dp)
                    ) {
                        Text(text = stringResource(textoAccion), style = MaterialTheme.typography.labelLarge)
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// Dato con icono y sin etiqueta.
@Composable
private fun DatoBreve(icono: ImageVector, texto: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Datos exclusivos de las previews.
private val ingresoEjemplo = IngresoResumen(
    ingresoId = 1L,
    placa = "ABC-123",
    modelo = "Honda Civic",
    color = "Rojo",
    numeroSerie = "SERIE-DE-EJEMPLO",
    nombreCliente = "Cliente de ejemplo",
    telefonoCliente = "8100000000",
    emailCliente = null,
    fechaEntrada = 1788955200000L,
    estado = EstadoIngreso.EN_REVISION.name
)

@Preview(name = "Ingreso · claro", showBackground = true, widthDp = 360)
@Composable
private fun IngresoCardClaroPreview() {
    MyApplicationTheme(darkTheme = false) {
        IngresoCard(
            ingreso = ingresoEjemplo,
            modifier = Modifier.padding(16.dp),
            onVerDetalle = {},
            onVerFotos = {},
            onEditar = {}
        )
    }
}

@Preview(name = "Ingreso · oscuro", showBackground = true, widthDp = 360)
@Composable
private fun IngresoCardOscuraPreview() {
    MyApplicationTheme(darkTheme = true) {
        IngresoCard(
            ingreso = ingresoEjemplo,
            modifier = Modifier.padding(16.dp),
            onVerDetalle = {},
            onVerFotos = {},
            onEditar = {}
        )
    }
}
