package com.example.myapplication

import com.example.myapplication.data.respaldo.EstadoDeIngreso
import com.example.myapplication.data.respaldo.IngresoComparado
import com.example.myapplication.data.respaldo.Lado
import com.example.myapplication.data.respaldo.ModoCombinacion
import com.example.myapplication.data.respaldo.PlanCombinacion
import com.example.myapplication.data.respaldo.ReglasCombinacion
import com.example.myapplication.data.respaldo.RutasRespaldo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasCombinacionTest {

    @Test
    fun placa_seIgualaSinGuionesEspaciosNiMayusculas() {
        assertEquals("KLM482B", ReglasCombinacion.normalizarPlaca("klm-482-b"))
        assertEquals(ReglasCombinacion.normalizarPlaca("KLM 482 B"), ReglasCombinacion.normalizarPlaca("KLM-482-B")) }

    @Test
    fun telefono_seIgualaConOSinLadaDelPais() {
        assertEquals("5512345678", ReglasCombinacion.normalizarTelefono("55 1234 5678"))
        assertEquals("5512345678", ReglasCombinacion.normalizarTelefono("+52 1 55 1234 5678"))
        assertEquals("5512345678", ReglasCombinacion.normalizarTelefono("(55) 1234-5678")) }

    @Test
    fun texto_ignoraAcentosMayusculasYEspacios() {
        assertEquals("jose perez", ReglasCombinacion.normalizarTexto("  José   PÉREZ "))
        assertEquals("", ReglasCombinacion.normalizarTexto(null)) }

    @Test
    fun cliente_esElMismoSiCoincidenTelefonoYNombre() {
        assertTrue(ReglasCombinacion.mismoCliente("Marta Ruiz", "55 1234 5678", "marta  RUIZ", "+52 5512345678"))
        assertFalse(ReglasCombinacion.mismoCliente("Marta Ruiz", "5512345678", "Marta Ruiz", "5598765432"))
        assertFalse(ReglasCombinacion.mismoCliente("Marta Ruiz", "5512345678", "Luis Pérez", "5512345678"))
        // Sin teléfono no se puede asegurar que sea la misma persona.
        assertFalse(ReglasCombinacion.mismoCliente("Marta Ruiz", "", "Marta Ruiz", "")) }

    @Test
    fun ingreso_esElMismoConIgualVehiculoFechaYMotivo() {
        assertEquals(ReglasCombinacion.claveIngreso(1, 100, "No enfría el aire"),
            ReglasCombinacion.claveIngreso(1, 100, "  no enfría   EL aire "))
        assertEquals(ReglasCombinacion.claveIngreso(1, 100, null), ReglasCombinacion.claveIngreso(1, 100, ""))
        assertFalse(ReglasCombinacion.claveIngreso(1, 100, "a") == ReglasCombinacion.claveIngreso(2, 100, "a"))
        assertFalse(ReglasCombinacion.claveIngreso(1, 100, "a") == ReglasCombinacion.claveIngreso(1, 101, "a")) } }

class IngresoComparadoTest {

    private fun lado(estado: String = "EN_REVISION", movimiento: Long = 1000, fotos: Int = 3, firma: Boolean = false) =
        EstadoDeIngreso(estado, movimiento, fotos, firma)

    private fun ingreso(celular: EstadoDeIngreso?, respaldo: EstadoDeIngreso, id: Long = 1) =
        IngresoComparado(id, celular?.let { 10 + id }, "KLM482B", 1, "motivo", celular, respaldo)

    @Test
    fun clasifica_nuevoIgualYDistinto() {
        assertTrue(ingreso(null, lado()).esNuevo)
        assertTrue(ingreso(lado(), lado()).esIgual)
        assertTrue(ingreso(lado(), lado(estado = "LISTO_PARA_FIRMA")).esDistinto)
        assertTrue(ingreso(lado(), lado(fotos = 5)).esDistinto)
        assertTrue(ingreso(lado(), lado(firma = true)).esDistinto)
        assertFalse(ingreso(lado(), lado()).esDistinto) }

    @Test
    fun masReciente_ganaElQueTieneElUltimoMovimientoMasNuevo() {
        assertEquals(Lado.RESPALDO, ingreso(lado(movimiento = 1000), lado(movimiento = 2000)).ganadorSegun(ModoCombinacion.MAS_RECIENTE))
        assertEquals(Lado.CELULAR, ingreso(lado(movimiento = 3000), lado(movimiento = 2000)).ganadorSegun(ModoCombinacion.MAS_RECIENTE))
        // Con empate se queda lo del celular.
        assertEquals(Lado.CELULAR, ingreso(lado(movimiento = 1000, fotos = 1), lado(movimiento = 1000, fotos = 4))
            .ganadorSegun(ModoCombinacion.MAS_RECIENTE)) }

    @Test
    fun modosFijos_ignoranLasFechas() {
        val i = ingreso(lado(movimiento = 5000), lado(movimiento = 1000))
        assertEquals(Lado.CELULAR, i.ganadorSegun(ModoCombinacion.SIEMPRE_CELULAR))
        assertEquals(Lado.RESPALDO, i.ganadorSegun(ModoCombinacion.SIEMPRE_RESPALDO)) }

    @Test
    fun plan_agrupaLosIngresos() {
        val plan = PlanCombinacion(listOf(
            ingreso(null, lado(), 1), ingreso(null, lado(), 2), ingreso(lado(), lado(), 3),
            ingreso(lado(), lado(estado = "ENTREGADO"), 4)), vehiculosNuevos = 2, clientesNuevos = 1)
        assertEquals(2, plan.nuevos.size)
        assertEquals(1, plan.iguales)
        assertEquals(listOf(4L), plan.distintos.map { it.idRespaldo }) } }

class RutasGuardadasTest {

    @Test
    fun entradaDeRutaGuardada_reconoceFotosYFirmasDeCualquierCelular() {
        assertEquals("fotos/a.jpg", RutasRespaldo.entradaDeRutaGuardada("/data/user/0/com.example.myapplication/files/a.jpg"))
        assertEquals("firmas/f.png", RutasRespaldo.entradaDeRutaGuardada("/data/user/10/otra/files/firmas/f.png"))
        assertEquals("fotos/a.jpg", RutasRespaldo.entradaDeRutaGuardada("C:\\datos\\files\\a.jpg")) } }
