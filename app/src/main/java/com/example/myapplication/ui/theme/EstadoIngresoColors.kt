package com.example.myapplication.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

@Immutable
data class EstadoIngresoColors(
    val registrado: Color,
    val enRevision: Color,
    val enReparacion: Color,
    val listoParaFirma: Color,
    val listoParaEntrega: Color,
    val noDisponible: Color
)

internal val LightEstadoIngresoColors = EstadoIngresoColors(
    registrado = IngresoRegistradoLight,
    enRevision = IngresoRevisionLight,
    enReparacion = IngresoReparacionLight,
    listoParaFirma = IngresoFirmaLight,
    listoParaEntrega = IngresoEntregaLight,
    noDisponible = IngresoSinEstadoLight
)

internal val DarkEstadoIngresoColors = EstadoIngresoColors(
    registrado = IngresoRegistradoDark,
    enRevision = IngresoRevisionDark,
    enReparacion = IngresoReparacionDark,
    listoParaFirma = IngresoFirmaDark,
    listoParaEntrega = IngresoEntregaDark,
    noDisponible = IngresoSinEstadoDark
)

internal val LocalEstadoIngresoColors = staticCompositionLocalOf {
    LightEstadoIngresoColors
}

val MaterialTheme.estadoIngresoColors: EstadoIngresoColors
    @Composable
    @ReadOnlyComposable
    get() = LocalEstadoIngresoColors.current