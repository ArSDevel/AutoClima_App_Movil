package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

// Identidad de marca
val AutoClimasBlueLight = Color(0xFF3B82F6)
val AutoClimasBlue = Color(0xFF2563EB)
val AutoClimasBlueDark = Color(0xFF1D4ED8)
val AutoClimasNavy = Color(0xFF0F172A)
val AutoClimasHeaderBlue = Color(0xFF1E3A8A)
val AutoClimasRed = Color(0xFFC51F32)
val AutoClimasRedLight = Color(0xFFFFB2BB)
val AutoClimasRedContainer = Color(0xFFFFE3E6)
val AutoClimasRedDeep = Color(0xFF680B1B)

// Tema claro: fondos y superficies
val AutoClimasWhite = Color(0xFFFFFFFF)
val AutoClimasIce = Color(0xFFEEF5FF)
val AutoClimasGrayLight = Color(0xFFF1F5F9)

// Niveles para tarjetas, menús y otros contenedores.
internal val AutoClimasLightSurfaceLowest = Color(0xFFFFFFFF)
internal val AutoClimasLightSurfaceLow = Color(0xFFF8FAFC)
internal val AutoClimasLightSurfaceContainer = Color(0xFFF1F5F9)
internal val AutoClimasLightSurfaceHigh = Color(0xFFE8EEF6)
internal val AutoClimasLightSurfaceHighest = Color(0xFFDFE7F1)

// Tema claro: textos y bordes
val AutoClimasSlateDark = Color(0xFF1E293B)
val AutoClimasSlate = Color(0xFF334155)
val AutoClimasSlateMuted = Color(0xFF64748B)
val AutoClimasBorder = Color(0xFFCBD5E1)

// Tema oscuro: fondos y superficies
internal val AutoClimasDarkBackground = Color(0xFF05080F)
// Superficie principal de tarjetas y paneles.
internal val AutoClimasDarkSurface = Color(0xFF090F19)
// Superficie secundaria para agrupar contenido.
internal val AutoClimasDarkSurfaceVariant = Color(0xFF0D1522)
// Encabezados y navegación.
internal val AutoClimasDarkHeader = Color(0xFF080F1B)

// Niveles oscuros con una diferencia sutil de luminosidad.
internal val AutoClimasDarkSurfaceLowest = Color(0xFF03060B)
internal val AutoClimasDarkSurfaceLow = Color(0xFF090F19)
internal val AutoClimasDarkSurfaceContainer = Color(0xFF0D1522)
internal val AutoClimasDarkSurfaceHigh = Color(0xFF121D2C)
internal val AutoClimasDarkSurfaceHighest = Color(0xFF182638)

// Tema oscuro: acciones y acentos
internal val AutoClimasDarkPrimary = Color(0xFF4C8DFF)
internal val AutoClimasDarkOnPrimary = Color(0xFF071426)
internal val AutoClimasDarkPrimaryContainer = Color(0xFF153568)

// Rojo más definido, evitando el rosa pastel.
internal val AutoClimasDarkRed = Color(0xFFF05268)
internal val AutoClimasDarkOnRed = Color(0xFF290A10)
internal val AutoClimasDarkRedContainer = Color(0xFF491923)

// Tema oscuro: textos y bordes
internal val AutoClimasDarkText = Color(0xFFE2E8F0)
internal val AutoClimasDarkTextSecondary = Color(0xFFA8B5C6)

internal val AutoClimasDarkOutline = Color(0xFF74859C)
internal val AutoClimasDarkOutlineVariant = Color(0xFF304158)

// Gradientes
// El tema claro conserva los azules de marca.
internal val AutoClimasGradientLightStart = AutoClimasBlueLight
internal val AutoClimasGradientLightMiddle = AutoClimasBlueDark
internal val AutoClimasGradientLightEnd = AutoClimasNavy

// En oscuro, el azul aparece de forma más discreta.
internal val AutoClimasGradientDarkStart = Color(0xFF102C52)
internal val AutoClimasGradientDarkMiddle = Color(0xFF0A172B)
internal val AutoClimasGradientDarkEnd = AutoClimasDarkBackground

// Error: tema claro
val AutoClimasError = Color(0xFFBA1A1A)
val AutoClimasOnError = Color(0xFFFFFFFF)
val AutoClimasErrorContainer = Color(0xFFFFDAD6)
val AutoClimasOnErrorContainer = Color(0xFF410002)

// Error: tema oscuro
val AutoClimasErrorDark = Color(0xFFF05268)
val AutoClimasOnErrorDark = Color(0xFF290A10)
val AutoClimasErrorContainerDark = Color(0xFF491923)
val AutoClimasOnErrorContainerDark = Color(0xFFFFD9DF)

// Estados de los ingresos: tema claro
// Azul: ingreso capturado.
internal val IngresoRegistradoLight = Color(0xFF1D4ED8)
// Violeta: revisión.
internal val IngresoRevisionLight = Color(0xFF7E22CE)
// Ámbar: trabajo de reparación.
internal val IngresoReparacionLight = Color(0xFF946200)
// Verde: listo para firma.
internal val IngresoFirmaLight = Color(0xFF15803D)
// Cian oscuro: listo para entrega.
internal val IngresoEntregaLight = Color(0xFF0E7490)
// Verde azulado: proceso concluido.
internal val IngresoEntregadoLight = Color(0xFF0F766E)
// Rojo: ingreso cancelado.
internal val IngresoCanceladoLight = Color(0xFFB4233D)
// Neutro: estado sin identificar.
internal val IngresoSinEstadoLight = Color(0xFF64748B)

// Estados de los ingresos: tema oscuro
internal val IngresoRegistradoDark = Color(0xFF4C8DFF)
internal val IngresoRevisionDark = Color(0xFFAE7AF7)
internal val IngresoReparacionDark = Color(0xFFD9A62B)
internal val IngresoFirmaDark = Color(0xFF35B978)
internal val IngresoEntregaDark = Color(0xFF29AEC2)
internal val IngresoEntregadoDark = Color(0xFF2CC5A0)
internal val IngresoCanceladoDark = Color(0xFFF05268)
internal val IngresoSinEstadoDark = Color(0xFF94A3B8)