package com.example.myapplication.ui.registro.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.registro.RegistroUiState

/**
 * Estado de espera durante la consulta inicial del expediente.
 * Permite volver sin esperar a que termine la consulta.
 */
@Composable
internal fun CargaInicialRegistro(
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    strokeWidth = 3.dp
                )
            }
        }

        Text(
            text = stringResource(R.string.navigation_loading_form),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        TextButton(onClick = onVolver) {
            Text(
                text = stringResource(
                    R.string.registro_back_dashboard
                )
            )
        }
    }
}

/**
 * Muestra el error de carga y delega el reintento al ViewModel.
 */
@Composable
internal fun ErrorCargaRegistro(
    mensaje: String,
    onReintentar: () -> Unit,
    onVolver: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = colors.errorContainer,
            contentColor = colors.onErrorContainer
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Text(
            text = stringResource(R.string.navigation_form_error),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = mensaje,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onReintentar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.dashboard_retry))
        }

        TextButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.registro_back_dashboard))
        }
    }
}

/**
 * Confirmación visual después de registrar o editar.
 * Las acciones se delegan a la pantalla que utiliza el componente.
 */
@Composable
internal fun ResultadoRegistro(
    uiState: RegistroUiState,
    onNuevaCaptura: () -> Unit,
    onVolver: () -> Unit,
    onVerIngreso: ((Long) -> Unit)?
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Text(
            text = stringResource(
                if (uiState.esEdicion) {
                    R.string.registro_updated_success
                } else {
                    R.string.registro_created_success
                }
            ),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        uiState.idRegistrado?.let { id ->
            Surface(
                shape = MaterialTheme.shapes.small,
                color = colors.surfaceVariant,
                contentColor = colors.onSurfaceVariant
            ) {
                Text(
                    text = stringResource(
                        R.string.registro_saved_identifier,
                        id
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    )
                )
            }

            onVerIngreso?.let { abrir ->
                Button(
                    enabled = !uiState.cargando,
                    onClick = { abrir(id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(
                            R.string.dashboard_card_view_detail
                        )
                    )
                }
            }
        }

        if (!uiState.esEdicion) {
            OutlinedButton(
                enabled = !uiState.cargando,
                onClick = onNuevaCaptura,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.registro_another))
            }
        }

        TextButton(
            enabled = !uiState.cargando,
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.registro_back_dashboard))
        }
    }
}