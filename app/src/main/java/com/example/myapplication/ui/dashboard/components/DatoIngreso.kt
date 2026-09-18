package com.example.myapplication.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

@Composable
internal fun DatoIngreso(
    etiqueta: String,
    valor: String?,
    icono: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val texto = valor?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.dashboard_not_registered)

    Row(modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(shape = MaterialTheme.shapes.small,
            color = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer
        ) {
            Box(modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) { Icon(imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp))
            }
        }

        Column(modifier = Modifier.weight(1f).padding(top = 1.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant)
            Text(text = texto,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface)
        }
    }
}