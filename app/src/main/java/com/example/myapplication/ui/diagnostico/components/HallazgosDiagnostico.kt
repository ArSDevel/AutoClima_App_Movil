package com.example.myapplication.ui.diagnostico.components

import androidx.annotation.StringRes
import com.example.myapplication.R
import com.example.myapplication.ui.diagnostico.HallazgoDiagnostico

// Traduce cada hallazgo a un recurso de texto.
// La interpretación de las respuestas se realiza en DiagnosticoUiState.
@StringRes
internal fun descripcionHallazgo(
    hallazgo: HallazgoDiagnostico
): Int {
    return when (hallazgo) {
        HallazgoDiagnostico.FALTA_REFRIGERANTE ->
            R.string.diagnostico_finding_refrigerant

        HallazgoDiagnostico.FUGA_VISIBLE_MANGUERAS ->
            R.string.diagnostico_finding_hoses

        HallazgoDiagnostico.FUGA_DETECTADA_UV ->
            R.string.diagnostico_finding_uv

        HallazgoDiagnostico.COMPRESOR_NO_EMBRAGA ->
            R.string.diagnostico_finding_engagement

        HallazgoDiagnostico.COMPRESOR_DANADO ->
            R.string.diagnostico_finding_compressor

        HallazgoDiagnostico.BANDA_DESGASTADA ->
            R.string.diagnostico_finding_belt

        HallazgoDiagnostico.VENTILADORES_NO_FUNCIONAN ->
            R.string.diagnostico_finding_fans

        HallazgoDiagnostico.FILTRO_OBSTRUIDO ->
            R.string.diagnostico_finding_filter

        HallazgoDiagnostico.CONDENSADOR_OBSTRUIDO ->
            R.string.diagnostico_finding_condenser } }