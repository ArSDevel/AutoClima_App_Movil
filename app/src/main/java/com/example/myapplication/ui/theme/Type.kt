package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

// Fuente del texto del cuerpo, como en el rediseño.
// Es una fuente variable: en Android 8 o superior se aplica cada grosor;
// en versiones anteriores se usa el grosor normal.
val DmSans = FontFamily(
    Font(R.font.dm_sans, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.dm_sans, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.dm_sans, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.dm_sans, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))))

// Fuente de títulos y botones, como en el rediseño.
val Montserrat = FontFamily(
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_bold, FontWeight.ExtraBold))

// Estilos que usa la aplicación: Montserrat para títulos y botones, DM Sans para el texto.
private val TipografiaBase = Typography(
    // Título principal, como "Iniciar sesión".
    headlineMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp),

    // Título de pantalla, como "AutoClimas".
    titleLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp),

    // Títulos de tarjetas o secciones.
    titleMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp),

    // Texto principal y contenido de campos.
    bodyLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp),

    // Descripciones y datos de las tarjetas.
    bodyMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp),

    // Información auxiliar breve.
    bodySmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp),

    // Texto de botones y etiquetas de campos.
    labelLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp),

    // Etiquetas pequeñas.
    labelMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp))

// Los estilos que no se definen arriba también usan DM Sans.
val Typography = TipografiaBase.copy(
    titleSmall = TipografiaBase.titleSmall.copy(fontFamily = DmSans, fontWeight = FontWeight.SemiBold),
    headlineSmall = TipografiaBase.headlineSmall.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold),
    labelSmall = TipografiaBase.labelSmall.copy(fontFamily = DmSans, fontWeight = FontWeight.Medium))
