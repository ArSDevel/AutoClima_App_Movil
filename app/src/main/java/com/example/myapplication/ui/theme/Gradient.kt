package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Brush

object AutoClimasGradients {
    // Fondo original
    val loginBackground: Brush = Brush.verticalGradient(
        colors = listOf(
            AutoClimasBlueLight,
            AutoClimasBlueDark,
            AutoClimasNavy
        )
    )

    // Degradado original del botón
    val primaryButton: Brush = Brush.horizontalGradient(
        colors = listOf(
            AutoClimasBlue,
            AutoClimasBlueDark
        )
    )
}