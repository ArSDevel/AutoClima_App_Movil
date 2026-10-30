package com.example.myapplication

import com.example.myapplication.data.reporte.FormatoReporte
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Pruebas del teléfono de WhatsApp y del formato de importes del reporte.
class FormatoReporteTest {

    @Test
    fun telefono_de10Digitos_agregaLadaDeMexico() {
        assertEquals("525512345678", FormatoReporte.telefonoWhatsApp("55 1234 5678"))
        assertEquals("525512345678", FormatoReporte.telefonoWhatsApp("(55) 1234-5678")) }

    @Test
    fun telefono_conLadaDePais_seConserva() {
        assertEquals("525512345678", FormatoReporte.telefonoWhatsApp("+52 55 1234 5678"))
        assertEquals("525512345678", FormatoReporte.telefonoWhatsApp("+52 1 55 1234 5678")) }

    @Test
    fun telefono_invalido_devuelveNull() {
        assertNull(FormatoReporte.telefonoWhatsApp(""))
        assertNull(FormatoReporte.telefonoWhatsApp("55 1234"))
        assertNull(FormatoReporte.telefonoWhatsApp("sin telefono")) }

    // El formato de moneda del dispositivo puede variar, por eso solo se comprueba el importe.
    @Test
    fun pesos_usaDosDecimalesSinRedondearConDouble() {
        val texto = FormatoReporte.pesos(395000)
        assertEquals(true, texto.contains("3,950.00")) } }

// El teléfono solo se acepta con 10 dígitos, con 52 o con + y lada de país.
class TelefonoWhatsAppTest {

    @Test
    fun telefono_de11DigitosSinMas_noSeAcepta() {
        assertNull(FormatoReporte.telefonoWhatsApp("55123456789")) }

    @Test
    fun telefono_internacionalConMas_seAcepta() {
        assertEquals("14155552671", FormatoReporte.telefonoWhatsApp("+1 415 555 2671")) } }
