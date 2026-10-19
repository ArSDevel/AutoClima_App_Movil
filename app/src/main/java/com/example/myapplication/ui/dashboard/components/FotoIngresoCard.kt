package com.example.myapplication.ui.dashboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.R
import java.io.File
import androidx.compose.foundation.clickable

// Ocupa el lugar del icono de vehículo en la tarjeta existente.
// Los ingresos sin fotografía conservan el icono original.
@Composable
fun FotoIngresoCard(
    ruta: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    tamano: androidx.compose.ui.unit.Dp = 84.dp
) {
    val colors = MaterialTheme.colorScheme
    val tieneFoto = !ruta.isNullOrBlank()
    var cargando by remember(ruta) { mutableStateOf(tieneFoto) }
    var errorImagen by remember(ruta) { mutableStateOf(false) }

    Surface(
        modifier = if (onClick != null) modifier.clickable(
            onClickLabel = stringResource(R.string.fotos_card_action), onClick = onClick
        ) else modifier,
        shape = MaterialTheme.shapes.medium,
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer
    ) {
        Box(
            modifier = Modifier.size(tamano),
            contentAlignment = Alignment.Center
        ) {
            if (tieneFoto) {
                AsyncImage(
                    model = File(requireNotNull(ruta)),
                    contentDescription = stringResource(R.string.registro_evidence_title),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = {
                        cargando = true
                        errorImagen = false },
                    onSuccess = {
                        cargando = false
                        errorImagen = false },
                    onError = {
                        cargando = false
                        errorImagen = true })
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                if (errorImagen) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = stringResource(R.string.evidence_image_unavailable),
                        modifier = Modifier.size(28.dp)) }
            } else {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)) } } } }