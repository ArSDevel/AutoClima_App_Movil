package com.example.myapplication.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.theme.LocalAutoClimasDark
import com.example.myapplication.ui.theme.estadoIngresoColors

private val TextoSecundarioCabecera = Color(0xFFDBEAFE)

/**
 * Cabecera común de las pantallas internas.
 * Muestra el título, un subtítulo opcional y, según corresponda,
 * el botón de menú o el de volver.
 */
@Composable
fun EncabezadoAutoClimas(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    onMenu: (() -> Unit)? = null,
    onVolver: (() -> Unit)? = null,
    habilitado: Boolean = true,
    espacioInferior: Dp = 0.dp,
    iconoVolver: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    descripcionVolver: String? = null,
    contenidoInferior: (@Composable () -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Box(modifier = modifier.fillMaxWidth().background(AutoClimasGradients.header)) {
        Column(
            modifier = Modifier.fillMaxWidth().statusBarsPadding()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 16.dp + espacioInferior),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onVolver != null) {
                BotonCabecera(
                    icono = iconoVolver,
                    descripcion = descripcionVolver ?: stringResource(R.string.detalle_back),
                    habilitado = habilitado,
                    onClick = onVolver
                )
            } else if (onMenu != null) {
                BotonCabecera(
                    icono = Icons.Default.Menu,
                    descripcion = stringResource(R.string.dashboard_open_menu),
                    habilitado = habilitado,
                    onClick = onMenu
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitulo != null) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundarioCabecera,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            acciones()
        }
        contenidoInferior?.invoke()
        }
    }
}

// Logo de AutoClimas. Es un PNG con fondo transparente; sobre fondos oscuros se usa
// la versión con el texto claro para que se lea sin necesidad de una placa blanca.
@Composable
fun LogoAutoClimas(
    modifier: Modifier = Modifier,
    sobreFondoOscuro: Boolean = LocalAutoClimasDark.current,
    descripcion: String? = null
) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(
            if (sobreFondoOscuro) R.drawable.logo_autoclimas_claro else R.drawable.logo_autoclimas
        ),
        contentDescription = descripcion,
        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
        modifier = modifier
    )
}

// Botón circular translúcido para usar sobre la cabecera.
@Composable
fun BotonCabecera(
    icono: ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier.size(44.dp).alpha(if (habilitado) 1f else 0.5f),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.16f),
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icono, contentDescription = descripcion)
        }
    }
}

// Barra inferior con la acción principal de la pantalla.
@Composable
fun BarraAccionInferior(
    modifier: Modifier = Modifier,
    contenido: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = contenido
        )
    }
}

// Etiqueta de estado con color y punto.
@Composable
fun PildoraEstado(
    estado: EstadoIngreso,
    modifier: Modifier = Modifier,
    sobreCabecera: Boolean = false
) {
    val color = if (sobreCabecera) Color.White else colorEstado(estado)
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (sobreCabecera) Color.White.copy(alpha = 0.18f) else color.copy(alpha = 0.14f),
        contentColor = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(
                text = stringResource(etiquetaEstado(estado)),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun colorEstado(estado: EstadoIngreso): Color {
    val colores = MaterialTheme.estadoIngresoColors
    return when (estado) {
        EstadoIngreso.REGISTRADO -> colores.registrado
        EstadoIngreso.EN_REVISION -> colores.enRevision
        EstadoIngreso.EN_REPARACION -> colores.enReparacion
        EstadoIngreso.LISTO_PARA_FIRMA -> colores.listoParaFirma
        EstadoIngreso.LISTO_PARA_ENTREGA -> colores.listoParaEntrega
        EstadoIngreso.ENTREGADO -> colores.entregado
        EstadoIngreso.CANCELADO -> colores.cancelado
        EstadoIngreso.NO_DISPONIBLE -> colores.noDisponible
    }
}

@StringRes
fun etiquetaEstado(estado: EstadoIngreso): Int {
    return when (estado) {
        EstadoIngreso.REGISTRADO -> R.string.dashboard_status_registered
        EstadoIngreso.EN_REVISION -> R.string.dashboard_status_review
        EstadoIngreso.EN_REPARACION -> R.string.dashboard_status_repair
        EstadoIngreso.LISTO_PARA_FIRMA -> R.string.dashboard_status_signature
        EstadoIngreso.LISTO_PARA_ENTREGA -> R.string.dashboard_status_delivery
        EstadoIngreso.ENTREGADO -> R.string.dashboard_status_delivered
        EstadoIngreso.CANCELADO -> R.string.dashboard_status_cancelled
        EstadoIngreso.NO_DISPONIBLE -> R.string.dashboard_status_unknown
    }
}

// Tarjeta compacta para elegir un ingreso: foto, placa, vehículo y cliente, y estado.
@Composable
fun TarjetaSeleccionIngreso(
    ingreso: com.example.myapplication.data.local.model.IngresoResumen,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.example.myapplication.ui.dashboard.components.FotoIngresoCard(
                ruta = ingreso.fotoPath,
                tamano = 72.dp
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = ingreso.placa,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOf(ingreso.modelo, ingreso.nombreCliente).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                PildoraEstado(estado = EstadoIngreso.desdeValor(ingreso.estado))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant
            )
        }
    }
}

enum class TipoAviso { ADVERTENCIA, EXITO }

// Aviso breve con icono, título, una línea de texto y dos botones del mismo tamaño.
// El botón secundario (contorno) es la acción que descarta; el principal (relleno) continúa.
@Composable
fun DialogoAviso(
    tipo: TipoAviso,
    titulo: String,
    mensaje: String,
    textoSecundario: String,
    onSecundario: () -> Unit,
    textoPrincipal: String,
    onPrincipal: () -> Unit,
    onDismiss: () -> Unit,
    habilitado: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    val estados = MaterialTheme.estadoIngresoColors
    val (fondoIcono, colorIcono, icono) = when (tipo) {
        TipoAviso.ADVERTENCIA -> Triple(
            estados.enReparacion.copy(alpha = 0.18f), estados.enReparacion, Icons.Default.Warning)
        TipoAviso.EXITO -> Triple(
            estados.listoParaFirma.copy(alpha = 0.18f), estados.listoParaFirma, Icons.Default.Check)
    }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            color = colors.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(fondoIcono),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icono, contentDescription = null, tint = colorIcono,
                        modifier = Modifier.size(28.dp))
                }
                Text(text = titulo, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold)
                Text(text = mensaje, style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onSecundario,
                        enabled = habilitado,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) { Text(textoSecundario, style = MaterialTheme.typography.titleMedium, maxLines = 1) }
                    androidx.compose.material3.Button(
                        onClick = onPrincipal,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) { Text(textoPrincipal, style = MaterialTheme.typography.titleMedium, maxLines = 1) }
                }
            }
        }
    }
}

// Dato que no se puede modificar: borde punteado, candado y el valor.
@Composable
fun CampoBloqueado(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val borde = colors.outline
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = etiqueta, style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.6f))
                .drawBehind {
                    drawRoundRect(
                        color = borde,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                        )
                    )
                }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = colors.onSurfaceVariant)
            Text(
                text = valor.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Nombre del técnico con sesión iniciada, disponible para el menú lateral.
val LocalNombreTecnico = androidx.compose.runtime.staticCompositionLocalOf<String?> { null }

// "Jesús Martínez Pérez" → "Jesús M."
fun nombreCortoTecnico(nombre: String?): String? {
    val partes = nombre?.trim()?.split(" ")?.filter { it.isNotBlank() } ?: return null
    if (partes.isEmpty()) return null
    return if (partes.size == 1) partes[0] else "${partes[0]} ${partes[1].first()}."
}
