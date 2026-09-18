package com.example.myapplication.ui.diagnostico.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import java.text.DateFormat
import java.util.Date

// Muestra los datos del registro como parte del reporte.
// La edición de estos datos se realiza desde el módulo de Registro.
@Composable
fun DatosIngresoDiagnostico(
    ingreso: IngresoResumen,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val estado = EstadoIngreso.desdeValor(ingreso.estado)

    SeccionDiagnostico(
        titulo = stringResource(R.string.diagnostico_data_title),
        descripcion = stringResource(
            R.string.diagnostico_data_description
        ),
        icono = Icons.Default.DirectionsCar,
        modifier = modifier
    ) {
        Text(text = stringResource(
                R.string.diagnostico_data_ingreso,
                ingreso.ingresoId),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant
        )

        // Se escribe el estado explícitamente para no depender
        // únicamente de un color que lo identifique.
        Surface(
            shape = MaterialTheme.shapes.small,
            color = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer
        ) {
            Text(
                text = stringResource(
                    R.string.diagnostico_data_status,
                    stringResource(
                        when (estado) {
                            EstadoIngreso.REGISTRADO ->
                                R.string.dashboard_status_registered
                            EstadoIngreso.EN_REVISION ->
                                R.string.dashboard_status_review
                            EstadoIngreso.EN_REPARACION ->
                                R.string.dashboard_status_repair
                            EstadoIngreso.LISTO_PARA_FIRMA ->
                                R.string.dashboard_status_signature
                            EstadoIngreso.LISTO_PARA_ENTREGA ->
                                R.string.dashboard_status_delivery
                            EstadoIngreso.ENTREGADO ->
                                R.string.dashboard_status_delivered
                            EstadoIngreso.CANCELADO ->
                                R.string.dashboard_status_cancelled
                            EstadoIngreso.NO_DISPONIBLE ->
                                R.string.dashboard_status_unknown
                        }
                    )
                ),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
            )
        }

        // Usa el ancho disponible de esta sección, no el del dispositivo.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth >= 600.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) { DatosVehiculo(
                        ingreso = ingreso,
                        modifier = Modifier.weight(1f))
                    DatosCliente(ingreso = ingreso,
                        modifier = Modifier.weight(1f))
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    DatosVehiculo(ingreso)
                    HorizontalDivider(
                        color = colors.outlineVariant)
                    DatosCliente(ingreso)
                }
            }
        }
        HorizontalDivider(color = colors.outlineVariant)
        TituloGrupoDatos(
            texto = stringResource(
                R.string.diagnostico_data_reception))
        DatoReporte(
            etiqueta = stringResource(
                R.string.diagnostico_data_entry_date
            ),
            valor = formatearFechaIngreso(ingreso.fechaEntrada))
        DatoReporte(
            etiqueta = stringResource(
                R.string.diagnostico_data_reason),
            valor = ingreso.motivoIngreso)
        DatoReporte(etiqueta = stringResource(
                R.string.diagnostico_data_initial_condition),
            valor = ingreso.condicionInicial)
    }
}

@Composable
private fun DatosVehiculo(
    ingreso: IngresoResumen,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TituloGrupoDatos(texto = stringResource(
                R.string.diagnostico_data_vehicle))
        DatoReporte(
            etiqueta = stringResource(R.string.diagnostico_data_plate),
            valor = ingreso.placa)
        DatoReporte(
            etiqueta = stringResource(R.string.diagnostico_data_model),
            valor = ingreso.modelo)
        DatoReporte(
            etiqueta = stringResource(R.string.diagnostico_data_color),
            valor = ingreso.color)
        DatoReporte(
            etiqueta = stringResource(R.string.diagnostico_data_serial),
            valor = ingreso.numeroSerie)
    }
}

@Composable
private fun DatosCliente(
    ingreso: IngresoResumen,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TituloGrupoDatos(texto = stringResource(
                R.string.diagnostico_data_customer))
        DatoReporte(
            etiqueta = stringResource(R.string.diagnostico_data_name),
            valor = ingreso.nombreCliente)
        DatoReporte(
            etiqueta = stringResource(R.string.diagnostico_data_phone),
            valor = ingreso.telefonoCliente)
        DatoReporte(
            etiqueta = stringResource(R.string.diagnostico_data_email),
            valor = ingreso.emailCliente)
    }
}

@Composable
private fun TituloGrupoDatos(
    texto: String
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.semantics {
            heading()
        }
    )
}

// Permite que valores largos, como correos o números de serie,
// ocupen varias líneas sin recortarse.
@Composable
private fun DatoReporte(
    etiqueta: String,
    valor: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = valor?.trim()?.takeIf { it.isNotEmpty() }
                ?: stringResource(R.string.diagnostico_data_not_provided),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface)
    }
}

// Respeta el idioma del dispositivo y muestra también la hora.
@Composable
private fun formatearFechaIngreso(fecha: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT,
        locale).format(Date(fecha))
}