package com.example.myapplication

import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.ReglasIngreso
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de los cambios de estado permitidos.
class ReglasIngresoTest {

    @Test
    fun avanceNormal_seHaceUnPasoPorVez() {
        assertTrue(ReglasIngreso.puedeCambiarEstado(EstadoIngreso.REGISTRADO, EstadoIngreso.EN_REVISION))
        assertTrue(ReglasIngreso.puedeCambiarEstado(EstadoIngreso.EN_REVISION, EstadoIngreso.EN_REPARACION))
        assertTrue(ReglasIngreso.puedeCambiarEstado(EstadoIngreso.EN_REPARACION, EstadoIngreso.LISTO_PARA_FIRMA))
        assertFalse(ReglasIngreso.puedeCambiarEstado(EstadoIngreso.REGISTRADO, EstadoIngreso.EN_REPARACION))
        assertFalse(ReglasIngreso.puedeCambiarEstado(EstadoIngreso.EN_REVISION, EstadoIngreso.LISTO_PARA_FIRMA)) }

    @Test
    fun firmaYEntrega_noSePuedenSaltarConUnMotivo() {
        // Quedan para el equipo que desarrolla esos módulos.
        assertFalse(ReglasIngreso.puedeCambiarEstado(EstadoIngreso.LISTO_PARA_FIRMA, EstadoIngreso.LISTO_PARA_ENTREGA))
        assertFalse(ReglasIngreso.puedeCambiarEstado(EstadoIngreso.LISTO_PARA_ENTREGA, EstadoIngreso.ENTREGADO)) }

    @Test
    fun devolverAReparacion_sigueDisponibleDesdeFirma() {
        assertTrue(ReglasIngreso.puedeCambiarEstado(EstadoIngreso.LISTO_PARA_FIRMA, EstadoIngreso.EN_REPARACION)) } }
