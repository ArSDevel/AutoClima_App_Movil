package com.example.myapplication.ui.camara

import com.example.myapplication.data.local.entity.Evidencia
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.data.local.model.ReglasIngreso

// Información que utilizan la cámara y la galería del ingreso.
data class CamaraUiState(
    // Carga del expediente y de sus fotografías.
    val cargando: Boolean = true,
    val ingreso: IngresoResumen? = null,
    val evidencias: List<Evidencia> = emptyList(),
    val ingresoNoEncontrado: Boolean = false,
    val errorCarga: String? = null,

    // Fotografía capturada que todavía debe registrarse.
    // Se conserva para reintentar si falla el guardado.
    val rutaPendiente: String? = null,
    val descripcion: String = "",

    // Estado del guardado.
    val guardando: Boolean = false,
    val errorGuardado: String? = null,
    val guardadoExitoso: Boolean = false,
    val evidenciaGuardadaId: Long? = null,
    val mensajeGaleria: String? = null,
    val errorGaleria: String? = null
) {

    // null indica que todavía no existe un expediente disponible.
    val estadoActual: EstadoIngreso?
        get() = ingreso?.let {
            EstadoIngreso.desdeValor(it.estado) }

    // Las fotografías se agregan durante la recepción,
    // la revisión o la reparación del vehículo.
    val permiteAgregar: Boolean
        get() {
            val estado = estadoActual ?: return false

            return !cargando &&
                    errorCarga == null &&
                    !ingresoNoEncontrado &&
                    ReglasIngreso.edicionCompleta(estado) }

    // Evita sustituir una fotografía que todavía está pendiente
    // de guardarse o cuyo resultado no ha sido procesado.
    val puedeCapturar: Boolean
        get() = permiteAgregar &&
                !guardando &&
                !guardadoExitoso &&
                rutaPendiente == null

    val puedeGuardar: Boolean
        get() = permiteAgregar &&
                !guardando &&
                !guardadoExitoso &&
                rutaPendiente != null }