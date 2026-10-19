package com.example.myapplication

import com.example.myapplication.data.catalogo.CatalogoVehiculos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogoVehiculosTest {

    @Test
    fun sugerencias_buscaPorMarcaOModelo() {
        assertTrue("Nissan Versa" in CatalogoVehiculos.sugerencias("nis"))
        assertTrue("Nissan Versa" in CatalogoVehiculos.sugerencias("versa"))
        assertTrue("Nissan Versa" in CatalogoVehiculos.sugerencias("versa nis"))
    }

    @Test
    fun sugerencias_ignoraAcentosYMayusculas() {
        assertTrue("SEAT León" in CatalogoVehiculos.sugerencias("LEON"))
    }

    @Test
    fun sugerencias_noRepiteUnaCoincidenciaExacta() {
        assertTrue(CatalogoVehiculos.sugerencias("Nissan Versa").isEmpty())
    }

    @Test
    fun sugerencias_vacioONoEncontrado() {
        assertTrue(CatalogoVehiculos.sugerencias("").isEmpty())
        assertEquals(emptyList<String>(), CatalogoVehiculos.sugerencias("zzzz"))
    }
}
