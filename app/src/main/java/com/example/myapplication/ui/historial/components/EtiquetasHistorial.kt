package com.example.myapplication.ui.historial.components

import androidx.annotation.StringRes
import com.example.myapplication.R
import com.example.myapplication.ui.historial.PeriodoHistorial

val accionesHistorial = listOf(
    "FOTOS", "CREADO", "EDITADO", "ESTADO", "CHECKLIST", "DIAGNOSTICO",
    "EVIDENCIA", "FOTO_PRINCIPAL", "EVIDENCIA_ELIMINADA"
)

@StringRes
fun etiquetaAccionHistorial(accion: String?): Int = when (accion) {
    null -> R.string.historial_all_actions
    "CREADO" -> R.string.historial_created
    "EDITADO" -> R.string.historial_edited
    "ESTADO" -> R.string.historial_state
    "CHECKLIST" -> R.string.historial_checklist
    "DIAGNOSTICO" -> R.string.historial_diagnosis
    "FOTOS" -> R.string.historial_photos
    "EVIDENCIA" -> R.string.historial_photo
    "FOTO_PRINCIPAL" -> R.string.historial_main_photo
    "EVIDENCIA_ELIMINADA" -> R.string.historial_deleted_photo
    else -> R.string.historial_other
}

@StringRes
fun etiquetaPeriodoHistorial(periodo: PeriodoHistorial): Int = when (periodo) {
    PeriodoHistorial.TODOS -> R.string.historial_all_dates
    PeriodoHistorial.HOY -> R.string.historial_today
    PeriodoHistorial.SIETE_DIAS -> R.string.historial_week
    PeriodoHistorial.TREINTA_DIAS -> R.string.historial_month
}

@StringRes
fun etiquetaEstadoHistorial(estado: String): Int = when (estado) {
    "REGISTRADO" -> R.string.historial_registered
    "EN_REVISION" -> R.string.historial_review
    "EN_REPARACION" -> R.string.historial_repair
    "LISTO_PARA_FIRMA" -> R.string.historial_signature
    "LISTO_PARA_ENTREGA" -> R.string.historial_delivery
    "ENTREGADO" -> R.string.historial_delivered
    "CANCELADO" -> R.string.historial_cancelled
    else -> R.string.historial_unknown_state
}
