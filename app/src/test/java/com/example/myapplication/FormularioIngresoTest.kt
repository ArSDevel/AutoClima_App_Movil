package com.example.myapplication

import com.example.myapplication.data.local.model.FormularioIngreso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

// Pruebas de las validaciones del formulario de ingreso.
class FormularioIngresoTest {

    private fun valido() = FormularioIngreso(
        placa = "KLM-482-B", modelo = "Nissan Versa", nombre = "Marta Ruiz", telefono = "55 1234 5678",
        motivo = "No enfría el aire")

    @Test
    fun formularioCompleto_noTieneErrores() {
        assertTrue(valido().errores().isEmpty()) }

    @Test
    fun camposObligatorios_seSeñalan() {
        val errores = FormularioIngreso().errores()
        assertTrue(errores.keys.containsAll(listOf("placa", "modelo", "nombre", "telefono", "motivo"))) }

    @Test
    fun telefono_debeTener10Digitos() {
        assertEquals("Faltan dígitos: son 10.", FormularioIngreso.errorTelefono("55 1234"))
        assertNull(FormularioIngreso.errorTelefono("55 1234 5678"))
        assertNull(FormularioIngreso.errorTelefono("+52 55 1234 5678"))
        assertTrue(FormularioIngreso.errorTelefono("55 1234 56789")!!.startsWith("Revisa el teléfono"))
        assertTrue(FormularioIngreso.errorTelefono("55-12AB-5678")!!.startsWith("Usa solo números")) }

    @Test
    fun serie_esOpcionalPeroDebeTener17SinIOQ() {
        assertTrue(valido().copy(numeroSerie = "").errores().isEmpty())
        assertTrue(valido().copy(numeroSerie = "3N1CN7AP4KL482001").errores().isEmpty())
        assertTrue(valido().copy(numeroSerie = "3N1CN7AP4KL48200").errores().containsKey("numeroSerie"))
        assertTrue(valido().copy(numeroSerie = "3N1CN7AP4KL48200I").errores().containsKey("numeroSerie"))
        assertEquals("3N1CN7AP4KL482001", FormularioIngreso.normalizarSerie("3n1-cn7ap4kl 482001999")) }

    @Test
    fun nombre_debeTenerLetras() {
        assertTrue(valido().copy(nombre = "12").errores().containsKey("nombre"))
        assertTrue(valido().copy(nombre = "A").errores().containsKey("nombre"))
        assertTrue(valido().copy(nombre = "José Núñez").errores().isEmpty()) }

    @Test
    fun fecha_noPuedeSerFutura() {
        val hoy = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 21, 12, 0, 0) }.timeInMillis
        assertTrue(valido().copy(fecha = "21/10/2026").errores(ahora = hoy).isEmpty())
        assertTrue(valido().copy(fecha = "20/10/2026").errores(ahora = hoy).isEmpty())
        assertEquals("La fecha de entrada no puede ser posterior a hoy.",
            valido().copy(fecha = "22/10/2026").errores(ahora = hoy)["fecha"]) }

    @Test
    fun longitudes_seLimitan() {
        assertTrue(valido().copy(motivo = "x".repeat(FormularioIngreso.MAXIMO_MOTIVO + 1)).errores().containsKey("motivo"))
        assertTrue(valido().copy(nombre = "A".repeat(FormularioIngreso.MAXIMO_NOMBRE + 1)).errores().containsKey("nombre"))
        assertTrue(valido().copy(email = "a@" + "b".repeat(100) + ".com").errores().containsKey("email")) }

    @Test
    fun soloContacto_noExigeLosDatosDelVehiculo() {
        val errores = FormularioIngreso(telefono = "55 1234 5678").errores(soloContacto = true)
        assertFalse(errores.containsKey("placa"))
        assertTrue(errores.isEmpty()) } }
