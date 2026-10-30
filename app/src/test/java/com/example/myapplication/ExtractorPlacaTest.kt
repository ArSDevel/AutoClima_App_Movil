package com.example.myapplication

import com.example.myapplication.ui.camara.EstabilizadorPlaca
import com.example.myapplication.ui.camara.ExtractorPlaca
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Pruebas del filtro que deja solo la placa del texto leído por la cámara.
class ExtractorPlacaTest {

    @Test
    fun extrae_laPlacaDeUnTextoConOtrasPalabras() {
        assertEquals("ABC-123-D", ExtractorPlaca.extraer("MEXICO\nABC-123-D\nCDMX 2026"))
        assertEquals("KLM-482-B", ExtractorPlaca.extraer("Nuevo León klm-482-b")) }

    @Test
    fun une_lasPartesSeparadasPorEspacios() {
        assertEquals("ABC-123-D", ExtractorPlaca.extraer("ABC 123 D")) }

    @Test
    fun sinPlaca_devuelveNull() {
        assertNull(ExtractorPlaca.extraer(""))
        assertNull(ExtractorPlaca.extraer("MEXICO CDMX TRANSPORTE PUBLICO"))
        assertNull(ExtractorPlaca.extraer("2026 12345678")) }

    @Test
    fun estabilizador_confirmaSoloTrasVariasLecturasIguales() {
        val estabilizador = EstabilizadorPlaca(lecturasNecesarias = 3)
        assertNull(estabilizador.registrar("ABC-123-D"))
        assertNull(estabilizador.registrar("ABC123D"))
        // Devuelve la primera lectura, que conserva los guiones.
        assertEquals("ABC-123-D", estabilizador.registrar("ABC123D")) }

    @Test
    fun estabilizador_reiniciaSiLaLecturaCambiaOSePierde() {
        val estabilizador = EstabilizadorPlaca(lecturasNecesarias = 2)
        assertNull(estabilizador.registrar("ABC-123-D"))
        assertNull(estabilizador.registrar("XYZ-987-W"))
        assertNull(estabilizador.registrar(null))
        assertNull(estabilizador.registrar("XYZ-987-W"))
        assertEquals("XYZ-987-W", estabilizador.registrar("XYZ-987-W")) } }
