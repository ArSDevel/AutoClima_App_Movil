package com.example.myapplication.ui.historial.components

import androidx.annotation.StringRes
import com.example.myapplication.R
import com.example.myapplication.ui.components.TipoEvento

// Texto de cada tipo de actividad. null es el filtro "todas" y FOTOS agrupa los eventos de fotografías.
@StringRes
fun etiquetaAccionHistorial(accion: String?): Int = when (accion) {
    null -> R.string.historial_all_actions
    "FOTOS" -> R.string.historial_photos
    else -> TipoEvento.desde(accion)?.etiquetaHistorial ?: R.string.historial_other }

// Texto de cada estado guardado en un evento.
@StringRes
fun etiquetaEstadoHistorial(estado: String): Int = when (estado) {
    "REGISTRADO" -> R.string.historial_registered
    "EN_REVISION" -> R.string.historial_review
    "EN_REPARACION" -> R.string.historial_repair
    "LISTO_PARA_FIRMA" -> R.string.historial_signature
    "LISTO_PARA_ENTREGA" -> R.string.historial_delivery
    "ENTREGADO" -> R.string.historial_delivered
    "CANCELADO" -> R.string.historial_cancelled
    else -> R.string.historial_unknown_state }
