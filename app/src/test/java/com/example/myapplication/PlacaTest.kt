package com.example.myapplication

import com.example.myapplication.data.local.model.FormularioIngreso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas del patrón de la placa.
class PlacaTest {

    @Test
    fun placasConFormatoComun_sonValidas() {
        listOf("ABC-123-D", "A12-BCD", "123-ABC", "ABC-12-34", "abc123d", "KLM-482-B").forEach {
            assertTrue("$it debería ser válida", FormularioIngreso.placaValida(it)) } }

    @Test
    fun placasSinPatron_sonInvalidas() {
        listOf("", "ABC", "ABCDEFG", "1234567", "AB--123", "ABC-123-D-1", "ABC 123", "FDDFDFFD").forEach {
            assertFalse("$it no debería ser válida", FormularioIngreso.placaValida(it)) } }

    @Test
    fun normalizar_quitaSimbolosYPasaAMayusculas() {
        assertEquals("ABC-123", FormularioIngreso.normalizarPlaca("abc 123".replace(' ', '-')))
        assertEquals("ABC123", FormularioIngreso.normalizarPlaca("ab.c 123")) }

    @Test
    fun formulario_marcaLaPlacaInvalida() {
        val errores = FormularioIngreso(placa = "ABCDEFG", modelo = "Versa", nombre = "Ana", telefono = "5512345678").errores()
        assertEquals(FormularioIngreso.MENSAJE_PLACA, errores["placa"]) } }
