package com.example.myapplication.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

object AutoClimasGradients {
    // Fondo azul de marca para el tema claro
    private val lightBackground: Brush = Brush.verticalGradient(
        colors = listOf(
            AutoClimasGradientLightStart,
            AutoClimasGradientLightMiddle,
            AutoClimasGradientLightEnd
        )
    )
    // Fondo azul profundo para el tema oscuro
    private val darkBackground: Brush = Brush.verticalGradient(
        colors = listOf(
            AutoClimasGradientDarkStart,
            AutoClimasGradientDarkMiddle,
            AutoClimasGradientDarkEnd
        )
    )

    val loginBackground: Brush
        @Composable
        get() = if (LocalAutoClimasDark.current) {
            darkBackground
        } else {
            lightBackground
        }

    // Cabecera de las pantallas internas.
    val header: Brush
        @Composable
        get() = if (LocalAutoClimasDark.current) {
            Brush.linearGradient(listOf(AutoClimasGradientDarkStart, AutoClimasGradientDarkMiddle))
        } else {
            Brush.linearGradient(listOf(AutoClimasHeaderBlue, AutoClimasBlue))
        }

    // Gradiente de marca para botones principales
    val primaryButton: Brush = Brush.horizontalGradient(
        colors = listOf(
            AutoClimasBlue,
            AutoClimasBlueDark
        )
    )
}