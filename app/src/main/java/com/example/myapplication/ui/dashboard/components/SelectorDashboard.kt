package com.example.myapplication.ui.dashboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

/**
 * Selector compartido por fecha y ordenamiento.
 * Ocupa el ancho disponible y mantiene el texto centrado.
 */
@Composable
fun <T> SelectorDashboard(
    titulo: String,
    seleccionado: T,
    opciones: List<T>,
    etiqueta: (T) -> Int,
    onSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierto by remember {
        mutableStateOf(false)
    }

    val textoSeleccionado = stringResource(etiqueta(seleccionado))

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = { abierto = true },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(
                        R.string.dashboard_selector_value,
                        titulo,
                        textoSeleccionado
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(etiqueta(opcion))
                        )
                    },
                    trailingIcon = {
                        if (opcion == seleccionado) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    onClick = {
                        abierto = false
                        onSeleccionar(opcion)
                    }
                )
            }
        }
    }
}