package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Colores a usar en modo claro
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

// Colores a usar en modo oscuro
private val DarkColorScheme = darkColorScheme(
    primary = AutoClimasBlueLight,
    onPrimary = AutoClimasNavy,
    primaryContainer = AutoClimasHeaderBlue,
    onPrimaryContainer = AutoClimasIce,

    secondary = AutoClimasRedLight,
    onSecondary = AutoClimasRedDeep,
    secondaryContainer = AutoClimasRedDeep,
    onSecondaryContainer = AutoClimasRedContainer,

    tertiary = AutoClimasIce,
    onTertiary = AutoClimasHeaderBlue,
    tertiaryContainer = AutoClimasSlate,
    onTertiaryContainer = AutoClimasIce,

    background = AutoClimasNavy,
    onBackground = AutoClimasGrayLight,

    surface = AutoClimasSlateDark,
    onSurface = AutoClimasGrayLight,
    surfaceVariant = AutoClimasSlate,
    onSurfaceVariant = AutoClimasBorder,

    outline = AutoClimasBorder,
    outlineVariant = AutoClimasSlateMuted,

    error = AutoClimasErrorDark,
    onError = AutoClimasOnErrorDark,
    errorContainer = AutoClimasErrorContainerDark,
    onErrorContainer = AutoClimasErrorContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}