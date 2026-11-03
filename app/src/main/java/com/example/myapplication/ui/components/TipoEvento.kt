package com.example.myapplication.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.R
import com.example.myapplication.ui.theme.EstadoIngresoColors

// Cada tipo de evento del historial con su título en el detalle del ingreso, su etiqueta en el historial general y su ícono.
// El nombre de cada valor es el texto que los repositorios guardan en evento_ingreso.accion. Un tipo nuevo se agrega solo aquí.
enum class TipoEvento(
    @StringRes val tituloDetalle: Int,
    @StringRes val etiquetaHistorial: Int,
    val icono: ImageVector,
    val color: (EstadoIngresoColors) -> Color) {
    CREADO(R.string.detalle_event_created, R.string.historial_created, Icons.Default.History, { it.registrado }),
    EDITADO(R.string.detalle_event_edited, R.string.historial_edited, Icons.Default.Edit, { it.registrado }),
    ESTADO(R.string.detalle_event_state, R.string.historial_state, Icons.AutoMirrored.Filled.ArrowForward, { it.enRevision }),
    CHECKLIST(R.string.detalle_event_checklist, R.string.historial_checklist, Icons.Default.Checklist, { it.registrado }),
    DIAGNOSTICO(R.string.detalle_event_diagnosis, R.string.historial_diagnosis, Icons.Default.Description, { it.enReparacion }),
    REPORTE(R.string.detalle_event_report, R.string.historial_report, Icons.AutoMirrored.Filled.Send, { it.listoParaFirma }),
    EVIDENCIA(R.string.fotos_event_added, R.string.historial_photo, Icons.Default.CameraAlt, { it.listoParaEntrega }),
    FOTO_PRINCIPAL(R.string.fotos_event_main, R.string.historial_main_photo, Icons.Default.CameraAlt, { it.listoParaEntrega }),
    EVIDENCIA_ELIMINADA(R.string.fotos_event_deleted, R.string.historial_deleted_photo, Icons.Default.CameraAlt,
        { it.listoParaEntrega }),
    EVIDENCIA_EDITADA(R.string.fotos_event_description, R.string.historial_edited_photo, Icons.Default.CameraAlt,
        { it.listoParaEntrega });

    companion object {
        // null si la acción no es de un tipo conocido, por ejemplo en eventos de otra versión de la app.
        fun desde(accion: String?): TipoEvento? = entries.firstOrNull { it.name == accion } } }
