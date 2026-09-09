package com.example.myapplication.ui.dashboard.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun FiltroEstadoIngreso(
    estadoSeleccionado: EstadoIngreso?,
    onEstadoSeleccionado: (EstadoIngreso?) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val textoSeleccionado = stringResource(etiquetaEstado(estadoSeleccionado))
    val descripcionFiltro = stringResource(R.string.dashboard_filter_description, textoSeleccionado)

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { menuAbierto = true },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.semantics { contentDescription = descripcionFiltro }
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = textoSeleccionado,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = menuAbierto,
            onDismissRequest = { menuAbierto = false }
        ) {
            OpcionEstado(
                texto = stringResource(R.string.dashboard_filter_all),
                seleccionada = estadoSeleccionado == null,
                onClick = { menuAbierto = false
                    onEstadoSeleccionado(null)
                }
            )

            EstadoIngreso.entries.forEach { estado ->
                OpcionEstado(
                    texto = stringResource(etiquetaEstado(estado)),
                    seleccionada = estadoSeleccionado == estado,
                    onClick = { menuAbierto = false
                        onEstadoSeleccionado(estado)
                    }
                )
            }
        }
    }
}

@Composable
private fun OpcionEstado(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = texto, style = MaterialTheme.typography.bodyLarge) },
        trailingIcon = {
            if (seleccionada) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        onClick = onClick
    )
}

@StringRes
private fun etiquetaEstado(estado: EstadoIngreso?): Int {
    return when (estado) {
        null -> R.string.dashboard_filter_all
        EstadoIngreso.REGISTRADO -> R.string.dashboard_status_registered
        EstadoIngreso.EN_REVISION -> R.string.dashboard_status_review
        EstadoIngreso.EN_REPARACION -> R.string.dashboard_status_repair
        EstadoIngreso.LISTO_PARA_FIRMA -> R.string.dashboard_status_signature
        EstadoIngreso.LISTO_PARA_ENTREGA -> R.string.dashboard_status_delivery
        EstadoIngreso.NO_DISPONIBLE -> R.string.dashboard_status_unknown
    }
}

@Preview(showBackground = true)
@Composable
private fun FiltroEstadoIngresoPreview() {
    var estado by remember { mutableStateOf<EstadoIngreso?>(null) }
    MyApplicationTheme(darkTheme = false) {
        FiltroEstadoIngreso(
            estadoSeleccionado = estado,
            onEstadoSeleccionado = { estado = it }
        )
    }
}