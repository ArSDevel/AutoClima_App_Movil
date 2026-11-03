package com.example.myapplication.util

import java.text.DateFormat
import java.util.Date
import java.util.Locale

// Formatos de fecha y hora de la app, en un solo lugar. Las pantallas usan el idioma del teléfono; el reporte PDF pasa
// español de México. Cada llamada crea su propio formato porque DateFormat no es seguro entre hilos.
object FormatoFechas {
    // Fecha y hora, por ejemplo "3 nov 2026 9:10".
    fun fechaYHora(milisegundos: Long, locale: Locale = Locale.getDefault()): String =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale).format(Date(milisegundos))

    // Solo la fecha, con el mes abreviado.
    fun fecha(milisegundos: Long, locale: Locale = Locale.getDefault()): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(milisegundos))

    // Fecha numérica corta, por ejemplo "3/11/26".
    fun fechaCorta(milisegundos: Long, locale: Locale = Locale.getDefault()): String =
        DateFormat.getDateInstance(DateFormat.SHORT, locale).format(Date(milisegundos))

    // Solo la hora.
    fun hora(milisegundos: Long, locale: Locale = Locale.getDefault()): String =
        DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(Date(milisegundos)) }
