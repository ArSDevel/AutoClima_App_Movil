package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

//Colores de los estados de un ingreso.
@Immutable
data class EstadoIngresoColors(
    val registrado: Color,
    val enRevision: Color,
    val enReparacion: Color,
    val listoParaFirma: Color,
    val listoParaEntrega: Color,
    val noDisponible: Color,
    val entregado: Color,
    val cancelado: Color
)

// Paleta para superficies claras
internal val LightEstadoIngresoColors = EstadoIngresoColors(
    registrado = IngresoRegistradoLight,
    enRevision = IngresoRevisionLight,
    enReparacion = IngresoReparacionLight,
    listoParaFirma = IngresoFirmaLight,
    listoParaEntrega = IngresoEntregaLight,
    noDisponible = IngresoSinEstadoLight,
    entregado = IngresoEntregadoLight,
    cancelado = IngresoCanceladoLight
)

// Paleta para superficies oscuras
internal val DarkEstadoIngresoColors = EstadoIngresoColors(
    registrado = IngresoRegistradoDark,
    enRevision = IngresoRevisionDark,
    enReparacion = IngresoReparacionDark,
    listoParaFirma = IngresoFirmaDark,
    listoParaEntrega = IngresoEntregaDark,
    noDisponible = IngresoSinEstadoDark,
    entregado = IngresoEntregadoDark,
    cancelado = IngresoCanceladoDark
)

// MyApplicationTheme proporciona la paleta correspondiente al tema seleccionado.
internal val LocalEstadoIngresoColors = staticCompositionLocalOf {
    LightEstadoIngresoColors
}

// Acceso a la paleta desde componentes Compose.
val MaterialTheme.estadoIngresoColors: EstadoIngresoColors
    @Composable
    @ReadOnlyComposable
    get() = LocalEstadoIngresoColors.current