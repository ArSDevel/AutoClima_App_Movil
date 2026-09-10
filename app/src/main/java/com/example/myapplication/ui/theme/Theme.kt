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
import androidx.compose.ui.platform.LocalContext

// Colores generales para modo claro.
private val LightColorScheme = lightColorScheme(
    primary = AutoClimasBlue,
    onPrimary = AutoClimasWhite,
    primaryContainer = AutoClimasIce,
    onPrimaryContainer = AutoClimasHeaderBlue,

    secondary = AutoClimasRed,
    onSecondary = AutoClimasWhite,
    secondaryContainer = AutoClimasRedContainer,
    onSecondaryContainer = AutoClimasRedDeep,

    tertiary = AutoClimasHeaderBlue,
    onTertiary = AutoClimasWhite,
    tertiaryContainer = AutoClimasIce,
    onTertiaryContainer = AutoClimasNavy,

    background = AutoClimasGrayLight,
    onBackground = AutoClimasNavy,

    surface = AutoClimasWhite,
    onSurface = AutoClimasSlateDark,
    surfaceVariant = AutoClimasIce,
    onSurfaceVariant = AutoClimasSlateMuted,

    outline = AutoClimasSlateMuted,
    outlineVariant = AutoClimasBorder,

    error = AutoClimasError,
    onError = AutoClimasOnError,
    errorContainer = AutoClimasErrorContainer,
    onErrorContainer = AutoClimasOnErrorContainer
)

// Colores generales para modo oscuro.
private val DarkColorScheme = darkColorScheme(
    primary = AutoClimasDarkPrimary,
    onPrimary = AutoClimasDarkOnPrimary,
    primaryContainer = AutoClimasDarkPrimaryContainer,
    onPrimaryContainer = AutoClimasDarkText,

    secondary = AutoClimasDarkRed,
    onSecondary = AutoClimasDarkOnRed,
    secondaryContainer = AutoClimasDarkRedContainer,
    onSecondaryContainer = AutoClimasDarkText,

    tertiary = AutoClimasDarkHeader,
    onTertiary = AutoClimasDarkText,
    tertiaryContainer = AutoClimasDarkSurfaceVariant,
    onTertiaryContainer = AutoClimasDarkText,

    background = AutoClimasDarkBackground,
    onBackground = AutoClimasDarkText,

    surface = AutoClimasDarkSurface,
    onSurface = AutoClimasDarkText,
    surfaceVariant = AutoClimasDarkSurfaceVariant,
    onSurfaceVariant = AutoClimasDarkTextSecondary,
    surfaceTint = AutoClimasDarkPrimary,

    outline = AutoClimasDarkOutline,
    outlineVariant = AutoClimasDarkOutlineVariant,

    error = AutoClimasErrorDark,
    onError = AutoClimasOnErrorDark,
    errorContainer = AutoClimasErrorContainerDark,
    onErrorContainer = AutoClimasOnErrorContainerDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Selecciona los colores generales de la aplicación.
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme

        else -> LightColorScheme
    }

    // Selecciona los colores específicos de los estados del ingreso.
    val estadoColors = if (darkTheme) {
        DarkEstadoIngresoColors
    } else {
        LightEstadoIngresoColors
    }

    // Permite consultar los colores de estado desde las pantallas.
    CompositionLocalProvider(
        LocalEstadoIngresoColors provides estadoColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AutoClimasShapes,
            content = content
        )
    }
}