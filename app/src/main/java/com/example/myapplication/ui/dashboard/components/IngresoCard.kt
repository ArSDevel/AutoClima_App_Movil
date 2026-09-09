package com.example.myapplication.ui.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.ui.dashboard.IngresoAccion
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.DateFormat
import java.util.Date
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.ui.theme.estadoIngresoColors

@Composable
fun IngresoCard(
    ingreso: IngresoResumen,
    modifier: Modifier = Modifier,
    acciones: List<IngresoAccion> = emptyList(),
    onAccion: ((IngresoAccion, Long) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    val locale = LocalConfiguration.current.locales[0]

    val estado = EstadoIngreso.desdeValor(ingreso.estado)
    val estadoColors = MaterialTheme.estadoIngresoColors

    val colorEstado = when (estado) {
        EstadoIngreso.REGISTRADO -> estadoColors.registrado
        EstadoIngreso.EN_REVISION -> estadoColors.enRevision
        EstadoIngreso.EN_REPARACION -> estadoColors.enReparacion
        EstadoIngreso.LISTO_PARA_FIRMA -> estadoColors.listoParaFirma
        EstadoIngreso.LISTO_PARA_ENTREGA -> estadoColors.listoParaEntrega
        EstadoIngreso.NO_DISPONIBLE -> estadoColors.noDisponible
    }

    val textoEstadoRes = when (estado) {
        EstadoIngreso.REGISTRADO -> R.string.dashboard_status_registered
        EstadoIngreso.EN_REVISION -> R.string.dashboard_status_review
        EstadoIngreso.EN_REPARACION -> R.string.dashboard_status_repair
        EstadoIngreso.LISTO_PARA_FIRMA -> R.string.dashboard_status_signature
        EstadoIngreso.LISTO_PARA_ENTREGA -> R.string.dashboard_status_delivery
        EstadoIngreso.NO_DISPONIBLE -> R.string.dashboard_status_unknown
    }

    val fechaFormateada = remember(ingreso.fechaEntrada, locale) {
        DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(ingreso.fechaEntrada))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = colors.surface,
            contentColor = colors.onSurface
        ),
        border = BorderStroke(width = 2.dp, color = colorEstado),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(textoEstadoRes),
                style = MaterialTheme.typography.labelLarge,
                color = colorEstado
            )
            Box(
                modifier = Modifier.width(40.dp).height(4.dp)
                    .clip(shapes.extraSmall).background(colorEstado))
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Se elige una config dependiendo del ancho
                if (maxWidth >= 520.dp && acciones.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        DatosIngreso(
                            ingreso = ingreso,
                            fechaFormateada = fechaFormateada,
                            modifier = Modifier.weight(1f)
                        )
                        AccionesIngreso(
                            acciones = acciones,
                            habilitadas = onAccion != null,
                            onAccion = { accion -> onAccion?.invoke(accion, ingreso.ingresoId) }
                        )
                    }
                } else {
                    // En poco espacio, las acciones pasan debajo
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DatosIngreso(
                            ingreso = ingreso,
                            fechaFormateada = fechaFormateada,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (acciones.isNotEmpty()) {
                            HorizontalDivider(color = colors.outlineVariant)
                            AccionesIngreso(
                                acciones = acciones,
                                habilitadas = onAccion != null,
                                onAccion = { accion -> onAccion?.invoke(accion, ingreso.ingresoId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatosIngreso(
    ingreso: IngresoResumen,
    fechaFormateada: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DatoIngreso(
            etiqueta = stringResource(R.string.dashboard_plate),
            valor = ingreso.placa,
            destacado = true
        )
        DatoIngreso(
            etiqueta = stringResource(R.string.dashboard_model),
            valor = ingreso.modelo
        )
        DatoIngreso(
            etiqueta = stringResource(R.string.dashboard_color),
            valor = ingreso.color
        )
        DatoIngreso(
            etiqueta = stringResource(R.string.dashboard_serial_number),
            valor = ingreso.numeroSerie
        )
        DatoIngreso(
            etiqueta = stringResource(R.string.dashboard_customer),
            valor = ingreso.nombreCliente
        )
        DatoIngreso(
            etiqueta = stringResource(R.string.dashboard_phone),
            valor = ingreso.telefonoCliente
        )
        DatoIngreso(
            etiqueta = stringResource(R.string.dashboard_email),
            valor = ingreso.emailCliente
        )
        DatoIngreso(
            etiqueta = stringResource(R.string.dashboard_entry_date),
            valor = fechaFormateada
        )
    }
}

@Composable
private fun DatoIngreso(
    etiqueta: String,
    valor: String?,
    destacado: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    val texto = valor?.takeIf { it.isNotBlank() } ?: stringResource(R.string.dashboard_not_registered)

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.onSurface)) {
                append(etiqueta)
                append(": ")
            }

            withStyle(
                SpanStyle(
                    color = if (destacado) { colors.primary } else { colors.onSurfaceVariant },
                    fontWeight = if (destacado) { FontWeight.SemiBold } else { FontWeight.Normal }
                )
            ) { append(texto) }
        },
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AccionesIngreso(
    acciones: List<IngresoAccion>,
    habilitadas: Boolean,
    onAccion: (IngresoAccion) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        acciones.forEach { accion ->
            val textoRes = when (accion) {
                IngresoAccion.VER_PDF -> R.string.dashboard_action_pdf
                IngresoAccion.FIRMAR -> R.string.dashboard_action_sign
                IngresoAccion.CHECKLIST -> R.string.dashboard_action_checklist
                IngresoAccion.EDITAR -> R.string.dashboard_action_edit
                IngresoAccion.ELIMINAR -> R.string.dashboard_action_delete
                IngresoAccion.ENTREGAR -> R.string.dashboard_action_deliver
            }

            val icono = when (accion) {
                IngresoAccion.VER_PDF -> Icons.Default.PictureAsPdf
                IngresoAccion.FIRMAR -> Icons.Default.Draw
                IngresoAccion.CHECKLIST -> Icons.Default.Checklist
                IngresoAccion.EDITAR -> Icons.Default.Edit
                IngresoAccion.ELIMINAR -> Icons.Default.Delete
                IngresoAccion.ENTREGAR -> Icons.Default.LocalShipping
            }

            TextButton(
                enabled = habilitadas,
                onClick = { onAccion(accion) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (accion == IngresoAccion.ELIMINAR) {
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(textoRes),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// Estos datos se utilizan únicamente en los previews
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
    estado = EstadoIngreso.LISTO_PARA_FIRMA.name
)

private val accionesEjemplo = listOf(
    IngresoAccion.VER_PDF,
    IngresoAccion.FIRMAR,
    IngresoAccion.EDITAR,
    IngresoAccion.ELIMINAR
)


// Version poco ancho
@Preview(
    name = "Tarjeta en poteléfonoco ancho",
    showBackground = true,
    widthDp = 360
)
@Composable
private fun IngresoCardTelefonoPreview() {
    MyApplicationTheme(darkTheme = false) {
        IngresoCard(
            ingreso = ingresoEjemplo,
            acciones = accionesEjemplo,
            modifier = Modifier.padding(16.dp),
            onAccion = { _, _ -> }
        )
    }
}

@Preview(
    name = "Tarjeta con espacio amplio",
    showBackground = true,
    widthDp = 700
)
@Composable
private fun IngresoCardAmpliaPreview() {
    MyApplicationTheme(darkTheme = false) {
        IngresoCard(
            ingreso = ingresoEjemplo,
            acciones = accionesEjemplo,
            modifier = Modifier.padding(16.dp),
            onAccion = { _, _ -> }
        )
    }
}