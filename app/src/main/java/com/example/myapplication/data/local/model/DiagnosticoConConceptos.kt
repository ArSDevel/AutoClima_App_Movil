package com.example.myapplication.data.local.model

import com.example.myapplication.data.local.entity.ConceptoCotizacion
import com.example.myapplication.data.local.entity.Diagnostico

// Resultado de consultar un diagnóstico y su cotización.
data class DiagnosticoConConceptos(
    val diagnostico: Diagnostico,
    val conceptos: List<ConceptoCotizacion>
)