package com.example.myapplication.ui.firma

import androidx.compose.ui.geometry.Offset
import com.example.myapplication.data.local.entity.ConceptoCotizacion
import com.example.myapplication.data.local.entity.Firma
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.data.local.model.ReglasIngreso

// Estado de la pantalla de firma. Los trazos se guardan en proporción al recuadro (de 0 a 1)
// para que se conserven al girar el teléfono.
data class FirmaUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val ingreso: IngresoResumen? = null,
    // null cuando todavía no hay cotización guardada.
    val totalCentavos: Long? = null,
    // Filas de la cotización, para el acta que se envía al cliente.
    val conceptos: List<ConceptoCotizacion> = emptyList(),
    val firmaGuardada: Firma? = null,
    val trazos: List<List<Offset>> = emptyList(),
    val guardando: Boolean = false,
    val error: String? = null,
    val firmaConfirmada: Boolean = false
) {
    val estadoActual: EstadoIngreso?
        get() = ingreso?.let { EstadoIngreso.desdeValor(it.estado) }

    // Solo se firma mientras el ingreso está listo para firma.
    val puedeFirmar: Boolean
        get() = estadoActual?.let { ReglasIngreso.firmar(it) } == true && !firmaConfirmada

    val hayTrazos: Boolean
        get() = trazos.any { it.size > 1 }

    val puedeConfirmar: Boolean
        get() = puedeFirmar && hayTrazos && !guardando }
