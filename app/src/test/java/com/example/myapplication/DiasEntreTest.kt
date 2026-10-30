package com.example.myapplication

import com.example.myapplication.ui.historial.components.diasEntre
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

// Pruebas del conteo de días del historial (Hoy, Ayer o fecha).
class DiasEntreTest {

    private fun fecha(anio: Int, mes: Int, dia: Int, hora: Int = 12): Long =
        Calendar.getInstance().apply { set(anio, mes, dia, hora, 0, 0) }.timeInMillis

    @Test
    fun mismoDia_sonCeroDias() {
        assertEquals(0, diasEntre(fecha(2026, Calendar.OCTOBER, 30, 23), fecha(2026, Calendar.OCTOBER, 30, 1))) }

    @Test
    fun ayer_aunqueSeaOtroMesOOtroAnio() {
        assertEquals(1, diasEntre(fecha(2026, Calendar.OCTOBER, 30), fecha(2026, Calendar.OCTOBER, 29)))
        assertEquals(1, diasEntre(fecha(2026, Calendar.NOVEMBER, 1), fecha(2026, Calendar.OCTOBER, 31)))
        assertEquals(1, diasEntre(fecha(2027, Calendar.JANUARY, 1, 9), fecha(2026, Calendar.DECEMBER, 31, 23))) }

    @Test
    fun variosDias_seCuentanCompletos() {
        assertEquals(2, diasEntre(fecha(2027, Calendar.JANUARY, 1), fecha(2026, Calendar.DECEMBER, 30))) } }
