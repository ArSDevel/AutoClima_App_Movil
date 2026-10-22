package com.example.myapplication.data.reporte

import com.example.myapplication.data.local.model.DatosEntrega
import java.io.File

// Variantes del PDF: reporte completo, cotización breve para el cliente y acta de entrega.
enum class TipoReporte { COMPLETO, COTIZACION, ACTA }

// Una fila de la cotización ya lista para imprimir.
data class ConceptoReporte(val descripcion: String, val importeCentavos: Long)

// Hallazgo del checklist. grave distingue una falla de algo que solo requiere atención.
data class HallazgoReporte(val texto: String, val grave: Boolean)

// Un punto del checklist con su resultado, para mostrar la revisión completa.
data class ItemChecklistReporte(val texto: String, val ok: Boolean)

// Fotografía con su descripción, para colocarla dentro del PDF.
data class FotoReporte(val archivo: File, val pie: String)

// Firma del cliente tal como se guardó.
data class FirmaReporte(val archivo: File, val fecha: Long)

// Todo lo que lleva el reporte. Es texto ya traducido, no depende de la pantalla.
// La pantalla llena lo que ya tiene a la vista; el ViewModel agrega técnico, firma y entrega.
data class DatosReporte(
    val ingresoId: Long,
    val placa: String,
    val modelo: String,
    val color: String?,
    val serie: String?,
    val cliente: String,
    val telefono: String,
    val correo: String?,
    val fechaEntrada: Long,
    val motivo: String?,
    val condicionInicial: String?,
    val hallazgos: List<HallazgoReporte>,
    val checklist: List<ItemChecklistReporte>,
    val comentariosChecklist: String,
    val observaciones: String,
    val conceptos: List<ConceptoReporte>,
    val totalCentavos: Long?,
    val fotos: List<FotoReporte>,
    // Técnico que guardó el diagnóstico.
    val tecnicoId: Long?,
    val tecnicoNombre: String? = null,
    val firma: FirmaReporte? = null,
    val entrega: DatosEntrega? = null)
