package com.example.myapplication.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.ui.dashboard.IngresoAccion
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.estadoIngresoColors
import java.text.DateFormat
import java.util.Date
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngresoCard(
    ingreso: IngresoResumen,
    modifier: Modifier = Modifier,
    acciones: List<IngresoAccion> = emptyList(),
    onAccion: ((IngresoAccion, Long) -> Unit)? = null,
    onVerDetalle: ((Long) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val estadoColors = MaterialTheme.estadoIngresoColors
    val estado = EstadoIngreso.desdeValor(ingreso.estado)
    val locale = LocalConfiguration.current.locales[0]
    var expandido by rememberSaveable(ingreso.ingresoId) { mutableStateOf(false) }

    val colorEstado = when (estado) {
        EstadoIngreso.REGISTRADO -> estadoColors.registrado
        EstadoIngreso.EN_REVISION -> estadoColors.enRevision
        EstadoIngreso.EN_REPARACION -> estadoColors.enReparacion
        EstadoIngreso.LISTO_PARA_FIRMA -> estadoColors.listoParaFirma
        EstadoIngreso.LISTO_PARA_ENTREGA -> estadoColors.listoParaEntrega
        EstadoIngreso.ENTREGADO -> estadoColors.entregado
        EstadoIngreso.CANCELADO -> estadoColors.cancelado
        EstadoIngreso.NO_DISPONIBLE -> estadoColors.noDisponible
    }

    val textoEstadoRes = when (estado) {
        EstadoIngreso.REGISTRADO -> R.string.dashboard_status_registered
        EstadoIngreso.EN_REVISION -> R.string.dashboard_status_review
        EstadoIngreso.EN_REPARACION -> R.string.dashboard_status_repair
        EstadoIngreso.LISTO_PARA_FIRMA -> R.string.dashboard_status_signature
        EstadoIngreso.LISTO_PARA_ENTREGA -> R.string.dashboard_status_delivery
        EstadoIngreso.ENTREGADO -> R.string.dashboard_status_delivered
        EstadoIngreso.CANCELADO -> R.string.dashboard_status_cancelled
        EstadoIngreso.NO_DISPONIBLE -> R.string.dashboard_status_unknown
    }

    val fechaFormateada = remember(ingreso.fechaEntrada, locale) {
        DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
            .format(Date(ingreso.fechaEntrada))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surface,
            contentColor = colors.onSurface),
        border = BorderStroke(2.dp, colorEstado),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identificación principal del vehículo.
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = stringResource(R.string.dashboard_plate),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant)
                Text(text = ingreso.placa,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.primary)
                Text(text = ingreso.modelo,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface)
            }
            // El estado se comunica mediante color y texto.
            Surface(color = colorEstado.copy(alpha = 0.12f),
                contentColor = colorEstado,
                shape = MaterialTheme.shapes.small
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colorEstado))
                    Text(text = stringResource(textoEstadoRes),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(weight = 1f, fill = false))
                }
            }
            HorizontalDivider(color = colors.outlineVariant)
            // Información esencial siempre visible.
            DatosPrincipales(ingreso = ingreso, fechaFormateada = fechaFormateada)
            // Información secundaria desplegable.
            AnimatedVisibility(visible = expandido) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = colors.outlineVariant)
                    DatoIngreso(etiqueta = stringResource(R.string.dashboard_color), valor = ingreso.color)
                    DatoIngreso(etiqueta = stringResource(R.string.dashboard_serial_number), valor = ingreso.numeroSerie)
                    DatoIngreso(etiqueta = stringResource(R.string.dashboard_email), valor = ingreso.emailCliente)
                }
            }
            // Las opciones se acomodan según el espacio disponible.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                onVerDetalle?.let { abrirDetalle -> FilledTonalButton(
                    onClick = { abrirDetalle(ingreso.ingresoId) }
                    ) { Text(text = stringResource(R.string.dashboard_card_view_detail))
                        Spacer(Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
                TextButton(onClick = { expandido = !expandido }
                ) {
                    Text(text = stringResource(if (expandido) {
                        R.string.dashboard_card_less_data
                    } else {
                        R.string.dashboard_card_more_data
                    }))
                    Spacer(Modifier.width(6.dp))
                    Icon(imageVector = if (expandido) {
                        Icons.Default.ExpandLess
                    } else {
                        Icons.Default.ExpandMore
                           }, contentDescription = null)
                }
            }
            if (acciones.isNotEmpty()) {
                HorizontalDivider(color = colors.outlineVariant)
                AccionesIngreso(acciones = acciones, habilitadas = onAccion != null,
                    onAccion = { accion -> onAccion?.invoke(accion, ingreso.ingresoId)})
            }
        }
    }
}

@Composable
private fun DatosPrincipales(
    ingreso: IngresoResumen,
    fechaFormateada: String
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()
    ) {
        if (maxWidth >= 520.dp) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DatoIngreso(etiqueta = stringResource(R.string.dashboard_customer), valor = ingreso.nombreCliente)
                    DatoIngreso(etiqueta = stringResource(R.string.dashboard_phone), valor = ingreso.telefonoCliente)
                }
                DatoIngreso(etiqueta = stringResource(R.string.dashboard_entry_date), valor = fechaFormateada, modifier = Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DatoIngreso(etiqueta = stringResource(R.string.dashboard_customer), valor = ingreso.nombreCliente)
                DatoIngreso(etiqueta = stringResource(R.string.dashboard_phone), valor = ingreso.telefonoCliente)
                DatoIngreso(etiqueta = stringResource(R.string.dashboard_entry_date), valor = fechaFormateada)
            }
        }
    }
}

@Composable
private fun DatoIngreso(
    etiqueta: String,
    valor: String?,
    modifier: Modifier = Modifier
) {
    val texto = valor?.takeIf { it.isNotBlank() } ?:
    stringResource(R.string.dashboard_not_registered)

    Column(modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(text = etiqueta, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = texto, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccionesIngreso(
    acciones: List<IngresoAccion>,
    habilitadas: Boolean,
    onAccion: (IngresoAccion) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        acciones.distinct().forEach { accion ->
            val (textoRes, icono) = when (accion) {
                IngresoAccion.VER_PDF -> R.string.dashboard_action_pdf to Icons.Default.PictureAsPdf
                IngresoAccion.INICIAR_REVISION -> R.string.dashboard_action_start_review to Icons.Default.PlayArrow
                IngresoAccion.CHECKLIST -> R.string.dashboard_action_checklist to Icons.Default.Checklist
                IngresoAccion.EVIDENCIAS -> R.string.dashboard_action_evidence to Icons.Default.CameraAlt
                IngresoAccion.DIAGNOSTICO -> R.string.dashboard_action_diagnosis to Icons.Default.Search
                IngresoAccion.FIRMAR -> R.string.dashboard_action_sign to Icons.Default.Draw
                IngresoAccion.DEVOLVER_A_REPARACION -> R.string.dashboard_action_return_repair to Icons.Default.Undo
                IngresoAccion.ENTREGAR -> R.string.dashboard_action_deliver to Icons.Default.LocalShipping
                IngresoAccion.EDITAR -> R.string.dashboard_action_edit to Icons.Default.Edit
                IngresoAccion.CANCELAR -> R.string.dashboard_action_cancel to Icons.Default.Cancel
                IngresoAccion.ELIMINAR -> R.string.dashboard_action_delete to Icons.Default.Delete
                IngresoAccion.REGULARIZAR_ESTADO -> R.string.dashboard_action_regularize to Icons.Default.Rule
            }

            val esDestructiva =
                accion == IngresoAccion.ELIMINAR ||
                        accion == IngresoAccion.CANCELAR

            TextButton(
                enabled = habilitadas,
                onClick = { onAccion(accion) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (esDestructiva) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Icon(imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = stringResource(textoRes),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
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

@Preview(
    name = "Ingreso · teléfono",
    showBackground = true,
    widthDp = 360
)
@Composable
private fun IngresoCardTelefonoPreview() {
    MyApplicationTheme(darkTheme = false) {
        IngresoCard(
            ingreso = ingresoEjemplo,
            modifier = Modifier.padding(16.dp),
            acciones = listOf(IngresoAccion.EDITAR),
            onAccion = { _, _ -> },
            onVerDetalle = {}
        )
    }
}

@Preview(
    name = "Ingreso · espacio amplio",
    showBackground = true,
    widthDp = 700
)
@Composable
private fun IngresoCardAmpliaPreview() {
    MyApplicationTheme(darkTheme = false) {
        IngresoCard(
            ingreso = ingresoEjemplo,
            modifier = Modifier.padding(16.dp),
            acciones = listOf(IngresoAccion.EDITAR),
            onAccion = { _, _ -> },
            onVerDetalle = {}
        )
    }
}

@Preview(
    name = "Ingreso · oscuro",
    showBackground = true,
    widthDp = 360
)
@Composable
private fun IngresoCardOscuraPreview() {
    MyApplicationTheme(darkTheme = true) {
        IngresoCard(
            ingreso = ingresoEjemplo,
            modifier = Modifier.padding(16.dp),
            acciones = listOf(IngresoAccion.EDITAR),
            onAccion = { _, _ -> },
            onVerDetalle = {}
        )
    }
}