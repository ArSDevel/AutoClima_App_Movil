package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

internal val LocalAutoClimasDark = staticCompositionLocalOf { false }


// Tema claro
private val LightColorScheme = lightColorScheme(
    // Acciones principales
    primary = AutoClimasBlue,
    onPrimary = AutoClimasWhite,
    primaryContainer = AutoClimasIce,
    onPrimaryContainer = AutoClimasHeaderBlue,
    // Acentos de marca
    secondary = AutoClimasRed,
    onSecondary = AutoClimasWhite,
    secondaryContainer = AutoClimasRedContainer,
    onSecondaryContainer = AutoClimasRedDeep,
    // Encabezados y elementos complementarios
    tertiary = AutoClimasHeaderBlue,
    onTertiary = AutoClimasWhite,
    tertiaryContainer = AutoClimasIce,
    onTertiaryContainer = AutoClimasNavy,
    // Fondo general
    background = AutoClimasGrayLight,
    onBackground = AutoClimasNavy,
    // Superficies y textos
    surface = AutoClimasWhite,
    onSurface = AutoClimasSlateDark,
    surfaceVariant = AutoClimasIce,
    onSurfaceVariant = AutoClimasSlateMuted,
    // Niveles de superficie para componentes Material 3
    surfaceDim = AutoClimasLightSurfaceHighest,
    surfaceBright = AutoClimasWhite,
    surfaceContainerLowest = AutoClimasLightSurfaceLowest,
    surfaceContainerLow = AutoClimasLightSurfaceLow,
    surfaceContainer = AutoClimasLightSurfaceContainer,
    surfaceContainerHigh = AutoClimasLightSurfaceHigh,
    surfaceContainerHighest = AutoClimasLightSurfaceHighest,
    // Evita añadir un tinte de color por elevación tonal
    surfaceTint = Color.Transparent,
    // Bordes y separadores
    outline = AutoClimasSlateMuted,
    outlineVariant = AutoClimasBorder,
    // Superficies de contraste, como ciertos mensajes
    inverseSurface = AutoClimasSlateDark,
    inverseOnSurface = AutoClimasGrayLight,
    inversePrimary = AutoClimasDarkPrimary,
    // Errores
    error = AutoClimasError,
    onError = AutoClimasOnError,
    errorContainer = AutoClimasErrorContainer,
    onErrorContainer = AutoClimasOnErrorContainer,
    // Capa que oscurece el contenido detrás de un modal
    scrim = Color.Black
)


// Tema oscuro
private val DarkColorScheme = darkColorScheme(
    // Acciones principales
    primary = AutoClimasDarkPrimary,
    onPrimary = AutoClimasDarkOnPrimary,
    primaryContainer = AutoClimasDarkPrimaryContainer,
    onPrimaryContainer = AutoClimasDarkText,
    // Acentos de marca
    secondary = AutoClimasDarkRed,
    onSecondary = AutoClimasDarkOnRed,
    secondaryContainer = AutoClimasDarkRedContainer,
    onSecondaryContainer = AutoClimasDarkText,
    // Encabezados y elementos complementarios
    tertiary = AutoClimasDarkHeader,
    onTertiary = AutoClimasDarkText,
    tertiaryContainer = AutoClimasDarkSurfaceVariant,
    onTertiaryContainer = AutoClimasDarkText,
    // Fondo general
    background = AutoClimasDarkBackground,
    onBackground = AutoClimasDarkText,
    // Superficies y textos
    surface = AutoClimasDarkSurface,
    onSurface = AutoClimasDarkText,
    surfaceVariant = AutoClimasDarkSurfaceVariant,
    onSurfaceVariant = AutoClimasDarkTextSecondary,
    // Niveles oscuros para tarjetas, menús y diálogos
    surfaceDim = AutoClimasDarkBackground,
    surfaceBright = AutoClimasDarkSurfaceHighest,
    surfaceContainerLowest = AutoClimasDarkSurfaceLowest,
    surfaceContainerLow = AutoClimasDarkSurfaceLow,
    surfaceContainer = AutoClimasDarkSurfaceContainer,
    surfaceContainerHigh = AutoClimasDarkSurfaceHigh,
    surfaceContainerHighest = AutoClimasDarkSurfaceHighest,
    // Mantiene los colores oscuros definidos en Color.kt
    surfaceTint = Color.Transparent,
    // Bordes y separadores.
    outline = AutoClimasDarkOutline,
    outlineVariant = AutoClimasDarkOutlineVariant,
    // Superficies de contraste
    inverseSurface = AutoClimasDarkText,
    inverseOnSurface = AutoClimasDarkBackground,
    inversePrimary = AutoClimasBlueDark,
    // Errores
    error = AutoClimasErrorDark,
    onError = AutoClimasOnErrorDark,
    errorContainer = AutoClimasErrorContainerDark,
    onErrorContainer = AutoClimasOnErrorContainerDark,
    scrim = Color.Black
)

// Tema de la aplicación
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val estadoColors = if (darkTheme) {
        DarkEstadoIngresoColors
    } else {
        LightEstadoIngresoColors
    }
    CompositionLocalProvider(
        LocalEstadoIngresoColors provides estadoColors,
        LocalAutoClimasDark provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AutoClimasShapes,
            content = content
        )
    }
}